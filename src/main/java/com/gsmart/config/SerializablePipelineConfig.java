// Localização: src/com/gsmart/config/SerializablePipelineConfig.java
package main.java.com.gsmart.config;

import main.java.com.gsmart.resources.DestinationType;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Uma representação "salvável" da configuração de uma pipeline.
 * Esta classe armazena os parâmetros necessários para reconstruir uma IDataSource
 * e uma PipelineConfiguration completa, uma vez que a IDataSource em si não pode ser serializada.
 */
public class SerializablePipelineConfig implements Serializable {
    private static final long serialVersionUID = 2L; // Versão atualizada

    // --- Campos de Destino Atualizados ---
    private final DestinationType destinationType;
    private final String destinationEndpoint;

    // Parâmetros gerais
    private final String mqttBrokerUrl;
    private final String telegramToken;
    private final String telegramChatId;
    private final List<MetricConfig> metricConfigs;
    private final List<AlertRule> alertRules;
    private final List<InsightRule> insightRules;

    // Parâmetros para reconstruir a fonte de dados
    private final String dataSourceType;
    private final Map<String, String> dataSourceParams;

    // --- Construtor Atualizado com 10 argumentos ---
    public SerializablePipelineConfig(DestinationType destinationType, String destinationEndpoint, String mqttBrokerUrl, String telegramToken, String telegramChatId, List<MetricConfig> metricConfigs, List<AlertRule> alertRules, List<InsightRule> insightRules, String dataSourceType, Map<String, String> dataSourceParams) {
        this.destinationType = destinationType;
        this.destinationEndpoint = destinationEndpoint;
        this.mqttBrokerUrl = mqttBrokerUrl;
        this.telegramToken = telegramToken;
        this.telegramChatId = telegramChatId;
        this.metricConfigs = metricConfigs;
        this.alertRules = alertRules;
        this.insightRules = insightRules;
        this.dataSourceType = dataSourceType;
        this.dataSourceParams = dataSourceParams;
    }

    // --- Getters Atualizados ---
    public DestinationType getDestinationType() { return destinationType; }
    public String getDestinationEndpoint() { return destinationEndpoint; }
    public String getMqttBrokerUrl() { return mqttBrokerUrl; }
    public String getTelegramToken() { return telegramToken; }
    public String getTelegramChatId() { return telegramChatId; }
    public List<MetricConfig> getMetricConfigs() { return metricConfigs; }
    public List<AlertRule> getAlertRules() { return alertRules; }
    public List<InsightRule> getInsightRules() { return insightRules; }
    public String getDataSourceType() { return dataSourceType; }
    public Map<String, String> getDataSourceParams() { return dataSourceParams; }
}