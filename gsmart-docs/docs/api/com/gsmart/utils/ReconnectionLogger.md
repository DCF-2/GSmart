# Classe: `ReconnectionLogger`

**Pacote:** `com.gsmart.utils`

## Descrição Geral

Classe utilitária dedicada a registar eventos específicos de conexão da pipeline.  
  
Esta classe utiliza um logger com o nome "ReconnectionLogger", que é configurado  
no ficheiro logback.xml para escrever num ficheiro de log separado (ex: reconnection.log).  
Isto permite isolar e analisar facilmente os eventos de perda e restabelecimento  
de conexão, facilitando a depuração de problemas de rede sem poluir o log geral da aplicação.

## Métodos da Classe

---

### `public static void logConnectionLost(String pipelineName)`

Regista uma mensagem de "Conexão Perdida" no log de reconexão.

- **Parâmetro:** `pipelineName` - O nome da pipeline que perdeu a conexão.


---

### `public static void logConnectionRestored(String pipelineName)`

Regista uma mensagem de "Conexão Restabelecida" no log de reconexão.

- **Parâmetro:** `pipelineName` - O nome da pipeline que restabeleceu a conexão.


