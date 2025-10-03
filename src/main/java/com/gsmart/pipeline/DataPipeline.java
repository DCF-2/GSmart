// Localização: src/com/gsmart/pipeline/DataPipeline.java
package main.java.com.gsmart.pipeline;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import main.java.com.gsmart.config.AlertRule;
import main.java.com.gsmart.config.InsightRule;
import main.java.com.gsmart.config.MetricConfig;
import main.java.com.gsmart.conection.ExportacaoDadosPWBI;
import main.java.com.gsmart.conection.ExportacaoDadosFabric;
import main.java.com.gsmart.resources.DestinationType;
import main.java.com.gsmart.resources.GSmartListener;
import main.java.com.gsmart.resources.IDataSource;
import main.java.com.gsmart.resources.TaskStatus;
import main.java.com.gsmart.services.CsvExportService;
import main.java.com.gsmart.services.MqttService;
import main.java.com.gsmart.services.TelegramService;
import main.java.com.gsmart.sources.DatabaseSource;
import main.java.com.gsmart.sources.ThingsBoardSource;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Representa o motor de uma única pipeline de processamento de dados.
 *
 * (O Javadoc permanece o mesmo)
 */
public class DataPipeline {
    public static final Logger logger = LoggerFactory.getLogger(DataPipeline.class);
    private static final Logger reconnectionLogger = LoggerFactory.getLogger("ReconnectionLogger");

    // ... (As variáveis de instância permanecem as mesmas)
    private final IDataSource dataSource;
    private final DestinationType destinationType;
    private final String destinationEndpoint;
    private final List<MetricConfig> metricConfigs;
    private final GSmartListener listener;
    private final List<AlertRule> alertRules;
    private final List<InsightRule> insightRules;
    private final String mqttBrokerUrl;
    private final String telegramToken;
    private final String telegramChatId;
    private final CsvExportService csvExportService;
    private final List<Map<String, Object>> telemetryBuffer;
    private String lastTriggeredAlertId = null;
    private String lastTriggeredAlarmId = null;
    private final OkHttpClient httpClient;
    private volatile boolean stopRequested = false;
    private final AtomicBoolean manualReconnectTrigger = new AtomicBoolean(false);


    public DataPipeline(IDataSource dataSource,DestinationType destinationType, String destinationEndpoint,List<MetricConfig> metricConfigs, GSmartListener listener, List<AlertRule> alertRules, List<InsightRule> insightRules, String telegramToken, String telegramChatId, String mqttBrokerUrl) {
        this.dataSource = dataSource;
        this.destinationType = destinationType;
        this.destinationEndpoint = destinationEndpoint;
        this.metricConfigs = metricConfigs;
        this.listener = listener;
        this.alertRules = alertRules;
        this.insightRules = insightRules;
        this.mqttBrokerUrl = mqttBrokerUrl;
        this.telegramToken = telegramToken;
        this.telegramChatId = telegramChatId;
        this.csvExportService = new CsvExportService();
        this.telemetryBuffer = new ArrayList<>();
        this.httpClient = new OkHttpClient();
    }

    // ... (Os métodos triggerManualReconnect e requestStop permanecem os mesmos)
    public void triggerManualReconnect() {
        logger.info("Sinal de reconexão manual recebido.");
        this.manualReconnectTrigger.set(true);
        Thread.currentThread().interrupt();
    }

    public void requestStop() {
        this.stopRequested = true;
    }


