package main.java.com.gsmart.config;

import main.java.com.gsmart.resources.ConditionType;

import java.io.Serializable;
import java.util.UUID;

/**
 * Representa uma única regra de alerta configurável pelo utilizador.
 * Contém todos os parâmetros necessários para avaliar uma condição e
 * disparar uma notificação.
 */
public class AlertRule implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String id;
    private String ruleName;
    private String metricToWatch;
    private ConditionType condition;
    private double thresholdValue;
    private String messageToSend;
    private boolean enabled;
    private boolean sendToMqtt;
    private boolean sendToTelegram;
    private double thresholdValueMax;
    private String category;

    /**
     * Constrói uma nova regra de alerta.
     *
     * @param ruleName O nome descritivo da regra para identificação na UI.
     * @param metricToWatch A métrica (nome original) que esta regra irá monitorizar.
     * @param condition O tipo de comparação a ser feita (ex: MAIOR_QUE, MENOR_QUE).
     * @param thresholdValue O valor limiar contra o qual a métrica será comparada.
     * @param messageToSend A mensagem de notificação a ser enviada quando a regra for despoletada.
     * @param sendToMqtt Se {@code true}, a notificação será enviada para o broker MQTT.
     * @param sendToTelegram Se {@code true}, a notificação será enviada para o Telegram.
     */
    public AlertRule(String ruleName, String metricToWatch, ConditionType condition, double thresholdValue, String messageToSend, boolean sendToMqtt, boolean sendToTelegram) {
        this.id = UUID.randomUUID().toString();
        this.ruleName = ruleName;
        this.metricToWatch = metricToWatch;
        this.condition = condition;
        this.thresholdValue = thresholdValue;
        this.messageToSend = messageToSend;
        this.enabled = true;
        this.sendToMqtt = sendToMqtt;
        this.sendToTelegram = sendToTelegram;
        this.thresholdValueMax = 0;
        this.category = "Geral";
    }

    /**
     * Construtor de cópia para duplicar uma regra de alerta.
     * <p>
     * Cria uma nova instância de {@code AlertRule} com os mesmos valores da regra
     * original, mas com um novo ID único.
     *
     * @param other A regra a ser copiada.
     */
    public AlertRule(AlertRule other) {
        this(other.ruleName, other.metricToWatch, other.condition, other.thresholdValue, other.messageToSend, other.sendToMqtt, other.sendToTelegram);
        this.setThresholdValueMax(other.getThresholdValueMax());
        this.setCategory(other.getCategory());
        this.setEnabled(other.isEnabled());
    }

    // Getters e Setters para todos os campos

    public String getId() {
        return id;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getMetricToWatch() {
        return metricToWatch;
    }

    public void setMetricToWatch(String metricToWatch) {
        this.metricToWatch = metricToWatch;
    }

    public ConditionType getCondition() {
        return condition;
    }

    public void setCondition(ConditionType condition) {
        this.condition = condition;
    }

    public double getThresholdValue() {
        return thresholdValue;
    }

    public void setThresholdValue(double thresholdValue) {
        this.thresholdValue = thresholdValue;
    }

    public String getMessageToSend() {
        return messageToSend;
    }

    public void setMessageToSend(String messageToSend) {
        this.messageToSend = messageToSend;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    public boolean isSendToMqtt() {
        return sendToMqtt;
    }

    public void setSendToMqtt(boolean sendToMqtt) {
        this.sendToMqtt = sendToMqtt;
    }

    public boolean isSendToTelegram() {
        return sendToTelegram;
    }

    public void setSendToTelegram(boolean sendToTelegram) {
        this.sendToTelegram = sendToTelegram;
    }

    public double getThresholdValueMax() {return thresholdValueMax;}

    public void setThresholdValueMax(double thresholdValueMax) {this.thresholdValueMax = thresholdValueMax;}

    public String getCategory() {return category;}

    public void setCategory(String category) {this.category = category;}
}