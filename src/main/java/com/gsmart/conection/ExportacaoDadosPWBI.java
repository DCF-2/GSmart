// Localização: src/main/java/conectiontingsboard/ExportacaoDadosPWBI.java
package main.java.com.gsmart.conection;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Classe utilitária responsável por enviar dados para a API de Push do Microsoft Power BI.
 *
 * Esta classe contém um método estático que formata o objeto de dados num payload JSON,
 * conforme esperado pela API, e realiza uma requisição POST para o endpoint especificado,
 * tratando a resposta do servidor.
 */
public class ExportacaoDadosPWBI {

    /**
     * Envia um único objeto de dados para um endpoint da API de Push do Power BI.
     *
     * Este método encapsula a lógica de conexão HTTP, a formatação do corpo da requisição
     * e o tratamento da resposta do servidor para garantir que os dados foram recebidos com sucesso.
     *
     * @param dataObject O objeto {@code JsonObject} contendo os dados a serem enviados.
     * @param powerBiApiUrl A URL completa do conjunto de dados de streaming do Power BI.
     * @throws IllegalArgumentException se a URL do Power BI for nula, vazia ou inválida.
     * @throws Exception se ocorrer um erro na comunicação com a API do Power BI (ex: código de resposta não-2xx).
     */
    public static void sendDataToPowerBI(JsonObject dataObject, String powerBiApiUrl) throws Exception {
        if (powerBiApiUrl == null || powerBiApiUrl.trim().isEmpty() || powerBiApiUrl.equals("SUA_URL_DE_PUSH_DO_POWER_BI_AQUI")) {
            throw new IllegalArgumentException("A URL da API do Power BI não foi configurada corretamente.");
        }

        JsonArray payloadArray = new JsonArray();
        payloadArray.add(dataObject);
        String jsonToSend = payloadArray.toString();

        System.out.println("\n[DEBUG POWER BI] Enviando para " + powerBiApiUrl);

        URL url = URI.create(powerBiApiUrl).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setDoOutput(true);

        try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8)) {
            writer.write(jsonToSend);
            writer.flush();
        }

        int responseCode = connection.getResponseCode();
        if (responseCode >= 200 && responseCode < 300) {
            System.out.println("Dados enviados para o Power BI com sucesso!");
        } else {
            String responseMessage = connection.getResponseMessage();
            try (InputStream errorStream = connection.getErrorStream()) {
                if (errorStream != null) {
                    try (Scanner scanner = new Scanner(errorStream)) {
                        responseMessage += " - " + scanner.useDelimiter("\\A").next();
                    }
                }
            } catch (Exception ex) {}
            throw new RuntimeException("Falha ao enviar dados para o Power BI. Código: " + responseCode + " | " + responseMessage);
        }
    }
}