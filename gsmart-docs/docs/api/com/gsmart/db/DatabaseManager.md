# Classe: `DatabaseManager`

**Pacote:** `com.gsmart.db`

## Descrição Geral

Gestor central para todas as operações da base de dados da aplicação GSmart.  
  
Esta classe é responsável por inicializar a base de dados SQLite, criar o schema  
necessário (tabelas de utilizadores e perfis) na primeira execução, e fornecer  
métodos para interagir com a base de dados.  
  
Utiliza o padrão Singleton para garantir que existe apenas uma instância  
a gerir a conexão.

## Métodos da Classe

---

### `public static void initializeDatabase()`

Inicializa a base de dados.  
  
Verifica se o ficheiro da base de dados existe. Se não existir, ou se as tabelas  
essenciais estiverem em falta, o método cria a estrutura necessária e insere  
os dados iniciais (perfis de acesso e utilizador administrador padrão).

---

### `private static Connection getConnection() throws SQLException`

Estabelece e retorna uma conexão com a base de dados SQLite.

- **Retorna:** Uma instância de `Connection`.
- **`@throws`**: se ocorrer um erro ao conectar.


---

### `private static void createTablesIfNotExists(Connection conn) throws SQLException`

Cria as tabelas 'roles' e 'users' se elas ainda não existirem.

- **Parâmetro:** `conn` - A conexão ativa com a base de dados.


---

### `private static void insertInitialData(Connection conn) throws SQLException`

Insere os perfis de acesso padrão ('ADMINISTRATOR', 'OPERATOR') e um utilizador  
administrador inicial se eles ainda não existirem.

- **Parâmetro:** `conn` - A conexão ativa com a base de dados.


---

### `public static String validateLogin(String username, String plainPassword)`

Valida as credenciais de um utilizador contra a base de dados.

- **Parâmetro:** `username` - O nome de utilizador a ser validado.
- **Parâmetro:** `plainPassword` - A senha em texto puro fornecida pelo utilizador.
- **Retorna:** O perfil (role) do utilizador se a autenticação for bem-sucedida, caso contrário null.


---

### `public static java.util.List<com.gsmart.model.User> getAllUsers()`

Busca e retorna uma lista de todos os utilizadores registados na base de dados.

- **Retorna:** Uma lista de objetos `com.gsmart.model.User`.


---

### `public static boolean addUser(String username, String plainPassword, String role)`

Adiciona um novo utilizador à base de dados.  
A senha é automaticamente encriptada (hashed) antes de ser guardada.

- **Parâmetro:** `username` - O nome de utilizador.
- **Parâmetro:** `plainPassword` - A senha em texto puro.
- **Parâmetro:** `role` - O perfil de acesso ("ADMINISTRATOR" ou "OPERATOR").
- **Retorna:** `true` se o utilizador for adicionado com sucesso, `false` em caso de erro (ex: nome de utilizador já existe).


---

### `private static boolean userExists(String username)`

Verifica se um nome de utilizador já existe na base de dados.

- **Parâmetro:** `username` - O nome de utilizador a verificar.
- **Retorna:** `true` se o utilizador existe, `false` caso contrário.


---

### `public static boolean updateUser(int userId, String newUsername, String newRole)`

Atualiza as informações de um utilizador existente (exceto a senha).

- **Parâmetro:** `userId` - O ID do utilizador a ser atualizado.
- **Parâmetro:** `newUsername` - O novo nome de utilizador.
- **Parâmetro:** `newRole` - O novo perfil de acesso.
- **Retorna:** `true` se a atualização for bem-sucedida, `false` caso contrário.


---

### `public static boolean updateUserPassword(int userId, String newPlainPassword)`

Atualiza a senha de um utilizador específico.

- **Parâmetro:** `userId` - O ID do utilizador cuja senha será alterada.
- **Parâmetro:** `newPlainPassword` - A nova senha em texto puro.
- **Retorna:** `true` se a senha for atualizada com sucesso, `false` caso contrário.


---

### `public static boolean removeUser(int userId)`

Remove um utilizador da base de dados com base no seu ID.  
Impede a remoção do utilizador "admin" por segurança.

- **Parâmetro:** `userId` - O ID do utilizador a ser removido.
- **Retorna:** `true` se o utilizador for removido com sucesso, `false` caso contrário.


