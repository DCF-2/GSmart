// Localização: src/main/java/com/gsmart/pipeline/DataPipeline.java
package main.java.com.gsmart.pipeline;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import main.java.com.gsmart.config.AlertRule;
import main.java.com.gsmart.config.InsightRule;
import main.java.com.gsmart.config.MetricConfig;
import main.java.com.gsmart.conection.ExportacaoDadosPWBI;
import main.java.com.gsmart.conection.ExportacaoDadosFabric;
import main.java.com.gsmart.resources.*;
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
import java.util.stream.Collectors;

public class DataPipeline {
    public static final Logger logger = LoggerFactory.getLogger(DataPipeline.class);
    private static final Logger reconnectionLogger = LoggerFactory.getLogger("ReconnectionLogger");

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

    // Mapas para guardar o ID da regra ativa por métrica.
    private final Map<String, String> activeAlertRulePerMetric = new HashMap<>();
    private final Map<String, String> activeAlarmRulePerMetric = new HashMap<>();


    private final OkHttpClient httpClient;
    private volatile boolean stopRequested = false;
    private final AtomicBoolean manualReconnectTrigger = new AtomicBoolean(false);

    public DataPipeline(IDataSource dataSource, DestinationType destinationType, String destinationEndpoint, List<MetricConfig> metricConfigs, GSmartListener listener, List<AlertRule> alertRules, List<InsightRule> insightRules, String telegramToken, String telegramChatId, String mqttBrokerUrl) {
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

    public void triggerManualReconnect() {
        logger.info("Sinal de reconexão manual recebido.");
        this.manualReconnectTrigger.set(true);
        Thread.currentThread().interrupt();
    }

    public void requestStop() {
        this.stopRequested = true;
    }

    public void run() {
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
                    if(listener != null) listener.onConnectionRestored();
                    currentRetryDelay = 0;
                }

                logger.info("🔄 --- Novo ciclo de processamento iniciado ---");

                logger.info("[1/4] 📥 Buscando dados da fonte: '{}'", dataSource.getSourceName());
                JsonObject telemetria = dataSource.fetchData();
                if (telemetria.keySet().isEmpty()) {
                    logger.warn("⚠️ A fonte de dados não retornou nenhuma métrica. O ciclo será reiniciado em 5 segundos.");
                    Thread.sleep(5000);
                    continue;
                }

                logger.info("[2/4] ⚙️  Processando e transformando {} métricas...", metricConfigs.stream().filter(m -> !m.isSystemMetric()).count());
                Map<String, Double> currentMetricValues = new HashMap<>();
                JsonObject pbiPayload = new JsonObject();
                processMetrics(telemetria, currentMetricValues, pbiPayload);

                logger.info("[3/4] ⚖️  Avaliando {} regras de alerta e {} de alarme...", alertRules.size(), insightRules.size());
                String mensagemAlertaPBI = evaluateAlertRules(currentMetricValues);
                String mensagemAlarmePBI = evaluateInsightRules(currentMetricValues);

                addToCsvBuffer(pbiPayload);
                finalizePayload(pbiPayload, !mensagemAlertaPBI.isEmpty(), mensagemAlertaPBI, mensagemAlarmePBI);

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

    /**
     * Avalia as regras de ALERTA, selecionando a de maior prioridade e notificando
     * apenas numa MUDANÇA DE ESTADO (de uma regra para outra).
     */
    private String evaluateAlertRules(Map<String, Double> currentMetricValues) {
        StringBuilder payloadBuilder = new StringBuilder();

        Map<String, List<AlertRule>> rulesByMetric = alertRules.stream()
                .filter(rule -> rule.isEnabled() && rule.getMetricToWatch() != null)
                .collect(Collectors.groupingBy(AlertRule::getMetricToWatch));

        for (String metricName : rulesByMetric.keySet()) {
            if (!currentMetricValues.containsKey(metricName)) continue;

            double valorAtual = currentMetricValues.get(metricName);
            List<AlertRule> rulesForMetric = rulesByMetric.get(metricName);

            // Filtra as regras que são acionadas pelo valor atual
            List<AlertRule> triggeredRules = rulesForMetric.stream()
                    .filter(rule -> checkCondition(valorAtual, rule.getCondition(), rule.getThresholdValue(), rule.getThresholdValueMax()))
                    .collect(Collectors.toList());

            Optional<AlertRule> bestRuleOpt = Optional.empty();
            if (!triggeredRules.isEmpty()) {
                // CORREÇÃO: Determina a prioridade com base no tipo de condição
                ConditionType firstCondition = triggeredRules.get(0).getCondition();
                if (firstCondition == ConditionType.GREATER_THAN || firstCondition == ConditionType.EQUALS || firstCondition == ConditionType.BETWEEN) {
                    // Para 'maior que', a regra com o MAIOR limiar é a mais prioritária
                    bestRuleOpt = triggeredRules.stream().max(Comparator.comparingDouble(AlertRule::getThresholdValue));
                } else { // LESS_THAN
                    // Para 'menor que', a regra com o MENOR limiar é a mais prioritária
                    bestRuleOpt = triggeredRules.stream().min(Comparator.comparingDouble(AlertRule::getThresholdValue));
                }
            }

            String previouslyActiveRuleId = activeAlertRulePerMetric.get(metricName);
            String bestRuleId = bestRuleOpt.map(AlertRule::getId).orElse(null);

            // Notifica apenas se a regra de maior prioridade MUDOU
            if (bestRuleId != null && !bestRuleId.equals(previouslyActiveRuleId)) {
                AlertRule triggeredRule = bestRuleOpt.get();
                activeAlertRulePerMetric.put(metricName, bestRuleId); // Atualiza o estado para a nova regra

                String mensagem = formatMessage(
                        triggeredRule.getMessageToSend().replace("{{value}}", String.format("%.2f", valorAtual))
                );

                logger.warn("🚨 ALERTA ATIVADO/ALTERADO! Métrica: '{}', Nova Regra: '{}'", metricName, triggeredRule.getRuleName());

                if (listener != null) listener.onAlert(triggeredRule.getRuleName(), mensagem);
                if (triggeredRule.isSendToMqtt()) publicarAlertaMqtt(mensagem);
                if (triggeredRule.isSendToTelegram()) TelegramService.enviarMensagem(this.telegramToken, this.telegramChatId, mensagem);
            }

            // Para o PBI, sempre adiciona a mensagem da regra ativa no ciclo atual
            if (bestRuleOpt.isPresent()) {
                String mensagem = formatMessage(
                        bestRuleOpt.get().getMessageToSend().replace("{{value}}", String.format("%.2f", valorAtual))
                );
                if (payloadBuilder.length() > 0) payloadBuilder.append(" | ");
                payloadBuilder.append(mensagem);
            }
        }
        return payloadBuilder.toString();
    }

    /**
     * Avalia as regras de ALARME (Insight) com a mesma lógica de estado e prioridade.
     */
    private String evaluateInsightRules(Map<String, Double> currentMetricValues) {
        StringBuilder payloadBuilder = new StringBuilder();

        Map<String, List<InsightRule>> rulesByMetric = insightRules.stream()
                .filter(rule -> rule.isEnabled() && rule.getMetricToWatch() != null)
                .collect(Collectors.groupingBy(InsightRule::getMetricToWatch));

        for (String metricName : rulesByMetric.keySet()) {
            if (!currentMetricValues.containsKey(metricName)) continue;

            double valorAtual = currentMetricValues.get(metricName);
            List<InsightRule> rulesForMetric = rulesByMetric.get(metricName);

            List<InsightRule> triggeredRules = rulesForMetric.stream()
                    .filter(rule -> checkCondition(valorAtual, rule.getCondition(), rule.getThresholdValue(), rule.getThresholdValueMax()))
                    .collect(Collectors.toList());

            Optional<InsightRule> bestRuleOpt = Optional.empty();
            if (!triggeredRules.isEmpty()) {
                ConditionType firstCondition = triggeredRules.get(0).getCondition();
                if (firstCondition == ConditionType.GREATER_THAN || firstCondition == ConditionType.EQUALS || firstCondition == ConditionType.BETWEEN) {
                    bestRuleOpt = triggeredRules.stream().max(Comparator.comparingDouble(InsightRule::getThresholdValue));
                } else { // LESS_THAN
                    bestRuleOpt = triggeredRules.stream().min(Comparator.comparingDouble(InsightRule::getThresholdValue));
                }
            }

            String previouslyActiveRuleId = activeAlarmRulePerMetric.get(metricName);
            String bestRuleId = bestRuleOpt.map(InsightRule::getId).orElse(null);

            if (bestRuleId != null && !bestRuleId.equals(previouslyActiveRuleId)) {
                InsightRule triggeredRule = bestRuleOpt.get();
                activeAlarmRulePerMetric.put(metricName, bestRuleId);

                String mensagem = formatMessage(
                        triggeredRule.getMessageToSend().replace("{{value}}", String.format("%.2f", valorAtual))
                );

                logger.info("💡 ALARME ATIVADO/ALTERADO! Métrica: '{}', Nova Regra: '{}'", metricName, triggeredRule.getRuleName());

                if (listener != null) listener.onInsight(mensagem, triggeredRule.getInsightType());
                if (triggeredRule.isSendToMqtt()) publicarAlarmeMqtt(mensagem, triggeredRule.getInsightType());
                if (triggeredRule.isSendToTelegram()) TelegramService.enviarMensagem(this.telegramToken, this.telegramChatId, mensagem);
            }

            if (bestRuleOpt.isPresent()) {
                String mensagem = formatMessage(
                        bestRuleOpt.get().getMessageToSend().replace("{{value}}", String.format("%.2f", valorAtual))
                );
                if (payloadBuilder.length() > 0) payloadBuilder.append(" | ");
                payloadBuilder.append(mensagem);
            }
        }
        return payloadBuilder.toString();
    }


    private String getDataSourceDetails() {
        if (dataSource instanceof ThingsBoardSource tbSource) {
            return String.format("ThingsBoard (Servidor: %s, Dispositivo: %s)", tbSource.getThingsboardUrl(), tbSource.getDeviceName());
        }
        if (dataSource instanceof DatabaseSource dbSource) {
            return String.format("Banco de Dados (URL: %s, Tabela: %s)", dbSource.getDbUrl(), dbSource.getTableName());
        }
        return "Desconhecida";
    }

    private String getDestinationDetails() {
        if (destinationType == DestinationType.POWER_BI) {
            return String.format("Power BI (URL: %s)", destinationEndpoint);
        }
        if (destinationType == DestinationType.FABRIC) {
            return "Microsoft Fabric Eventstream";
        }
        return "Desconhecido";
    }

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
                dataSource.fetchData();
                if (listener != null) listener.onConnectionRestored();
                reconnectionLogger.info("CONEXÃO RESTABELECIDA - Pipeline: {}", dataSource.getSourceName());
                break;
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