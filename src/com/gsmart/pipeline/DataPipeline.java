// Localização: src/com/gsmart/pipeline/DataPipeline.java
package com.gsmart.pipeline;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.gsmart.config.AlertRule;
import com.gsmart.config.InsightRule;
import com.gsmart.config.MetricConfig;
import com.gsmart.conection.ExportacaoDadosPWBI;
import com.gsmart.resources.GSmartListener;
import com.gsmart.resources.IDataSource;
import com.gsmart.resources.TaskStatus;
import com.gsmart.sources.ThingsBoardSource;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Classe utilitária estática que atua como o motor de regras de negócio.
 *
 * A sua responsabilidade é analisar um conjunto de métricas de um ciclo de processamento
 * e gerar "insights" ou alertas humanamente legíveis com base em limiares pré-definidos.
 * As regras avaliam custos, diagnósticos de manutenção e previsões de falhas,
 * comunicando os resultados através do {@code GSmartListener}.
 *
 * @see com.gsmart.resources.GSmartListener
 */
public class DataPipeline {
    public static final Logger logger = LoggerFactory.getLogger(DataPipeline.class);
    private static final Logger reconnectionLogger = LoggerFactory.getLogger("ReconnectionLogger");

    private final IDataSource dataSource;
    private final String powerBiPushUrl;
    private final List<MetricConfig> metricConfigs;
    private final GSmartListener listener;
    private final List<AlertRule> alertRules;
    private final List<InsightRule> insightRules;
    private final String mqttBrokerUrl;
    private final String telegramToken;
    private final String telegramChatId;
    private final Map<String, Long> alertCooldowns;
    private final Map<String, Long> alarmCooldowns;
    private final OkHttpClient httpClient;
    private volatile boolean stopRequested = false;
    private final AtomicBoolean manualReconnectTrigger = new AtomicBoolean(false);

    /**
     * Construtor da DataPipeline.
     *
     * @param dataSource A fonte dos dados.
     * @param powerBiPushUrl A URL de destino do Power BI.
     * @param metricConfigs A configuração das métricas a serem processadas.
     * @param listener O canal de comunicação com a GUI.
     * @param alertRules A lista de regras de alerta a serem avaliadas.
     * @param insightRules A lista de regras de alarme a serem avaliadas.
     * @param telegramChatId O id do seu chat no telegram.
     * @param telegramToken O token do seu chatboot do telegram.
     */
    public DataPipeline(IDataSource dataSource, String powerBiPushUrl, List<MetricConfig> metricConfigs, GSmartListener listener, List<AlertRule> alertRules, List<InsightRule> insightRules, String telegramToken, String telegramChatId, String mqttBrokerUrl) {
        this.dataSource = dataSource;
        this.powerBiPushUrl = powerBiPushUrl;
        this.metricConfigs = metricConfigs;
        this.listener = listener;
        this.alertRules = alertRules;
        this.insightRules = insightRules;
        this.mqttBrokerUrl = mqttBrokerUrl;
        this.telegramToken = telegramToken;
        this.telegramChatId = telegramChatId;
        this.alertCooldowns = new HashMap<>();
        this.alarmCooldowns = new HashMap<>();
        this.httpClient = new OkHttpClient();
    }


    public void triggerManualReconnect() {
        logger.info("Sinal de reconexão manual recebido.");
        this.manualReconnectTrigger.set(true);
        Thread.currentThread().interrupt();
    }

    public void requestStop() {
        this.stopRequested = true;
    }