    /**
     * Inicia e executa o ciclo de processamento contínuo da pipeline.
     */
    public void run() {
        // --- ✨ LOG INICIAL MELHORADO ✨ ---
        logger.info("======================================================================");
        logger.info("🚀 NOVA PIPELINE INICIADA");
        logger.info("   ├─ Fonte de Dados: {}", getDataSourceDetails());
        logger.info("   └─ Destino dos Dados: {}", getDestinationDetails());
        logger.info("======================================================================");

        final long MAX_RETRY_DELAY = 300;
        final long RETRY_INCREMENT = 5;
        long currentRetryDelay = 0;

        while (!stopRequested) {
            try {
                if (currentRetryDelay > 0) {
                    logger.info("✅ Conexão restabelecida. Retomando operação normal.");
                    currentRetryDelay = 0;
                }

                logger.info("🔄 --- Novo ciclo de processamento iniciado ---");

                // [ETAPA 1] BUSCA DE DADOS
                logger.info("[1/4] 📥 Buscando dados da fonte: '{}'", dataSource.getSourceName());
                JsonObject telemetria = dataSource.fetchData();
                if (telemetria.keySet().isEmpty()) {
                    logger.warn("⚠️ A fonte de dados não retornou nenhuma métrica. O ciclo será reiniciado em 5 segundos.");
                    Thread.sleep(5000);
                    continue;
                }

                // [ETAPA 2] PROCESSAMENTO E TRANSFORMAÇÃO
                logger.info("[2/4] ⚙️  Processando e transformando {} métricas...", metricConfigs.stream().filter(m -> !m.isSystemMetric()).count());
                Map<String, Double> currentMetricValues = new HashMap<>();
                JsonObject pbiPayload = new JsonObject();
                processMetrics(telemetria, currentMetricValues, pbiPayload);

                // [ETAPA 3] AVALIAÇÃO DE REGRAS
                logger.info("[3/4] ⚖️  Avaliando {} regras de alerta e {} de alarme...", alertRules.size(), insightRules.size());
                String mensagemAlertaPBI = evaluateAlertRules(currentMetricValues);
                String mensagemAlarmePBI = evaluateInsightRules(currentMetricValues);

                addToCsvBuffer(pbiPayload);
                finalizePayload(pbiPayload, !mensagemAlertaPBI.isEmpty(), mensagemAlertaPBI, mensagemAlarmePBI);

                // [ETAPA 4] ENVIO DOS DADOS
                logger.info("[4/4] 📤 Enviando dados para o destino: {}", destinationType);
                if (this.destinationType == DestinationType.POWER_BI) {
                    ExportacaoDadosPWBI.sendDataToPowerBI(pbiPayload, this.destinationEndpoint);
                } else if (this.destinationType == DestinationType.FABRIC) {
                    ExportacaoDadosFabric.sendDataToFabric(pbiPayload, this.destinationEndpoint);
                }
                logger.info("✅ Ciclo concluído com sucesso.");
                Thread.sleep(5000);

            } catch (InterruptedException e) {
                logger.warn("Pipeline interrompida. Encerrando...");
                this.stopRequested = true;
            } catch (Exception e) {
                String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                logger.error("❌ Falha crítica na pipeline: {}", errorMessage, e);

                if (dataSource instanceof ThingsBoardSource) {
                    ((ThingsBoardSource) dataSource).clearAuthToken();
                }

                if (listener != null) {
                    listener.onConnectionLost(errorMessage);
                    reconnectionLogger.info("CONEXÃO PERDIDA - Pipeline: {}", dataSource.getSourceName());
                }

                handleReconnection(MAX_RETRY_DELAY, RETRY_INCREMENT);
            }
        }

        exportRemainingData();
        logger.info("🛑 FIM DO LOOP. Pipeline para '{}' finalizada.", dataSource.getSourceName());
        if (listener != null) {
            listener.onStatusUpdate(TaskStatus.FINISHED);
        }
    }

    // --- ✨ NOVOS MÉTODOS PARA DETALHES NO LOG ✨ ---

    /**
     * Gera uma string detalhada sobre a fonte de dados para o log.
     */
    private String getDataSourceDetails() {
        if (dataSource instanceof ThingsBoardSource tbSource) {
            return String.format("ThingsBoard (Servidor: %s, Dispositivo: %s)", tbSource.getThingsboardUrl(), tbSource.getDeviceName());
        }
        if (dataSource instanceof DatabaseSource dbSource) {
            return String.format("Banco de Dados (URL: %s, Tabela: %s)", dbSource.getDbUrl(), dbSource.getTableName());
        }
        return "Desconhecida";
    }

    /**
     * Gera uma string detalhada sobre o destino dos dados para o log.
     */
    private String getDestinationDetails() {
        if (destinationType == DestinationType.POWER_BI) {
            return String.format("Power BI (URL: %s)", destinationEndpoint);
        }
        if (destinationType == DestinationType.FABRIC) {
            // Não expomos a connection string completa no log por segurança
            return "Microsoft Fabric Eventstream";
        }
        return "Desconhecido";
    }

