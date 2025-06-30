// Localização: src/main/java/com/gsmart/sources/ThingsBoardSource.java
package com.gsmart.sources;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ThingsBoardSource implements IDataSource {
    private static final Logger logger = LoggerFactory.getLogger(ThingsBoardSource.class);

    private final String thingsboardUrl;
    private final String deviceId;
    private final List<String> keysToFetch;
    private final OkHttpClient client;
    private String authToken;

    public ThingsBoardSource(String thingsboardUrl, String deviceId, List<String> keysToFetch, OkHttpClient client) {
        this.thingsboardUrl = thingsboardUrl != null && thingsboardUrl.endsWith("/") ? thingsboardUrl.substring(0, thingsboardUrl.length() - 1) : thingsboardUrl;
        this.deviceId = deviceId;
        this.keysToFetch = keysToFetch;
        this.client = client;
    }


    /**
     * Tenta autenticar no servidor para validar a URL e as credenciais.
     * @return true se a autenticação for bem-sucedida, false caso contrário.
     */
    public boolean testConnection() {
        try {
            ensureAuthenticated();
            return true;
        } catch (IOException e) {
            logger.error("Falha no teste de conexão com o ThingsBoard: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getSourceName() {
        if (this.deviceId != null && !this.deviceId.isEmpty()) {
            return "ThingsBoard (Device: " + this.deviceId + ")";
        }
        return "ThingsBoard";
    }

    private void ensureAuthenticated() throws IOException {
        if (this.authToken != null) { return; }
        logger.info("Autenticando no ThingsBoard em {}...", this.thingsboardUrl);
        String authUrl = this.thingsboardUrl + "/api/auth/login";
        String credentials = "{\"username\":\"tenant@thingsboard.org\", \"password\":\"tenant\"}";
        RequestBody body = RequestBody.create(credentials, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder().url(authUrl).post(body).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new IOException("Falha na autenticação: " + response.code() + " " + response.message());
            }
            String jsonResponse = response.body().string();
            this.authToken = JsonParser.parseString(jsonResponse).getAsJsonObject().get("token").getAsString();
            logger.info("Autenticação bem-sucedida!");
        }
    }

    public List<DeviceProfile> getDeviceProfiles() throws IOException {
        ensureAuthenticated();
        // Endpoint para buscar informações dos perfis de dispositivo (funciona em v2.5+)
        String profilesUrl = this.thingsboardUrl + "/api/deviceProfileInfos?pageSize=100&page=0";
        logger.info("Buscando perfis de dispositivo...");
        Request request = new Request.Builder().url(profilesUrl).addHeader("X-Authorization", "Bearer " + this.authToken).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new IOException("Falha ao buscar perfis de dispositivo: " + response.code() + " " + response.message());
            }
            String jsonResponse = response.body().string();
            List<DeviceProfile> profiles = new ArrayList<>();
            JsonObject pageData = JsonParser.parseString(jsonResponse).getAsJsonObject();
            pageData.getAsJsonArray("data").forEach(element -> {
                JsonObject profileObject = element.getAsJsonObject();
                String name = profileObject.get("name").getAsString();
                String id = profileObject.get("id").getAsJsonObject().get("id").getAsString();
                profiles.add(new DeviceProfile(name, id));
            });
            logger.info("Encontrados {} perfis de dispositivo.", profiles.size());
            return profiles;
        }
    }

    public List<Device> getDevicesByProfileId(String deviceProfileId) throws IOException {
        ensureAuthenticated();
        // Este é o endpoint correto e robusto para buscar dispositivos por ID de perfil
        String devicesUrl = String.format("%s/api/tenant/devices?deviceProfileId=%s&pageSize=1024&page=0", this.thingsboardUrl, deviceProfileId);
        logger.info("Buscando dispositivos para o perfil ID: {}", deviceProfileId);
        Request request = new Request.Builder().url(devicesUrl).addHeader("X-Authorization", "Bearer " + this.authToken).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new IOException("Falha ao buscar dispositivos por ID de perfil: " + response.code() + " " + response.message());
            }
            String jsonResponse = response.body().string();
            List<Device> devices = new ArrayList<>();
            JsonObject pageData = JsonParser.parseString(jsonResponse).getAsJsonObject();
            pageData.getAsJsonArray("data").forEach(element -> {
                JsonObject deviceObject = element.getAsJsonObject();
                String name = deviceObject.get("name").getAsString();
                String id = deviceObject.get("id").getAsJsonObject().get("id").getAsString();
                devices.add(new Device(name, id));
            });
            logger.info("Encontrados {} dispositivos.", devices.size());
            return devices;
        }
    }

    // Métodos antigos (fetchData, getAvailableKeys) permanecem os mesmos
    @Override
    public JsonObject fetchData() throws IOException {
        ensureAuthenticated();
        String keys = String.join(",", this.keysToFetch);
        String dataUrl = String.format("%s/api/plugins/telemetry/DEVICE/%s/values/timeseries?keys=%s", this.thingsboardUrl, this.deviceId, keys);
        Request request = new Request.Builder().url(dataUrl).addHeader("X-Authorization", "Bearer " + this.authToken).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new IOException("Falha ao buscar dados do ThingsBoard: " + response.code() + " " + response.message());
            }
            return JsonParser.parseString(response.body().string()).getAsJsonObject();
        }
    }

    public List<String> getAvailableKeys() throws IOException {
        ensureAuthenticated();
        String keysUrl = String.format("%s/api/plugins/telemetry/DEVICE/%s/keys/timeseries", this.thingsboardUrl, this.deviceId);
        Request request = new Request.Builder().url(keysUrl).addHeader("X-Authorization", "Bearer " + this.authToken).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new IOException("Falha ao buscar lista de chaves: " + response.code() + " " + response.message());
            }
            List<String> keys = new ArrayList<>();
            JsonParser.parseString(response.body().string()).getAsJsonArray().forEach(element -> keys.add(element.getAsString()));
            return keys;
        }
    }
    public void testConnectionAndThrow() throws IOException {
        ensureAuthenticated(); // Este metodo já lança uma IOException em caso de falha
    }
    public void clearAuthToken() {
        this.authToken = null;
        logger.info("Token de autenticação do ThingsBoard foi limpo devido a uma falha de conexão.");
    }
}
