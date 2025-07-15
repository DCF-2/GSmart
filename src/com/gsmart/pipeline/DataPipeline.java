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
     */
    public DataPipeline(IDataSource dataSource, String powerBiPushUrl, List<MetricConfig> metricConfigs, GSmartListener listener, List<AlertRule> alertRules, List<InsightRule> insightRules) {
        this.dataSource = dataSource;
        this.powerBiPushUrl = powerBiPushUrl;
        this.metricConfigs = metricConfigs;
        this.listener = listener;
        this.alertRules = alertRules;
        this.insightRules = insightRules; // Adicione a atribuição para as novas regras
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
                logger.info("--- Iniciando novo ciclo de processamento em {} ---", horaAtualBrasil.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

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

                logger.debug("[MOTOR DE REGRAS] A iniciar avaliação de {} regras.", alertRules != null ? alertRules.size() : 0);
                boolean alertaCriticoDisparado = false;
                if (alertRules != null && !alertRules.isEmpty()) {
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
                            }

                            logger.debug("   - Resultado da condição: {}", condicaoSatisfeita);

                            if (condicaoSatisfeita) {
                                logger.info("   - CONDIÇÃO SATISFEITA! A disparar alerta.");
                                enviarAlertaParaNodeRED(rule.getMessageToSend());
                                alertaCriticoDisparado = true;
                            }
                        } else {
                            logger.warn("   - A métrica '{}' para a regra '{}' não foi encontrada nos dados atuais.", metricToWatch, rule.getRuleName());
                        }
                    }
                }
                logger.debug("[MOTOR DE INSIGHTS] A iniciar avaliação de {} regras de insight.", insightRules != null ? insightRules.size() : 0);
                if (insightRules != null && !insightRules.isEmpty()) {
                    for (InsightRule rule : insightRules) {
                        if (!rule.isEnabled()) continue;

                        if (currentMetricValues.containsKey(rule.getMetricToWatch())) {
                            double valorAtual = currentMetricValues.get(rule.getMetricToWatch());
                            boolean condicaoSatisfeita = false;
                            switch (rule.getCondition()) {
                                case GREATER_THAN: condicaoSatisfeita = valorAtual > rule.getThresholdValue(); break;
                                case LESS_THAN: condicaoSatisfeita = valorAtual < rule.getThresholdValue(); break;
                                case EQUALS: condicaoSatisfeita = valorAtual == rule.getThresholdValue(); break;
                            }

                            if (condicaoSatisfeita) {
                                logger.info("   - INSIGHT GERADO! '{}'", rule.getRuleName());
                                // Envia para a GUI
                                if (listener != null) {
                                    listener.onInsight(rule.getMessageToSend(), rule.getInsightType());
                                }
                                // Envia para o MQTT
                                enviarInsightParaNodeRED(rule.getMessageToSend(), rule.getInsightType());
                            }
                        }
                    }
                }


                pbiPayload.addProperty("AlertaCritico", alertaCriticoDisparado ? 1 : 0);
                pbiPayload.addProperty("timestamp", Instant.now().minus(3, ChronoUnit.HOURS).toString());
                pbiPayload.addProperty("HdDev", horaAtualBrasil.format(DateTimeFormatter.ofPattern("HH:mm:ss - dd/MM/yyyy")));
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


    private void enviarAlertaParaNodeRED(String mensagem) {
        try {
            logger.info("Enviando alerta para o Node-RED: {}", mensagem);

            JsonObject jsonPayload = new JsonObject();
            jsonPayload.addProperty("alerta_msg", mensagem);

            RequestBody body = RequestBody.create(jsonPayload.toString(), MediaType.get("application/json; charset=utf-8"));

            Request request = new Request.Builder()
                    .url("http://10.5.0.11:1880/alerta") // Restaurado para http
                    .post(body)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    logger.warn("Falha ao enviar alerta para o Node-RED.", e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        logger.warn("Falha ao enviar alerta para o Node-RED. Código: {}", response.code());
                    } else {
                        logger.info("Alerta enviado com sucesso para o Node-RED.");
                    }
                    response.close();
                }
            });
        } catch (Exception e) {
            logger.error("Erro ao tentar construir o pedido de alerta para o Node-RED.", e);
        }
    }
    /**
     * Envia uma mensagem de alarme para o endpoint /insight do Node-RED.
     *
     * Este método formata um payload JSON contendo a mensagem e o tipo do alarme,
     * e realiza uma requisição POST para um tópico MQTT diferente dos alertas críticos.
     *
     * @param mensagem O texto do alarme a ser enviado.
     * @param tipo A categoria do alarme (ex: "CUSTO", "EFICIÊNCIA").
     */
    private void enviarInsightParaNodeRED(String mensagem, String tipo) {
        try {
            logger.info("Enviando insight para o Node-RED: [Tipo: {}] {}", tipo, mensagem);

            JsonObject jsonPayload = new JsonObject();
            jsonPayload.addProperty("insight_msg", mensagem);
            jsonPayload.addProperty("insight_type", tipo);

            RequestBody body = RequestBody.create(jsonPayload.toString(), MediaType.get("application/json; charset=utf-8"));

            // Usando um tópico MQTT diferente para insights
            Request request = new Request.Builder()
                    .url("http://10.5.0.11:1880/insight") // Novo endpoint: /insight
                    .post(body)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    logger.warn("Falha ao enviar insight para o Node-RED.", e);
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        logger.info("Insight enviado com sucesso para o Node-RED.");
                    } else {
                        logger.warn("Falha ao enviar insight para o Node-RED. Código: {}", response.code());
                    }
                    response.close();
                }
            });
        } catch (Exception e) {
            logger.error("Erro ao tentar construir o pedido de insight para o Node-RED.", e);
        }
    }
}
