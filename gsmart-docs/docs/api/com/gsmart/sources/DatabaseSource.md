# Classe: DatabaseSource

**Pacote:** `com.gsmart.sources`

## Descrição Geral

Implementação da interface `IDataSource` para se conectar e interagir  
com um banco de dados relacional através de JDBC.  
  
Esta classe é projetada para buscar o registo mais recente de uma tabela específica,  
simulando uma fonte de dados de telemetria. Ela lida com a conexão, execução de  
queries e formatação dos resultados para o formato JSON esperado pela pipeline.

- **`@see`**: com.gsmart.resources.IDataSource
- **`@see`**: java.sql.Connection


## Métodos da Classe

---

### `public boolean testConnection()`

Tenta estabelecer uma conexão com o banco de dados para validar a URL e as credenciais.

- **`@return`**: `true` se a conexão for bem-sucedida e válida, `false` caso contrário.


---

### `public List<String> getAvailableTables() throws SQLException`

Busca e retorna uma lista com os nomes de todas as tabelas visíveis no schema 'public'.

- **`@return`**: Uma lista de nomes de tabelas.
- **`@throws`**: se ocorrer um erro de acesso ao banco de dados durante a busca.


---

### `public JsonObject fetchData() throws Exception`

Busca o registro mais recente (ordenado por 'timestamp' descendente) da tabela configurada.  
Constrói e retorna um objeto JSON formatado de maneira similar à API do ThingsBoard  
para manter a compatibilidade com o resto da pipeline.

- **`@return`**: Um JsonObject contendo os dados do registro mais recente.
- **`@throws`**: se a busca no banco de dados falhar ou se a configuração estiver incompleta.


---

### `public List<String> getAvailableColumns(String tableName) throws SQLException`

Retorna uma lista com os nomes de todas as colunas de uma tabela específica.

- **Parâmetro:** `tableName` - O nome da tabela cujas colunas serão listadas.
- **`@return`**: Uma lista contendo os nomes das colunas.
- **`@throws`**: se a tabela não for encontrada ou se ocorrer um erro de acesso ao banco.


---

### `public void testConnectionAndThrow() throws SQLException`

Testa a conexão com o banco de dados e lança uma exceção em caso de falha.  
Diferente de #testConnection(), este método é projetado para interromper o fluxo  
caso a conexão não possa ser estabelecida, propagando a exceção.

- **`@throws`**: se a conexão falhar ou não for válida.


