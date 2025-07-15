package com.gsmart.config;

import com.gsmart.resources.ConditionType;

import java.io.Serializable;
import java.util.UUID;

/**
 * Representa uma única regra de alerta configurável pelo utilizador.
 * Contém todos os parâmetros necessários para avaliar uma condição e
 * disparar uma notificação.
 */
public class AlertRule implements Serializable {

    private final String id;
    private String ruleName;
    private String metricToWatch;
    private ConditionType condition;
    private double thresholdValue;
    private String messageToSend;
    private boolean enabled;

    public AlertRule(String ruleName, String metricToWatch, ConditionType condition, double thresholdValue, String messageToSend) {
        this.id = UUID.randomUUID().toString();
        this.ruleName = ruleName;
        this.metricToWatch = metricToWatch;
        this.condition = condition;
        this.thresholdValue = thresholdValue;
        this.messageToSend = messageToSend;
        this.enabled = true; // As regras são ativadas por padrão ao serem criadas
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
}