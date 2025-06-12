package conectiontingsboard;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ExportacaoDadosPWBI {

    private static final String POWER_BI_API_URL = "https://api.powerbi.com/beta/f06a7122-3027-4312-b120-38f60897fba4/datasets/f66d6aac-aa43-43ed-95e7-b0ddd73aa55b/rows?experience=power-bi&key=%2FGiFg%2FatH4ofhOC4mhiHAvtkRDXKbzVrQ0hF55HguIl0izrL2sOT7mLLd7F55BJr1I%2FFEmGL3549XtSg4o%2FfpA%3D%3D";

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


