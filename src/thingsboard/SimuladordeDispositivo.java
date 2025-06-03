package thingsboard;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;

public class SimuladordeDispositivo {

    static String IP_TB = "http://10.5.0.80:8080/api/v1/";
    private static int contadorEnvios = 0; // Contador de envios

    // Função para enviar dados para o ThingsBoard
    public static void sendToThingsBoard(String deviceToken, String jsonData) throws IOException {
        contadorEnvios++; // Incrementa a cada envio
        URL url = new URL(IP_TB + deviceToken + "/telemetry");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonData.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            System.out.println("Envio " + contadorEnvios + ":");
            System.out.println("Dados enviados com sucesso para o ThingsBoard!");
        } else {
            System.out.println("Envio " + contadorEnvios + ": Falha ao enviar dados. Código de resposta: " + responseCode);
        }
    }
}
