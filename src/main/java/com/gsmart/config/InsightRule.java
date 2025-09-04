package main.java.com.gsmart.config;

import main.java.com.gsmart.resources.ConditionType;
import java.io.Serializable;
import java.util.UUID;

/**
 * Representa uma única regra configurável para a geração de "Alarmes" (anteriormente "Insights").
 *
 * Esta classe permite definir uma condição baseada numa métrica que, quando satisfeita,
 * gera uma mensagem inteligente e categorizada, fornecendo observações proativas
 * sobre o estado do sistema.
 */
public class InsightRule implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String id;
    private String ruleName;
    private String metricToWatch;
    private ConditionType condition;
    private double thresholdValue;
    private String messageToSend;
    private String insightType;
    private boolean enabled;
    private boolean sendToTelegram;
    private boolean sendToMqtt;
    private double thresholdValueMax;
    private String category;


    /**
     * Constrói uma nova regra de Alarme (Insight).
     *
     * @param ruleName O nome descritivo da regra (ex: "Alarme de Baixa Eficiência").
     * @param metricToWatch A chave da métrica a ser monitorizada (ex: "fatorPotencia").
     * @param condition A condição de avaliação (`MAIOR_QUE`, `MENOR_QUE`, `IGUAL_A`).
     * @param thresholdValue O valor limiar para a condição.
     * @param messageToSend O texto do alarme a ser gerado quando a condição é satisfeita.
     * @param insightType A categoria do alarme (ex: "EFICIÊNCIA", "CUSTO", "MANUTENÇÃO").
     */
    public InsightRule(String ruleName, String metricToWatch, ConditionType condition, double thresholdValue, String messageToSend, String insightType) {
        this.id = UUID.randomUUID().toString();
        this.ruleName = ruleName;
        this.metricToWatch = metricToWatch;
        this.condition = condition;
        this.thresholdValue = thresholdValue;
        this.messageToSend = messageToSend;
        this.insightType = insightType;
        this.enabled = true;
        this.sendToMqtt = true;
        this.sendToTelegram = true;
        this.thresholdValueMax = 0;
        this.category = "Geral";
    }

    /**
     * Construtor de cópia para duplicar uma regra de alarme.
     * <p>
     * Cria uma nova instância de {@code InsightRule} com os mesmos valores da regra
     * original, mas com um novo ID único.
     *
     * @param other A regra a ser copiada.
     */
    public InsightRule(InsightRule other) {
        this(other.ruleName, other.metricToWatch, other.condition, other.thresholdValue, other.messageToSend, other.insightType);
        this.setThresholdValueMax(other.getThresholdValueMax());
        this.setCategory(other.getCategory());
        this.setEnabled(other.isEnabled());
        this.setSendToMqtt(other.isSendToMqtt());
        this.setSendToTelegram(other.isSendToTelegram());
    }

    // --- Getters e Setters ---

    public String getId() { return id; }
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public String getMetricToWatch() { return metricToWatch; }
    public void setMetricToWatch(String metricToWatch) { this.metricToWatch = metricToWatch; }
    public ConditionType getCondition() { return condition; }
    public void setCondition(ConditionType condition) { this.condition = condition; }
    public double getThresholdValue() { return thresholdValue; }
    public void setThresholdValue(double thresholdValue) { this.thresholdValue = thresholdValue; }
    public String getMessageToSend() { return messageToSend; }
    public void setMessageToSend(String messageToSend) { this.messageToSend = messageToSend; }
    public String getInsightType() { return insightType; }
    public void setInsightType(String insightType) { this.insightType = insightType; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isSendToTelegram() {return sendToTelegram;}
    public void setSendToTelegram(boolean sendToTelegram) {this.sendToTelegram = sendToTelegram;}
    public boolean isSendToMqtt() {
        return sendToMqtt;
    }
    public void setSendToMqtt(boolean sendToMqtt) {this.sendToMqtt = sendToMqtt;}
    public double getThresholdValueMax() {return thresholdValueMax;}
    public void setThresholdValueMax(double thresholdValueMax) {this.thresholdValueMax = thresholdValueMax;}
    public String getCategory() {return category;}
    public void setCategory(String category) {this.category = category;}
}