    // --- O RESTANTE DA CLASSE (MÉTODOS PRIVADOS) PERMANECE O MESMO ---
    private void processMetrics(JsonObject telemetria, Map<String, Double> currentMetricValues, JsonObject pbiPayload) {
        for (MetricConfig config : this.metricConfigs) {
            String originalName = config.getOriginalName();
            if (config.isSystemMetric() || !telemetria.has(originalName)) continue;

            try {
                JsonElement valorElement = telemetria.getAsJsonArray(originalName).get(0).getAsJsonObject().get("value");
                if (valorElement.isJsonPrimitive()) {
                    double valorNumerico;
                    try {
                        valorNumerico = valorElement.getAsDouble();
                    } catch (NumberFormatException ex) {
                        pbiPayload.addProperty(config.getAlias(), valorElement.getAsString());
                        continue;
                    }

                    currentMetricValues.put(originalName, valorNumerico);
                    double valorParaEnviar = valorNumerico;

                    if (config.getExpression() != null && !config.getExpression().trim().isEmpty()) {
                        Expression e = new ExpressionBuilder(config.getExpression()).variable("valor").build().setVariable("valor", valorNumerico);
                        valorParaEnviar = e.evaluate();
                    }
                    pbiPayload.addProperty(config.getAlias(), valorParaEnviar);
                } else {
                    pbiPayload.addProperty(config.getAlias(), valorElement.toString());
                }
            } catch (Exception e) {
                logger.warn("⚠️ Não foi possível processar a métrica '{}'. Causa: {}", originalName, e.getMessage());
            }
        }
    }

    private String evaluateAlertRules(Map<String, Double> currentMetricValues) {
        String triggeredMessage = "";
        for (AlertRule rule : alertRules) {
            if (!rule.isEnabled() || !currentMetricValues.containsKey(rule.getMetricToWatch())) continue;

            double valorAtual = currentMetricValues.get(rule.getMetricToWatch());
            boolean condicaoSatisfeita = checkCondition(valorAtual, rule.getCondition(), rule.getThresholdValue(), rule.getThresholdValueMax());

            if (condicaoSatisfeita) {
                if (!rule.getId().equals(this.lastTriggeredAlertId)) {
                    logger.warn("🚨 ALERTA CRÍTICO DISPARADO! Regra: '{}'", rule.getRuleName());
                    this.lastTriggeredAlertId = rule.getId();
                    String mensagem = formatMessage(rule.getMessageToSend());
                    triggeredMessage = mensagem;

                    if (listener != null) listener.onAlert(rule.getRuleName(), mensagem);
                    if (rule.isSendToMqtt()) publicarAlertaMqtt(mensagem);
                    if (rule.isSendToTelegram()) TelegramService.enviarMensagem(this.telegramToken, this.telegramChatId, mensagem);
                } else {
                    logger.debug("   - Alerta '{}' já ativo. Nenhuma nova notificação.", rule.getRuleName());
                }
            }
        }
        // Se nenhuma regra foi satisfeita, limpa o último alerta.
        if (triggeredMessage.isEmpty()) {
            this.lastTriggeredAlertId = null;
        }
        return triggeredMessage;
    }

    private String evaluateInsightRules(Map<String, Double> currentMetricValues) {
        String triggeredMessage = "";
        for (InsightRule rule : insightRules) {
            if (!rule.isEnabled() || !currentMetricValues.containsKey(rule.getMetricToWatch())) continue;

            double valorAtual = currentMetricValues.get(rule.getMetricToWatch());
            boolean condicaoSatisfeita = checkCondition(valorAtual, rule.getCondition(), rule.getThresholdValue(), rule.getThresholdValueMax());

            if (condicaoSatisfeita) {
                if (!rule.getId().equals(this.lastTriggeredAlarmId)) {
                    logger.info("💡 ALARME INTELIGENTE GERADO! Regra: '{}'", rule.getRuleName());
                    this.lastTriggeredAlarmId = rule.getId();
                    String mensagem = formatMessage(rule.getMessageToSend());
                    triggeredMessage = mensagem;

                    if (listener != null) listener.onInsight(mensagem, rule.getInsightType());
                    if (rule.isSendToMqtt()) publicarAlarmeMqtt(mensagem, rule.getInsightType());
                    if (rule.isSendToTelegram()) TelegramService.enviarMensagem(this.telegramToken, this.telegramChatId, mensagem);
                } else {
                    logger.debug("   - Alarme '{}' já ativo. Nenhuma nova notificação.", rule.getRuleName());
                }
            }
        }
        if (triggeredMessage.isEmpty()) {
            this.lastTriggeredAlarmId = null;
        }
        return triggeredMessage;
    }

