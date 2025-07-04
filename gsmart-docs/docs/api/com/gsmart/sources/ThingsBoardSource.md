# Classe: ThingsBoardSource

**Pacote:** `com.gsmart.sources`

## Descrição Geral

Implementação da interface {@link IDataSource} para se conectar e interagir
com uma instância da plataforma de IoT ThingsBoard.
Esta classe utiliza a biblioteca OkHttp para realizar chamadas à API REST do ThingsBoard
para autenticação, busca de metadados (perfis, dispositivos) e coleta de
dados de telemetria.

## Métodos

---

### `public boolean testConnection()`

Tenta autenticar no servidor para validar a URL e as credenciais padrão.
É um método de baixo custo para verificar a conectividade básica.

---

### `private void ensureAuthenticated() throws IOException`

Garante que uma sessão autenticada com o ThingsBoard exista.
Se um token de autenticação ainda não foi obtido, este método realiza a
requisição de login e armazena o token para uso em chamadas futuras.

---

### `public List<DeviceProfile> getDeviceProfiles() throws IOException`

Busca metadados do servidor ThingsBoard.
(getDeviceProfiles -> Busca todos os perfis de dispositivo disponíveis.)
(getDevicesByProfileId -> Busca todos os dispositivos associados a um perfil específico.)
(getAvailableKeys -> Busca todas as chaves de telemetria de um dispositivo específico.)

---

### `public List<Device> getDevicesByProfileId(String deviceProfileId) throws IOException`

Busca metadados do servidor ThingsBoard.
(getDeviceProfiles -> Busca todos os perfis de dispositivo disponíveis.)
(getDevicesByProfileId -> Busca todos os dispositivos associados a um perfil específico.)
(getAvailableKeys -> Busca todas as chaves de telemetria de um dispositivo específico.)

---

### `public List<String> getAvailableKeys() throws IOException`

Busca metadados do servidor ThingsBoard.
(getDeviceProfiles -> Busca todos os perfis de dispositivo disponíveis.)
(getDevicesByProfileId -> Busca todos os dispositivos associados a um perfil específico.)
(getAvailableKeys -> Busca todas as chaves de telemetria de um dispositivo específico.)

---

### `public void clearAuthToken()`

Limpa o token de autenticação armazenado.
Este método é chamado quando ocorre uma falha de conexão, forçando
uma nova tentativa de autenticação no próximo ciclo.

