package thingsboard;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;

public class ThingsBoardClient {

    // Envia os dados para o ThingsBoard
    public static void sendData(String accessToken, String jsonData) throws Exception {
        URL url = new URL("http://<thingsboard_host>/api/v1/" + accessToken + "/telemetry");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // Envia os dados em JSON
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonData.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Recebe a resposta
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            System.out.println("Dados enviados com sucesso.");
        } else {
            System.out.println("Falha no envio de dados. Código de resposta: " + responseCode);
        }
    }

    public static void sendContinuousData(String accessToken) {
        try {
            while (true) {
                // Simulando dados de sensores
                double temperature = 20 + (Math.random() * 10);  // Temperatura entre 20 e 30
                double humidity = 30 + (Math.random() * 50);    // Umidade entre 30 e 80

                // Criando o payload em JSON
                String jsonData = String.format("{\"temperature\": %.2f, \"humidity\": %.2f}", temperature, humidity);

                // Enviar os dados para o ThingsBoard
                sendData(accessToken, jsonData);

                // Aguardar 10 segundos antes de enviar os próximos dados
                Thread.sleep(10000);  // 10 segundos
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String accessToken = "<SEU_ACCESS_TOKEN>"; // Coloque seu token de acesso aqui
        sendContinuousData(accessToken);
    }
}

