// Localização: src/com/gsmart/db/DatabaseManager.java
package com.gsmart.db;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
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

    /**
     * Busca e retorna uma lista de todos os utilizadores registados na base de dados.
     *
     * @return Uma lista de objetos {@link com.gsmart.model.User}.
     */
    public static java.util.List<com.gsmart.model.User> getAllUsers() {
        java.util.List<com.gsmart.model.User> users = new java.util.ArrayList<>();
        String sql = """
        SELECT u.id, u.username, r.name AS role
        FROM users u
        JOIN roles r ON u.role_id = r.id
        ORDER BY u.username
    """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(new com.gsmart.model.User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("role")
                ));
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar a lista de utilizadores.", e);
        }
        return users;
    }

    /**
     * Adiciona um novo utilizador à base de dados.
     * A senha é automaticamente encriptada (hashed) antes de ser guardada.
     *
     * @param username O nome de utilizador.
     * @param plainPassword A senha em texto puro.
     * @param role O perfil de acesso ("ADMINISTRATOR" ou "OPERATOR").
     * @return {@code true} se o utilizador for adicionado com sucesso, {@code false} em caso de erro (ex: nome de utilizador já existe).
     */
    public static boolean addUser(String username, String plainPassword, String role) {
        String sql = "INSERT INTO users(username, password_hash, role_id) VALUES(?, ?, (SELECT id FROM roles WHERE name = ?))";

        // Verifica se o utilizador já existe para evitar erros de constraint UNIQUE
        if (userExists(username)) {
            logger.error("Tentativa de adicionar um utilizador que já existe: {}", username);
            return false;
        }

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String hashedPassword = org.mindrot.jbcrypt.BCrypt.hashpw(plainPassword, org.mindrot.jbcrypt.BCrypt.gensalt());

            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, role);
            pstmt.executeUpdate();
            logger.info("Novo utilizador '{}' com perfil '{}' adicionado com sucesso.", username, role);
            return true;

        } catch (SQLException e) {
            logger.error("Erro ao adicionar o novo utilizador: " + username, e);
            return false;
        }
    }

    /**
     * Verifica se um nome de utilizador já existe na base de dados.
     * @param username O nome de utilizador a verificar.
     * @return {@code true} se o utilizador existe, {@code false} caso contrário.
     */
    private static boolean userExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.error("Erro ao verificar a existência do utilizador: " + username, e);
        }
        return false; // Assumir que não existe em caso de erro
    }

    /**
     * Atualiza as informações de um utilizador existente (exceto a senha).
     *
     * @param userId O ID do utilizador a ser atualizado.
     * @param newUsername O novo nome de utilizador.
     * @param newRole O novo perfil de acesso.
     * @return {@code true} se a atualização for bem-sucedida, {@code false} caso contrário.
     */
    public static boolean updateUser(int userId, String newUsername, String newRole) {
        String sql = "UPDATE users SET username = ?, role_id = (SELECT id FROM roles WHERE name = ?) WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newUsername);
            pstmt.setString(2, newRole);
            pstmt.setInt(3, userId);
            pstmt.executeUpdate();
            logger.info("Utilizador com ID {} atualizado para username '{}' e perfil '{}'.", userId, newUsername, newRole);
            return true;
        } catch (SQLException e) {
            logger.error("Erro ao atualizar o utilizador com ID: " + userId, e);
            return false;
        }
    }

    /**
     * Atualiza a senha de um utilizador específico.
     *
     * @param userId O ID do utilizador cuja senha será alterada.
     * @param newPlainPassword A nova senha em texto puro.
     * @return {@code true} se a senha for atualizada com sucesso, {@code false} caso contrário.
     */
    public static boolean updateUserPassword(int userId, String newPlainPassword) {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String hashedPassword = org.mindrot.jbcrypt.BCrypt.hashpw(newPlainPassword, org.mindrot.jbcrypt.BCrypt.gensalt());
            pstmt.setString(1, hashedPassword);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
            logger.info("Senha para o utilizador com ID {} foi atualizada com sucesso.", userId);
            return true;
        } catch (SQLException e) {
            logger.error("Erro ao atualizar a senha para o utilizador com ID: " + userId, e);
            return false;
        }
    }

    /**
     * Remove um utilizador da base de dados com base no seu ID.
     * Impede a remoção do utilizador "admin" por segurança.
     *
     * @param userId O ID do utilizador a ser removido.
     * @return {@code true} se o utilizador for removido com sucesso, {@code false} caso contrário.
     */
    public static boolean removeUser(int userId) {
        // Medida de segurança: verifica se o utilizador a ser removido não é o admin principal.
        String checkAdminSql = "SELECT username FROM users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkAdminSql)) {
            checkStmt.setInt(1, userId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && "admin".equalsIgnoreCase(rs.getString("username"))) {
                logger.warn("Tentativa de remover o utilizador 'admin' principal. Ação bloqueada.");
                // Retorna um erro específico para a interface poder tratar.
                JOptionPane.showMessageDialog(null, "O utilizador 'admin' principal não pode ser removido.", "Ação Proibida", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (SQLException e) {
            logger.error("Erro ao verificar o utilizador antes de remover (ID: " + userId + ")", e);
            return false;
        }

        // Se não for o admin, prossegue com a remoção.
        String deleteSql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
            deleteStmt.setInt(1, userId);
            int rowsAffected = deleteStmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Utilizador com ID {} removido com sucesso.", userId);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Erro ao remover o utilizador com ID: " + userId, e);
        }
        return false;
    }
}