    public void run() {
        logger.info("🚀 INICIANDO PIPELINE COM FONTE: {} 🚀", dataSource.getSourceName());

        final long MAX_RETRY_DELAY = 300;
        final long RETRY_INCREMENT = 5;
        long currentRetryDelay = 0;

        while (!stopRequested) {
            try {
                if (currentRetryDelay > 0) {
                    logger.info("Conexão restaurada. Retomando operação normal.");
                    currentRetryDelay = 0;
                }

                ZonedDateTime horaAtualBrasil = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"));
                String mensagemAlertaPBI = "";
                String mensagemAlarmePBI = "";
                String timestampPrefix = horaAtualBrasil.format(DateTimeFormatter.ofPattern("'['dd/MM/yyyy HH:mm:ss']' "));
                logger.info("--- Iniciando novo ciclo de processamento às {} ---", horaAtualBrasil.toLocalTime());

                logger.info("[ETAPA 1/3] Buscando dados da fonte...");
                JsonObject telemetria = dataSource.fetchData();

                Map<String, Double> currentMetricValues = new HashMap<>();
                JsonObject pbiPayload = new JsonObject();

                for (MetricConfig config : this.metricConfigs) {
                    String originalName = config.getOriginalName();
                    String alias = config.getAlias();
                    if (telemetria.has(originalName)) {
                        try {
                            JsonElement valorFinal = telemetria.get(originalName).getAsJsonArray().get(0).getAsJsonObject().get("value");
                            if (valorFinal.isJsonPrimitive()) {
                                double valorNumerico;
                                try {
                                    valorNumerico = Double.parseDouble(valorFinal.getAsString());
                                } catch (NumberFormatException | UnsupportedOperationException ex) {
                                    pbiPayload.addProperty(alias, valorFinal.getAsString());
                                    continue;
                                }

                                currentMetricValues.put(originalName, valorNumerico);
                                String expressao = config.getExpression();
                                double valorParaEnviar = valorNumerico;

                                if (expressao != null && !expressao.trim().isEmpty()) {
                                    Expression e = new ExpressionBuilder(expressao).variable("valor").build().setVariable("valor", valorNumerico);
                                    valorParaEnviar = e.evaluate();
                                }
                                pbiPayload.addProperty(alias, valorParaEnviar);

                            } else {
                                pbiPayload.addProperty(alias, valorFinal.toString());
                            }
                        } catch (Exception e) {
                            logger.warn("Não foi possível processar a chave '{}'. Pulando.", originalName, e);
                        }
                    }
                }

                logger.debug("[MOTOR DE REGRAS] A iniciar avaliação de {} regras de alerta.", alertRules != null ? alertRules.size() : 0);
                boolean alertaCriticoDisparado = false;
                if (alertRules != null && !alertRules.isEmpty()) {
                    long currentTime = System.currentTimeMillis();

                    for (AlertRule rule : alertRules) {
                        logger.debug("--- Avaliando regra: '{}'", rule.getRuleName());
                        if (!rule.isEnabled()) {
                            logger.debug("   - Regra desativada. A pular.");
                            continue;
                        }

                        String metricToWatch = rule.getMetricToWatch();
                        if (currentMetricValues.containsKey(metricToWatch)) {
                            double valorAtual = currentMetricValues.get(metricToWatch);
                            double valorLimiar = rule.getThresholdValue();
                            boolean condicaoSatisfeita = false;

                            logger.debug("   - A verificar métrica: '{}' | Valor Atual: {} | Condição: {} | Limiar: {}",
                                    metricToWatch, valorAtual, rule.getCondition(), valorLimiar);

                            switch (rule.getCondition()) {
                                case GREATER_THAN: condicaoSatisfeita = valorAtual > valorLimiar; break;
                                case LESS_THAN: condicaoSatisfeita = valorAtual < valorLimiar; break;
                                case EQUALS: condicaoSatisfeita = valorAtual == valorLimiar; break;
                                case BETWEEN: condicaoSatisfeita = valorAtual > rule.getThresholdValue() && valorAtual < rule.getThresholdValueMax(); break;
                            }

                            logger.debug("   - Resultado da condição: {}", condicaoSatisfeita);

                            if (condicaoSatisfeita) {
                                long lastSentTime = alertCooldowns.getOrDefault(rule.getId(), 0L);
                                long cooldownMillis = rule.getCooldownSeconds() * 1000L;

                                if ((currentTime - lastSentTime) > cooldownMillis) {
                                    logger.info("   - CONDIÇÃO SATISFEITA! Cooldown permite o envio. Disparando alerta.");
                                    alertaCriticoDisparado = true;

                                    String mensagemComTimestamp = timestampPrefix + rule.getMessageToSend();

                                    if (rule.isSendToMqtt()) {
                                        publicarAlertaMqtt(mensagemComTimestamp);
                                    }
                                    if (rule.isSendToTelegram()) {
                                        com.gsmart.services.TelegramService.enviarMensagem(this.telegramToken, this.telegramChatId, mensagemComTimestamp);
                                    }
                                    mensagemAlertaPBI = mensagemComTimestamp;

                                    alertCooldowns.put(rule.getId(), currentTime);
                                } else {
                                    logger.info("   - CONDIÇÃO SATISFEITA! Mas o alerta para a regra '{}' está em cooldown. O envio foi ignorado.", rule.getRuleName());
                                }
                            }
                        } else {
                            logger.warn("   - A métrica '{}' para a regra '{}' não foi encontrada nos dados atuais.", metricToWatch, rule.getRuleName());
                        }
                    }
                }

                logger.debug("[MOTOR DE ALARMES] A iniciar avaliação de {} regras de alarme.", insightRules != null ? insightRules.size() : 0);
                if (insightRules != null && !insightRules.isEmpty()) {
                    long currentTime = System.currentTimeMillis(); // Obtenha o tempo atual uma vez

                    for (InsightRule rule : insightRules) {
                        if (!rule.isEnabled()) continue;

                        if (currentMetricValues.containsKey(rule.getMetricToWatch())) {
                            double valorAtual = currentMetricValues.get(rule.getMetricToWatch());
                            boolean condicaoSatisfeita = false;
                            switch (rule.getCondition()) {
                                case GREATER_THAN: condicaoSatisfeita = valorAtual > rule.getThresholdValue(); break;
                                case LESS_THAN: condicaoSatisfeita = valorAtual < rule.getThresholdValue(); break;
                                case EQUALS: condicaoSatisfeita = valorAtual == rule.getThresholdValue(); break;
                                case BETWEEN: condicaoSatisfeita = valorAtual > rule.getThresholdValue() && valorAtual < rule.getThresholdValueMax(); break;
                            }

                            if (condicaoSatisfeita) {
                                long lastSentTime = alarmCooldowns.getOrDefault(rule.getId(), 0L);
                                long cooldownMillis = rule.getCooldownSeconds() * 1000L;

                                if ((currentTime - lastSentTime) > cooldownMillis) {
                                    logger.info("   - ALARME GERADO! '{}'", rule.getRuleName());

                                    String mensagemComTimestamp = timestampPrefix + rule.getMessageToSend();
                                    // Envia para a GUI (sempre)
                                    if (listener != null) {
                                        listener.onInsight(mensagemComTimestamp, rule.getInsightType());
                                    }
                                    publicarAlarmeMqtt(mensagemComTimestamp, rule.getInsightType());
                                    if (rule.isSendToTelegram()) {
                                        com.gsmart.services.TelegramService.enviarMensagem(this.telegramToken, this.telegramChatId, mensagemComTimestamp);
                                    }
                                    mensagemAlarmePBI = mensagemComTimestamp;
                                    // Atualiza o timestamp do último envio para esta regra de alarme.
                                    alarmCooldowns.put(rule.getId(), currentTime);
                                } else {
                                    logger.info("   - CONDIÇÃO DE ALARME SATISFEITA! Mas a regra '{}' está em cooldown. O envio foi ignorado.", rule.getRuleName());
                                }
                            }
                        }
                    }
                }


                pbiPayload.addProperty("AlertaCritico", alertaCriticoDisparado ? 1 : 0);
                pbiPayload.addProperty("timestamp", Instant.now().minus(3, ChronoUnit.HOURS).toString());
                pbiPayload.addProperty("UltimoAlerta", mensagemAlertaPBI);
                pbiPayload.addProperty("UltimoAlarme", mensagemAlarmePBI);
                pbiPayload.addProperty("HoraDev", horaAtualBrasil.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                pbiPayload.addProperty("DataDev", horaAtualBrasil.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                pbiPayload.addProperty("OrigemDados", dataSource.getSourceName());

                ExportacaoDadosPWBI.sendDataToPowerBI(pbiPayload, this.powerBiPushUrl);
                Thread.sleep(5000);

            } catch (InterruptedException e) {
                logger.warn("Sinal de interrupção recebido. Encerrando a pipeline...");
                this.stopRequested = true;
            } catch (Exception e) {
                String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                logger.error("Falha na pipeline: {}", errorMessage, e);
                if (dataSource instanceof ThingsBoardSource) {
                    ((ThingsBoardSource) dataSource).clearAuthToken();
                }
                if (listener != null) {
                    listener.onConnectionLost(errorMessage);
                    reconnectionLogger.info("CONEXÃO PERDIDA - Pipeline: {}", dataSource.getSourceName());
                }

                while (!stopRequested) {
                    try {
                        if (manualReconnectTrigger.compareAndSet(true, false)) {
                            logger.info("Iniciando tentativa de reconexão manual imediata...");
                            currentRetryDelay = 0;
                        } else {
                            currentRetryDelay += RETRY_INCREMENT;
                            if (currentRetryDelay > MAX_RETRY_DELAY) currentRetryDelay = MAX_RETRY_DELAY;
                            if (listener != null) listener.onReconnectionAttempt(currentRetryDelay);
                            Thread.sleep(currentRetryDelay * 1000);
                        }
                        logger.info("Tentando reconectar...");
                        dataSource.fetchData();
                        if (listener != null) listener.onConnectionRestored();
                        reconnectionLogger.info("CONEXÃO RESTABELECIDA - Pipeline: {}", dataSource.getSourceName());
                        break;
                    } catch (InterruptedException ie) {
                        logger.warn("Sinal de interrupção recebido durante a reconexão. Encerrando a pipeline...");
                        this.stopRequested = true;
                    } catch (Exception retryEx) {
                        logger.error("Tentativa de reconexão falhou: {}", retryEx.getMessage());
                        reconnectionLogger.info("TENTATIVA DE RECONEXÃO FALHOU - Pipeline: {}", dataSource.getSourceName());
                    }
                }
            }
        }

        logger.info("FIM DO LOOP. Execução da pipeline para {} finalizada.", dataSource.getSourceName());
        if (listener != null) {
            listener.onStatusUpdate(TaskStatus.FINISHED);
        }
    }
    /**
     * Publica uma mensagem de alerta crítico no tópico MQTT 'gsmart/alerta'.
     * Utiliza o {@link com.gsmart.services.MqttService} para a comunicação direta com o broker.
     * @param mensagem O conteúdo da mensagem de alerta a ser publicada.
     */
    private void publicarAlertaMqtt(String mensagem) {
        com.gsmart.services.MqttService.publish(this.mqttBrokerUrl, "gsmart/alerta", mensagem);
    }

    /**
     * Publica uma mensagem de alarme (insight) num subtópico MQTT dinâmico.
     * O tópico é formatado como 'gsmart/alarme/{tipo}', permitindo uma filtragem fácil
     * por parte dos clientes MQTT.
     * @param mensagem O conteúdo do alarme a ser publicado.
     * @param tipo A categoria do alarme (ex: "CUSTO", "MANUTENCAO"), que definirá o subtópico.
     */
    private void publicarAlarmeMqtt(String mensagem, String tipo) {
        // Publicamos o alarme num subtópico para melhor organização
        String topic = "gsmart/alarme/" + tipo.toLowerCase();
        com.gsmart.services.MqttService.publish(this.mqttBrokerUrl, topic, mensagem);
    }


}
