// Localização: src/main/java/com/gsmart/sources/ThingsBoardSource.java
package main.java.com.gsmart.sources;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import main.java.com.gsmart.resources.IDataSource;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação da interface {@code IDataSource} para se conectar e interagir
 * com uma instância da plataforma de IoT ThingsBoard.
 *
 * Esta classe utiliza a biblioteca OkHttp para realizar chamadas à API REST do ThingsBoard
 * para autenticação, busca de metadados (perfis de dispositivo, dispositivos) e
 * coleta de dados de telemetria em tempo real.
 *
 * @see main.java.com.gsmart.resources.IDataSource
 * @see okhttp3.OkHttpClient
 */
public class ThingsBoardSource implements IDataSource {
    private static final Logger logger = LoggerFactory.getLogger(ThingsBoardSource.class);

    private final String thingsboardUrl;
    private final String username;
    private final String password;
    private final String deviceId;
    private String deviceName;
    private final List<String> keysToFetch;
    private final OkHttpClient client;
    private String authToken;


    /**
     * Construtor da classe ThingsBoardSource.
     *
     * @param thingsboardUrl A URL base do servidor ThingsBoard (ex: "http://host:port").
     * @param deviceId O ID do dispositivo específico do qual os dados serão buscados.
     * @param deviceName O Nome do Dispositivo específico do qual os dados serão buscados.
     * @param keysToFetch Uma lista de chaves de telemetria a serem requisitadas na API.
     * @param client Uma instância partilhada de OkHttpClient para realizar as requisições HTTP.
     */
    public ThingsBoardSource(String thingsboardUrl, String username, String password, String deviceId, String deviceName,List<String> keysToFetch, OkHttpClient client) {
        this.thingsboardUrl = thingsboardUrl != null && thingsboardUrl.endsWith("/") ? thingsboardUrl.substring(0, thingsboardUrl.length() - 1) : thingsboardUrl;
        this.username = username;
        this.password = password;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.keysToFetch = keysToFetch;
        this.client = client;
    }


    /**
     * Tenta autenticar no servidor ThingsBoard para validar a URL e as credenciais padrão.
     * É um método de baixo custo para verificar a conectividade básica antes de
     * prosseguir com operações mais complexas.
     *
     * @return {@code true} se a autenticação for bem-sucedida, {@code false} caso contrário.
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
        if (this.deviceName != null && !this.deviceName.isEmpty()) {
            return this.deviceName; // Retorna o nome do dispositivo
        }
        if (this.deviceId != null && !this.deviceId.isEmpty()) {
            return "ThingsBoard (Device: " + this.deviceId + ")";
        }
        return "ThingsBoard";
    }

    /**
     * Garante que uma sessão autenticada com o ThingsBoard exista.
     * Se um token de autenticação ainda não foi obtido, este método realiza a
     * requisição de login e armazena o token para uso em chamadas futuras.
     *
     * @throws IOException se a requisição de autenticação falhar.
     */
    private void ensureAuthenticated() throws IOException {
        if (this.authToken != null) { return; }
        logger.info("Autenticando no ThingsBoard em {}...", this.thingsboardUrl);
        String authUrl = this.thingsboardUrl + "/api/auth/login";
        String credentials = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", this.username, this.password);
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

    /**
     * Busca no servidor ThingsBoard a lista de todos os perfis de dispositivo disponíveis.
     *
     * @return Uma lista de objetos {@code DeviceProfile}, cada um contendo o nome e o ID de um perfil.
     * @throws java.io.IOException se a requisição à API falhar.
     */
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

    /**
     * Busca no servidor ThingsBoard a lista de todos os dispositivos associados a um
     * perfil de dispositivo específico.
     *
     * @param deviceProfileId O ID do perfil de dispositivo cujos dispositivos serão listados.
     * @return Uma lista de objetos {@code Device}.
     * @throws java.io.IOException se a requisição à API falhar.
     */
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

    /**
     * Busca no servidor ThingsBoard a lista de todas as chaves de telemetria
     * disponíveis para o dispositivo atualmente configurado.
     *
     * @return Uma lista de {@code String} contendo os nomes das chaves (ex: "temperatura", "humidade").
     * @throws java.io.IOException se a requisição à API falhar.
     */
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

    /**
     * Limpa o token de autenticação armazenado.
     * Este método é chamado quando ocorre uma falha de conexão, forçando
     * uma nova tentativa de autenticação no próximo ciclo.
     */
    public void clearAuthToken() {
        this.authToken = null;
        logger.info("Token de autenticação do ThingsBoard foi limpo devido a uma falha de conexão.");
    }
    public String getThingsboardUrl() {return thingsboardUrl;}
    public String getDeviceId() {return deviceId;}
    public String getDeviceName() {return deviceName;}
    public String getUsername() {return username;}
    public String getPassword() {return password;}
}
