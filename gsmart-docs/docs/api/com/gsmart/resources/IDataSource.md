# Classe: IDataSource

**Pacote:** `com.gsmart.resources`

## Descrição Geral

Define o contrato para todas as fontes de dados do sistema GSmart.
Qualquer classe que represente uma fonte de dados, como um banco de dados
ou uma API de IoT, deve implementar esta interface para garantir que a
{@link com.gsmart.pipeline.DataPipeline} possa interagir com ela de forma padronizada.

## Métodos

---

### ` abstract JsonObject fetchData() throws Exception`

Busca o conjunto de dados mais recente da fonte.
A implementação deve lidar com toda a lógica de comunicação, como
requisições HTTP ou consultas a banco de dados.

---

### ` abstract String getSourceName()`

Retorna um nome descritivo e legível para a fonte de dados.
Este nome é usado na interface do usuário para identificar a tarefa.

