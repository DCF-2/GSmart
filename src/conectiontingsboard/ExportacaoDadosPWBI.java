package conectiontingsboard;

import com.google.gson.JsonObject;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;

public class ExportacaoDadosPWBI {

    private static final String POWER_BI_API_URL = "https://api.powerbi.com/beta/f06a7122-3027-4312-b120-38f60897fba4/datasets/de1e5aee-b655-4e65-8db7-d8674d2e0faa/rows?experience=power-bi&key=w5jrwVRtiix7WSdcuKVvly3WbLa2FlxmMJStI6BfT%2FTGQskytnGRu1JR9fLEyZODKueBxaJh5y9oe2q9VF3XDQ%3D%3D";

    public static void sendFormattedToPowerBI(String metrica, double valor, Instant timestamp) throws Exception {
        JsonObject payload = new JsonObject();
        payload.addProperty("Metricas", metrica);
        payload.addProperty("Valor", valor);
        payload.addProperty("DataHora", timestamp.toString()); // formato ISO 8601

        // Power BI espera um array de objetos JSON
        String jsonArray = "[" + payload.toString() + "]";

        sendJsonToPowerBI(jsonArray);
    }

    public static void sendJsonToPowerBI(String jsonData) throws Exception {
        URL url = new URL(POWER_BI_API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream())) {
            writer.write(jsonData);
            writer.flush();
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_ACCEPTED) {
            System.out.println("Dados enviados para o Power BI com sucesso!");
        } else {
            throw new RuntimeException("Falha ao enviar dados para o Power BI: " + responseCode);
        }
    }
}
