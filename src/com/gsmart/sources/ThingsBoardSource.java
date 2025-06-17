// Localização: src/main/java/com/gsmart/sources/ThingsBoardSource.java
package com.gsmart.sources;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class ThingsBoardSource implements IDataSource {
    private static final String USERNAME = "tenant@thingsboard.org";
    private static final String PASSWORD = "tenant";
    private final String apiUrl;
    private final String deviceId;
    private static final String TELEMETRY_KEYS = "ConsDiaP,ConsHora,ConsHoraP,EAkWh,Envio,Erro,Ia_n,Ib_n,Ic_n,NSerie,temperature,Va_n,Vb_n,Vc_n,VFreq,WiFi.RSSI,Ptot,Fator_Potencia,Va_b,Vb_c,Vc_a";

    public ThingsBoardSource(String apiUrl, String deviceId) {
        this.apiUrl = apiUrl;
        this.deviceId = deviceId;
    }

    @Override
    public JsonObject fetchData() throws Exception {
        String token = this.getToken();
        return this.fetchTelemetryData(token);
    }

    private String getToken() throws Exception {
        URL url = new URL(this.apiUrl + "/api/auth/login");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        String payload = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", USERNAME, PASSWORD);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes("UTF-8"));
        }
        try (InputStream is = conn.getInputStream(); Scanner scanner = new Scanner(is)) {
            return JsonParser.parseString(scanner.useDelimiter("\\A").next()).getAsJsonObject().get("token").getAsString();
        }
    }

    private JsonObject fetchTelemetryData(String token) throws Exception {
        String dataUrl = String.format("%s/api/plugins/telemetry/DEVICE/%s/values/timeseries?keys=%s", this.apiUrl, this.deviceId, TELEMETRY_KEYS);
        URL url = new URL(dataUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("X-Authorization", "Bearer " + token);
        try (InputStream is = conn.getInputStream(); Scanner scanner = new Scanner(is)) {
            return JsonParser.parseString(scanner.useDelimiter("\\A").next()).getAsJsonObject();
        }
    }

    @Override
    public String getSourceName() {
        return "ThingsBoard (Device: " + this.deviceId + ")";
    }
}