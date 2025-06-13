package conectiontingsboard;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class ThingsBoardAPI {

    private static final String AUTH_URL = "http://10.8.0.5:8080/api/auth/login";
    private static final String DATA_URL = "http://10.8.0.5:8080/api/plugins/telemetry/DEVICE/06e109e0-e8ff-11ee-bb8b-2563c61db9b0/values/timeseries?keys=ConsDiaP,ConsHoraP,EAkWh,Envio,Erro,HdDev,Ia_n,Ib_n,Ic_n,NSerie,Va_n,Vb_n,Vc_n,VFreq,WiFi.RSSI,Ptot,Fator_Potencia,Va_b,Vb_c,Vc_a,ConsHora";
    private static final String USERNAME = "tenant@thingsboard.org";
    private static final String PASSWORD = "tenant";

    public static String getToken() throws Exception {
        URL url = new URL(AUTH_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // Criar o JSON de login
        String loginPayload = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", USERNAME, PASSWORD);
        try (OutputStream os = connection.getOutputStream()) {
            os.write(loginPayload.getBytes());
            os.flush();
        }

        // Obter a resposta
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            try (Scanner scanner = new Scanner(connection.getInputStream())) {
                String response = scanner.useDelimiter("\\A").next();
                // Parsear o token JWS
                JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
                return jsonObject.get("token").getAsString();
            }
        } else {
            throw new RuntimeException("Erro ao autenticar: " + connection.getResponseCode() + " - " + connection.getResponseMessage());
        }
    }

    public static JsonObject fetchData(String token) throws Exception {
        URL url = new URL(DATA_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("X-Authorization", "Bearer " + token);

        // Obter a resposta
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            try (Scanner scanner = new Scanner(connection.getInputStream())) {
                String response = scanner.useDelimiter("\\A").next();
                // Retornar os dados da API
                return JsonParser.parseString(response).getAsJsonObject();
            }
        } else {
            throw new RuntimeException("Erro ao buscar dados: " + connection.getResponseCode() + " - " + connection.getResponseMessage());
        }
    }

    public static void main(String[] args) {
        try {
            String token = getToken();
            System.out.println("Token obtido: " + token);

            JsonObject data = fetchData(token);
            System.out.println("Dados obtidos: " + data.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



