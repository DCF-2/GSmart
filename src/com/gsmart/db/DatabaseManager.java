// Localização: src/com/gsmart/db/DatabaseManager.java
package com.gsmart.db;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.*;

/**
 * Gestor central para todas as operações da base de dados da aplicação GSmart.
 *
 * Esta classe é responsável por inicializar a base de dados SQLite, criar o schema
 * necessário (tabelas de utilizadores e perfis) na primeira execução, e fornecer
 * métodos para interagir com a base de dados.
 *
 * Utiliza o padrão Singleton para garantir que existe apenas uma instância
 * a gerir a conexão.
 */
public class DatabaseManager {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static final String DB_FILE = "gsmart_users.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE;

    /**
     * Inicializa a base de dados.
     *
     * Verifica se o ficheiro da base de dados existe. Se não existir, ou se as tabelas
     * essenciais estiverem em falta, o método cria a estrutura necessária e insere
     * os dados iniciais (perfis de acesso e utilizador administrador padrão).
     */
    public static void initializeDatabase() {
        File dbFile = new File(DB_FILE);
        boolean needsSetup = !dbFile.exists();

        try (Connection conn = getConnection()) {
            if (conn != null) {
                if (needsSetup) {
                    logger.info("Ficheiro da base de dados não encontrado. A criar novo em: {}", dbFile.getAbsolutePath());
                }
                logger.info("A verificar a estrutura da base de dados...");
                createTablesIfNotExists(conn); // Esta chamada agora não lança mais SQLException
                insertInitialData(conn);
                logger.info("Base de dados inicializada com sucesso.");
            }
        } catch (SQLException e) {
            // Este catch agora lida principalmente com erros de getConnection() ou insertInitialData()
            logger.error("Erro crítico durante a inicialização da base de dados.", e);
        }
    }

    /**
     * Estabelece e retorna uma conexão com a base de dados SQLite.
     * @return Uma instância de {@link Connection}.
     * @throws SQLException se ocorrer um erro ao conectar.
     */
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Cria as tabelas 'roles' e 'users' se elas ainda não existirem.
     * @param conn A conexão ativa com a base de dados.
     */
    private static void createTablesIfNotExists(Connection conn) throws SQLException {
        String createRolesTableSql = """
            CREATE TABLE IF NOT EXISTS roles (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE
            );
        """;

        String createUsersTableSql = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password_hash TEXT NOT NULL,
                role_id INTEGER,
                FOREIGN KEY (role_id) REFERENCES roles (id)
            );
        """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createRolesTableSql);
            logger.debug("Tabela 'roles' verificada/criada.");
            stmt.execute(createUsersTableSql);
            logger.debug("Tabela 'users' verificada/criada.");
        }catch (SQLException e) {
            logger.error("FALHA CRÍTICA AO CRIAR TABELAS DA BASE DE DADOS.", e);
            // Lança uma exceção não verificada para parar a aplicação.
            // Isto é melhor do que continuar com uma base de dados corrompida.
            throw new RuntimeException("Não foi possível criar as tabelas da base de dados. A aplicação não pode continuar.", e);
        }
    }

    /**
     * Insere os perfis de acesso padrão ('ADMINISTRATOR', 'OPERATOR') e um utilizador
     * administrador inicial se eles ainda não existirem.
     * @param conn A conexão ativa com a base de dados.
     */
    private static void insertInitialData(Connection conn) throws SQLException {
        // --- Inserir Perfis (Roles) ---
        String insertRoleSql = "INSERT OR IGNORE INTO roles(name) VALUES(?);";
        try (PreparedStatement pstmt = conn.prepareStatement(insertRoleSql)) {
            pstmt.setString(1, "ADMINISTRATOR");
            pstmt.executeUpdate();
            pstmt.setString(1, "OPERATOR");
            pstmt.executeUpdate();
            logger.debug("Perfis de acesso padrão garantidos.");
        }

        // --- Inserir Utilizador Admin Padrão ---
        String checkAdminSql = "SELECT COUNT(*) FROM users WHERE username = 'admin';";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkAdminSql)) {
            if (rs.getInt(1) == 0) {
                logger.info("Utilizador 'admin' não encontrado. A criar utilizador administrador padrão.");
                String insertAdminSql = "INSERT INTO users(username, password_hash, role_id) VALUES(?, ?, (SELECT id FROM roles WHERE name = ?));";
                try (PreparedStatement pstmt = conn.prepareStatement(insertAdminSql)) {
                    pstmt.setString(1, "admin");
                    String hashedPassword = BCrypt.hashpw("admin", BCrypt.gensalt());
                    pstmt.setString(2, hashedPassword);

                    pstmt.setString(3, "ADMINISTRATOR");
                    pstmt.executeUpdate();
                    logger.info("Utilizador 'admin' com senha padrão criado com sucesso.");
                }
            }
        }
    }
    /**
     * Valida as credenciais de um utilizador contra a base de dados.
     *
     * @param username O nome de utilizador a ser validado.
     * @param plainPassword A senha em texto puro fornecida pelo utilizador.
     * @return O perfil (role) do utilizador se a autenticação for bem-sucedida, caso contrário null.
     */
    public static String validateLogin(String username, String plainPassword) {
        String sql = """
            SELECT u.password_hash, r.name
            FROM users u
            JOIN roles r ON u.role_id = r.id
            WHERE u.username = ?
        """;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                // Compara a senha fornecida com o hash guardado
                if (BCrypt.checkpw(plainPassword, storedHash)) {
                    String role = rs.getString("name");
                    logger.info("Utilizador '{}' autenticado com sucesso com o perfil '{}'.", username, role);
                    return role; // Sucesso na autenticação
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao validar o login para o utilizador: " + username, e);
        }
        logger.warn("Tentativa de login falhada para o utilizador: {}", username);
        return null; // Falha na autenticação
    }
}
