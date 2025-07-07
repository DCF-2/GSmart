# Classe: ThingsBoardSource

**Pacote:** `com.gsmart.sources`

## Descrição Geral

Implementação da interface `IDataSource` para se conectar e interagir  
com uma instância da plataforma de IoT ThingsBoard.  
  
Esta classe utiliza a biblioteca OkHttp para realizar chamadas à API REST do ThingsBoard  
para autenticação, busca de metadados (perfis de dispositivo, dispositivos) e  
coleta de dados de telemetria em tempo real.

- **`@see`**: com.gsmart.resources.IDataSource
- **`@see`**: okhttp3.OkHttpClient


## Métodos da Classe

---

### `public boolean testConnection()`

Tenta autenticar no servidor ThingsBoard para validar a URL e as credenciais padrão.  
É um método de baixo custo para verificar a conectividade básica antes de  
prosseguir com operações mais complexas.

- **`@return`**: `true` se a autenticação for bem-sucedida, `false` caso contrário.


---

### `private void ensureAuthenticated() throws IOException`

Garante que uma sessão autenticada com o ThingsBoard exista.  
Se um token de autenticação ainda não foi obtido, este método realiza a  
requisição de login e armazena o token para uso em chamadas futuras.

- **`@throws`**: se a requisição de autenticação falhar.


---

### `public List<DeviceProfile> getDeviceProfiles() throws IOException`

Busca no servidor ThingsBoard a lista de todos os perfis de dispositivo disponíveis.

- **`@return`**: Uma lista de objetos `DeviceProfile`, cada um contendo o nome e o ID de um perfil.
- **`@throws`**: se a requisição à API falhar.


---

### `public List<Device> getDevicesByProfileId(String deviceProfileId) throws IOException`

Busca no servidor ThingsBoard a lista de todos os dispositivos associados a um  
perfil de dispositivo específico.

- **Parâmetro:** `deviceProfileId` - O ID do perfil de dispositivo cujos dispositivos serão listados.
- **`@return`**: Uma lista de objetos `Device`.
- **`@throws`**: se a requisição à API falhar.


---

### `public List<String> getAvailableKeys() throws IOException`

Busca no servidor ThingsBoard a lista de todas as chaves de telemetria  
disponíveis para o dispositivo atualmente configurado.

- **`@return`**: Uma lista de `String` contendo os nomes das chaves (ex: "temperatura", "humidade").
- **`@throws`**: se a requisição à API falhar.


---

### `public void clearAuthToken()`

Limpa o token de autenticação armazenado.  
Este método é chamado quando ocorre uma falha de conexão, forçando  
uma nova tentativa de autenticação no próximo ciclo.

