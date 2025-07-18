package com.gsmart.services;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classe de serviço responsável por publicar mensagens num broker MQTT.
 *
 * Utiliza a biblioteca Eclipse Paho para se conectar a um broker, publicar
 * uma mensagem num tópico específico e desconectar-se de forma segura.
 */
public class MqttService {

    private static final Logger logger = LoggerFactory.getLogger(MqttService.class);

    /**
     * Conecta-se a um broker MQTT, publica uma mensagem num tópico e desconecta-se.
     *
     * @param brokerUrl O endereço do broker MQTT (ex: "tcp://localhost:1883").
     * @param topic O tópico MQTT no qual a mensagem será publicada.
     * @param message O conteúdo da mensagem a ser publicada.
     */
    public static void publish(String brokerUrl, String topic, String message) {
        if (brokerUrl == null || brokerUrl.trim().isEmpty()) {
            logger.warn("URL do Broker MQTT não configurada. A mensagem não será publicada.");
            return;
        }

        String clientId = "GSmartClient_" + System.currentTimeMillis();
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            // Cria o cliente MQTT
            MqttClient client = new MqttClient(brokerUrl, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true); // Garante que a sessão é limpa ao conectar

            logger.info("A conectar ao broker MQTT em {}", brokerUrl);
            client.connect(connOpts);
            logger.info("Conectado com sucesso ao broker.");

            // Cria a mensagem MQTT
            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            mqttMessage.setQos(1); // Quality of Service 1: Pelo menos uma vez

            // Publica a mensagem
            logger.info("A publicar mensagem no tópico '{}': {}", topic, message);
            client.publish(topic, mqttMessage);
            logger.info("Mensagem publicada com sucesso.");

            // Desconecta-se do broker
            client.disconnect();
            logger.info("Desconectado do broker.");

        } catch (MqttException me) {
            logger.error("Falha na operação MQTT. Razão: {}. Mensagem: {}. Causa: {}",
                    me.getReasonCode(), me.getMessage(), me.getCause());
        } catch (Exception e) {
            logger.error("Ocorreu um erro inesperado durante a publicação MQTT.", e);
        }
    }
}