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
    private boolean sendToMqtt;
    private boolean sendToTelegram;
    private int cooldownSeconds;
    private double thresholdValueMax;


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
        this.cooldownSeconds = 0;
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

    public int getCooldownSeconds() {
        return cooldownSeconds;
    }

    public void setCooldownSeconds(int cooldownSeconds){
        this.cooldownSeconds = cooldownSeconds;
    }

    public double getThresholdValueMax() {return thresholdValueMax;}

    public void setThresholdValueMax(double thresholdValueMax) {this.thresholdValueMax = thresholdValueMax;}
}