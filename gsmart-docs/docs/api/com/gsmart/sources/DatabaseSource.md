# Classe: DatabaseSource

**Pacote:** `com.gsmart.sources`

## Descrição Geral

Implementação da interface {@link IDataSource} para se conectar e interagir
com um banco de dados relacional através de JDBC.
Esta classe é projetada para buscar o registro mais recente de uma tabela específica,
simulando uma fonte de dados de telemetria.

## Métodos

---

### `public boolean testConnection()`

Tenta estabelecer uma conexão com o banco de dados para validar a URL e as credenciais.

---

### `public List<String> getAvailableTables() throws SQLException`

Busca e retorna uma lista com os nomes de todas as tabelas visíveis no schema 'public'.

---

### `public JsonObject fetchData() throws Exception`

Busca o registro mais recente (ordenado por 'timestamp' descendente) da tabela configurada.
Constrói e retorna um objeto JSON formatado de maneira similar à API do ThingsBoard
para manter a compatibilidade com o resto da pipeline.

---

### `public List<String> getAvailableColumns(String tableName) throws SQLException`

Retorna uma lista com os nomes de todas as colunas de uma tabela específica.

---

### `public void testConnectionAndThrow() throws SQLException`

Testa a conexão com o banco de dados e lança uma exceção em caso de falha.
Diferente de {@link #testConnection()}, este método é projetado para interromper o fluxo
caso a conexão não possa ser estabelecida, propagando a exceção.

