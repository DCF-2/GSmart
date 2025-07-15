# Classe: `IDataSource`

**Pacote:** `com.gsmart.resources`

## Descrição Geral

Define o contrato para todas as fontes de dados do sistema GSmart.  
  
Qualquer classe que represente uma fonte de dados, como um banco de dados  
ou uma API de IoT, deve implementar esta interface. Isso garante que a  
`DataPipeline` possa interagir com qualquer fonte de forma padronizada,  
promovendo a extensibilidade do sistema.

## Métodos da Classe

---

### ` abstract JsonObject fetchData() throws Exception`

Busca o conjunto de dados mais recente da fonte.  
  
A implementação deste método deve lidar com toda a lógica de comunicação,  
como realizar requisições HTTP ou executar consultas a um banco de dados.

- **Retorna:** Um objeto `JsonObject` contendo os dados de telemetria ou os  
registos obtidos. O formato deve ser consistente para o processamento.
- **`@throws`**: Se ocorrer qualquer erro durante a comunicação ou a  
coleta dos dados (ex: falha de rede, erro de autenticação).


---

### ` abstract String getSourceName()`

Retorna um nome descritivo e legível para a fonte de dados.  
  
Este nome é utilizado na interface do utilizador para identificar a tarefa  
de monitoramento e em logs para facilitar a depuração.

- **Retorna:** Uma `String` com o nome da fonte (ex: "ThingsBoard" ou "Banco de Dados").


