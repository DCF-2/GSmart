package conectiontingsboard;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ExportacaoDadosPWBI {

    private static final String POWER_BI_API_URL = "https://api.powerbi.com/beta/f06a7122-3027-4312-b120-38f60897fba4/datasets/b8512173-e419-4a24-9559-2f9f52935190/rows?experience=power-bi&key=%2F7U6mAvLW4ddN8tfVZJfT30CTl6LGrU4wmW%2FdotkmHnoj5eaVfUZh9fzlWFCgFnsSWI55MecpelCBmyb9reDpw%3D%3D";

    /**
     * Envia um objeto JSON para a API do Power BI.
     * O objeto já deve conter todas as métricas formatadas.
     * @param dataObject O objeto JSON a ser enviado.
     * @throws Exception Se ocorrer um erro no envio.
     */
    public static void sendDataToPowerBI(JsonObject dataObject) throws Exception {
        // A API do Power BI espera receber um array contendo um ou mais objetos
        JsonArray payloadArray = new JsonArray();
        payloadArray.add(dataObject);

        String jsonToSend = payloadArray.toString();
        System.out.println("\n[DEBUG POWER BI] Tentando enviar o seguinte JSON:");
        System.out.println(jsonToSend);
        sendJsonToPowerBI(jsonToSend);
    }

    private static void sendJsonToPowerBI(String jsonData) throws Exception {
        URL url = URI.create(POWER_BI_API_URL).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setDoOutput(true);

        try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8)) {
            writer.write(jsonData);
            writer.flush();
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_ACCEPTED) {
            System.out.println("Dados enviados para o Power BI com sucesso!\n");
        } else {
            throw new RuntimeException("Falha ao enviar dados para o Power BI. Código de resposta: " + responseCode + " " + connection.getResponseMessage());
        }
    }
}


