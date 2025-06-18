// Localização: src/main/java/com/gsmart/sources/ThingsBoardSource.java
package com.gsmart.sources;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ThingsBoardSource implements IDataSource {
    private static final String USERNAME = "tenant@thingsboard.org";
    private static final String PASSWORD = "tenant";
    private final String apiUrl;
    private final String deviceId;
    private final List<String> selectedKeys;

    public ThingsBoardSource(String apiUrl, String deviceId, List<String> selectedKeys) {
        this.apiUrl = apiUrl;
        this.deviceId = deviceId;
        this.selectedKeys = selectedKeys;
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
        // --- AQUI ESTÁ A LÓGICA CORRIGIDA ---
        // Se a lista de chaves selecionadas pelo usuário for nula ou vazia,
        // simplesmente retorna um objeto JSON vazio, sem fazer a chamada à API.
        if (this.selectedKeys == null || this.selectedKeys.isEmpty()) {
            System.out.println("Nenhuma métrica selecionada para busca no ThingsBoard. Retornando dados vazios.");
            return new JsonObject();
        }

        // Se houver chaves, monta a URL apenas com elas.
        String keysToFetch = String.join(",", this.selectedKeys);

        String dataUrl = String.format("%s/api/plugins/telemetry/DEVICE/%s/values/timeseries?keys=%s", this.apiUrl, this.deviceId, keysToFetch);
        URL url = new URL(dataUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("X-Authorization", "Bearer " + token);
        try (InputStream is = conn.getInputStream(); Scanner scanner = new Scanner(is)) {
            return JsonParser.parseString(scanner.useDelimiter("\\A").next()).getAsJsonObject();
        }
    }

    public List<String> getAvailableKeys() throws Exception {
        String token = this.getToken();
        String keysUrl = String.format("%s/api/plugins/telemetry/DEVICE/%s/keys/timeseries", this.apiUrl, this.deviceId);
        URL url = new URL(keysUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("X-Authorization", "Bearer " + token);
        List<String> availableKeys = new ArrayList<>();
        try (InputStream is = conn.getInputStream(); Scanner scanner = new Scanner(is)) {
            JsonArray jsonArray = JsonParser.parseString(scanner.useDelimiter("\\A").next()).getAsJsonArray();
            jsonArray.forEach(jsonElement -> availableKeys.add(jsonElement.getAsString()));
        }
        return availableKeys;
    }

    @Override
    public String getSourceName() {
        return "Thingsboard API";
    }
}