    private boolean checkCondition(double value, main.java.com.gsmart.resources.ConditionType condition, double threshold, double thresholdMax) {
        return switch (condition) {
            case GREATER_THAN -> value > threshold;
            case LESS_THAN -> value < threshold;
            case EQUALS -> value == threshold;
            case BETWEEN -> {
                double min = Math.min(threshold, thresholdMax);
                double max = Math.max(threshold, thresholdMax);
                yield value >= min && value <= max;
            }
        };
    }

    private void addToCsvBuffer(JsonObject pbiPayload) {
        Map<String, Object> dataRow = new LinkedHashMap<>();
        for (String key : pbiPayload.keySet()) {
            JsonElement element = pbiPayload.get(key);
            if (element.isJsonPrimitive()) {
                if (element.getAsJsonPrimitive().isNumber()) dataRow.put(key, element.getAsNumber());
                else if (element.getAsJsonPrimitive().isBoolean()) dataRow.put(key, element.getAsBoolean());
                else dataRow.put(key, element.getAsString());
            }
        }
        telemetryBuffer.add(dataRow);
    }

    private void finalizePayload(JsonObject payload, boolean hasAlert, String alertMessage, String insightMessage) {
        ZonedDateTime horaAtualBrasil = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"));
        payload.addProperty("AlertaCritico", hasAlert ? 1 : 0);
        payload.addProperty("timestamp", Instant.now().minus(3, ChronoUnit.HOURS).toString());
        payload.addProperty("UltimoAlerta", alertMessage);
        payload.addProperty("UltimoAlarme", insightMessage);
        payload.addProperty("HoraDev", horaAtualBrasil.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        payload.addProperty("DataDev", horaAtualBrasil.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        payload.addProperty("OrigemDados", dataSource.getSourceName());
    }

    private void handleReconnection(long maxDelay, long increment) {
        long currentDelay = 0;
        while (!stopRequested) {
            try {
                if (manualReconnectTrigger.compareAndSet(true, false)) {
                    logger.info("Iniciando tentativa de reconexão manual imediata...");
                    currentDelay = 0;
                } else {
                    currentDelay = Math.min(currentDelay + increment, maxDelay);
                    logger.info("Tentando reconectar em {} segundos...", currentDelay);
                    if (listener != null) listener.onReconnectionAttempt(currentDelay);
                    Thread.sleep(currentDelay * 1000);
                }

                logger.info("📡 Tentando reconectar à fonte de dados...");
                dataSource.fetchData(); // Se isto não lançar exceção, a conexão foi restabelecida.
                if (listener != null) listener.onConnectionRestored();
                reconnectionLogger.info("CONEXÃO RESTABELECIDA - Pipeline: {}", dataSource.getSourceName());
                break; // Sai do loop de reconexão
            } catch (InterruptedException ie) {
                logger.warn("Pipeline interrompida durante a tentativa de reconexão.");
                this.stopRequested = true;
            } catch (Exception retryEx) {
                logger.error("❌ Tentativa de reconexão falhou: {}", retryEx.getMessage());
                reconnectionLogger.info("TENTATIVA DE RECONEXÃO FALHOU - Pipeline: {}", dataSource.getSourceName());
            }
        }
    }

    private String formatMessage(String message) {
        ZonedDateTime horaAtualBrasil = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"));
        String timestampPrefix = horaAtualBrasil.format(DateTimeFormatter.ofPattern("'['dd/MM/yyyy HH:mm:ss']' "));
        return timestampPrefix + message;
    }

    private void publicarAlertaMqtt(String mensagem) {
        MqttService.publish(this.mqttBrokerUrl, "gsmart/alerta", mensagem);
    }

    private void publicarAlarmeMqtt(String mensagem, String tipo) {
        String topic = "gsmart/alarme/" + tipo.toLowerCase();
        MqttService.publish(this.mqttBrokerUrl, topic, mensagem);
    }

    public void exportRemainingData() {
        if (!telemetryBuffer.isEmpty()) {
            logger.info("💾 Exportando {} registos de telemetria restantes para CSV...", telemetryBuffer.size());
            csvExportService.exportData(new ArrayList<>(telemetryBuffer));
            telemetryBuffer.clear();
        }
    }
}