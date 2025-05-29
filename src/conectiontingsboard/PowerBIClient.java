package conectiontingsboard;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class PowerBIClient {
    private final String powerBIUrl;

    public PowerBIClient(String powerBIUrl) {
        this.powerBIUrl = powerBIUrl;
    }

    public void sendData(String jsonPayload) throws Exception {
        URL url = new URL(powerBIUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream())) {
            writer.write(jsonPayload);
            writer.flush();
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Erro ao enviar dados para o Power BI: HTTP " + responseCode);
        }

        System.out.println("Dados enviados para o Power BI com sucesso.");
    }
}
