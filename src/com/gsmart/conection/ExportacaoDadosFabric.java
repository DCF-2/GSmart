// Localização: src/com/gsmart/conection/ExportacaoDadosFabric.java
package com.gsmart.conection;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.azure.messaging.eventhubs.*;

import java.nio.charset.StandardCharsets;

/**
 * Classe utilitária responsável por enviar dados para um Fluxo de Eventos (Eventstream) do Microsoft Fabric.
 *
 * Esta classe utiliza o SDK do Azure Event Hubs para se conectar de forma segura ao endpoint
 * fornecido pelo Fabric e enviar os dados da pipeline como eventos em formato JSON.
 */
public class ExportacaoDadosFabric {

    /**
     * Envia um único objeto de dados para um Fluxo de Eventos do Fabric.
     *
     * Este método encapsula a lógica de conexão com o Event Hubs, a criação de um lote de eventos
     * e o envio assíncrono dos dados.
     *
     * @param dataObject O objeto {@code JsonObject} contendo os dados a serem enviados.
     * @param connectionString A "Connection String" completa obtida do endpoint "Aplicativo Personalizado" no Fabric.
     * @throws Exception se ocorrer um erro na comunicação com a API do Fabric (ex: connection string inválida).
     */
    public static void sendDataToFabric(JsonObject dataObject, String connectionString) throws Exception {
        if (connectionString == null || connectionString.trim().isEmpty()) {
            throw new IllegalArgumentException("A 'Connection String' do Microsoft Fabric não foi configurada corretamente.");
        }

        // O Fabric espera um objeto JSON, não um array. Vamos enviar o objeto diretamente.
        String jsonToSend = dataObject.toString();

        // Usamos um bloco try-with-resources para garantir que o producer seja fechado.
        EventHubProducerClient producerClient = null;
        try {
            // 1. Criar um cliente produtor usando a chave de ligação.
            producerClient = new EventHubClientBuilder()
                    .connectionString(connectionString)
                    .buildProducerClient();

            // 2. Criar um lote de eventos para otimizar o envio.
            EventDataBatch eventDataBatch = producerClient.createBatch();

            // 3. Adicionar o nosso JSON como um único evento ao lote.
            EventData eventData = new EventData(jsonToSend.getBytes(StandardCharsets.UTF_8));

            // Tenta adicionar o evento ao lote. Se o lote ficar cheio (muito improvável para um só evento), envia o lote atual e cria um novo.
            if (!eventDataBatch.tryAdd(eventData)) {
                producerClient.send(eventDataBatch);
                eventDataBatch = producerClient.createBatch();
                if (!eventDataBatch.tryAdd(eventData)) {
                    throw new IllegalArgumentException("O evento é muito grande para caber num lote vazio.");
                }
            }

            // 4. Envia o lote de eventos se ele contiver algum evento.
            if (eventDataBatch.getCount() > 0) {
                producerClient.send(eventDataBatch);
                System.out.println("Dados enviados para o Microsoft Fabric com sucesso!");
            }

        } catch (Exception e) {
            System.err.println("Falha ao enviar dados para o Fabric: " + e.getMessage());
            throw e; // Relança a exceção para que a DataPipeline a possa apanhar.
        } finally {
            // 5. Garante que o cliente produtor é fechado, libertando os recursos de rede.
            if (producerClient != null) {
                producerClient.close();
            }
        }
    }
}