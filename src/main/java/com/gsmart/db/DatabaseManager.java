// Localização: src/com/gsmart/db/DatabaseManager.java
package main.java.com.gsmart.db;

import main.java.com.gsmart.model.User;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestor central para todas as operações da base de dados da aplicação GSmart.
 *
 * Esta classe é responsável por inicializar a base de dados SQLite, criar o schema
 * necessário (tabelas de utilizadores e perfis) e fornecer métodos para autenticar,
 * criar, ler, atualizar e remover utilizadores.
 *
 * Utiliza o padrão Singleton para garantir que existe apenas uma instância
 * a gerir a conexão com a base de dados durante todo o ciclo de vida da aplicação.
 */
public class DatabaseManager {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static final String DATA_DIRECTORY = "data/db";
    private static final String DB_FILE = DATA_DIRECTORY + "/gsmart_users.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE;

    // Instância única (Singleton) ---
    private static final DatabaseManager instance = new DatabaseManager();

    // Construtor privado para impedir a criação de novas instâncias
    private DatabaseManager() {
        try {
            Files.createDirectories(Paths.get(DATA_DIRECTORY));
        } catch (IOException e) {
            logger.error("Falha crítica ao criar o diretório da base de dados.", e);
        }
        initializeDatabase();
    }

    // Método público para aceder à instância única
    public static DatabaseManager getInstance() {
        return instance;
    }

    // --- ALTERAÇÃO 2: O método 'initializeDatabase' agora é privado e não estático ---
    private void initializeDatabase() {
        File dbFile = new File(DB_FILE);
        boolean needsSetup = !dbFile.exists();

        try (Connection conn = getConnection()) {
            if (conn != null) {
                if (needsSetup) {
                    logger.info("Ficheiro da base de dados não encontrado. A criar novo em: {}", dbFile.getAbsolutePath());
                }
                logger.info("A verificar a estrutura da base de dados...");
                createTablesIfNotExists(conn);
                insertInitialData(conn);
                logger.info("Base de dados inicializada com sucesso.");
            }
        } catch (SQLException e) {
            logger.error("Erro crítico durante a inicialização da base de dados.", e);
            throw new RuntimeException("Não foi possível inicializar a base de dados.", e);
        }
    }

    // --- ALTERAÇÃO 3: Todos os métodos públicos agora NÃO são estáticos ---

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private void createTablesIfNotExists(Connection conn) throws SQLException {
        String createRolesTableSql = "CREATE TABLE IF NOT EXISTS roles (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE);";
        String createUsersTableSql = "CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT NOT NULL UNIQUE, password_hash TEXT NOT NULL, role_id INTEGER, FOREIGN KEY (role_id) REFERENCES roles (id));";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createRolesTableSql);
            stmt.execute(createUsersTableSql);
        }
    }

    private void insertInitialData(Connection conn) throws SQLException {
        String insertRoleSql = "INSERT OR IGNORE INTO roles(name) VALUES(?);";
        try (PreparedStatement pstmt = conn.prepareStatement(insertRoleSql)) {
            pstmt.setString(1, "ADMINISTRATOR");
            pstmt.executeUpdate();
            pstmt.setString(1, "OPERATOR");
            pstmt.executeUpdate();
        }

        String checkAdminSql = "SELECT COUNT(*) FROM users WHERE username = 'admin';";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkAdminSql)) {
            if (rs.getInt(1) == 0) {
                String insertAdminSql = "INSERT INTO users(username, password_hash, role_id) VALUES(?, ?, (SELECT id FROM roles WHERE name = ?));";
                try (PreparedStatement pstmt = conn.prepareStatement(insertAdminSql)) {
                    pstmt.setString(1, "admin");
                    pstmt.setString(2, BCrypt.hashpw("admin", BCrypt.gensalt()));
                    pstmt.setString(3, "ADMINISTRATOR");
                    pstmt.executeUpdate();
                    logger.info("Utilizador 'admin' com senha padrão criado com sucesso.");
                }
            }
        }
    }

    /**
     * Valida as credenciais de um utilizador contra a base de dados.
     * <p>
     * Compara o hash da senha fornecida (usando BCrypt) com o hash armazenado na
     * base de dados para o nome de utilizador especificado.
     *
     * @param username O nome de utilizador a ser validado.
     * @param plainPassword A senha em texto puro a ser verificada.
     * @return O perfil de acesso do utilizador (ex: "ADMINISTRATOR") se a autenticação
     * for bem-sucedida; {@code null} caso contrário.
     */
    public String validateLogin(String username, String plainPassword) {
        String sql = "SELECT u.password_hash, r.name FROM users u JOIN roles r ON u.role_id = r.id WHERE u.username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                if (BCrypt.checkpw(plainPassword, rs.getString("password_hash"))) {
                    String role = rs.getString("name");
                    logger.info("Utilizador '{}' autenticado com sucesso com o perfil '{}'.", username, role);
                    return role;
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao validar o login para o utilizador: " + username, e);
        }
        logger.warn("Tentativa de login falhada para o utilizador: {}", username);
        return null;
    }

    /**
     * Busca e retorna uma lista de todos os utilizadores registados na base de dados.
     * <p>
     * As informações incluem o ID do utilizador, o nome de utilizador e o seu perfil de acesso.
     * A senha (hash) não é incluída por razões de segurança.
     *
     * @return Uma lista de objetos {@link main.java.com.gsmart.model.User} representando
     * todos os utilizadores, ordenada por nome de utilizador.
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT u.id, u.username, r.name AS role FROM users u JOIN roles r ON u.role_id = r.id ORDER BY u.username";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(new User(rs.getInt("id"), rs.getString("username"), rs.getString("role")));
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar a lista de utilizadores.", e);
        }
        return users;
    }

    /**
     * Adiciona um novo utilizador à base de dados.
     * <p>
     * Antes de inserir, verifica se o nome de utilizador já existe para evitar duplicados.
     * A senha fornecida é transformada num hash seguro usando BCrypt antes de ser armazenada.
     *
     * @param username O nome de utilizador único para o novo utilizador.
     * @param plainPassword A senha em texto puro para o novo utilizador.
     * @param role O perfil de acesso a ser atribuído (ex: "OPERATOR").
     * @return {@code true} se o utilizador for adicionado com sucesso; {@code false} se o
     * nome de utilizador já existir ou se ocorrer um erro na base de dados.
     */
    public boolean addUser(String username, String plainPassword, String role) {
        if (userExists(username)) {
            logger.error("Tentativa de adicionar um utilizador que já existe: {}", username);
            return false;
        }
        String sql = "INSERT INTO users(username, password_hash, role_id) VALUES(?, ?, (SELECT id FROM roles WHERE name = ?))";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, BCrypt.hashpw(plainPassword, BCrypt.gensalt()));
            pstmt.setString(3, role);
            pstmt.executeUpdate();
            logger.info("Novo utilizador '{}' com perfil '{}' adicionado com sucesso.", username, role);
            return true;
        } catch (SQLException e) {
            logger.error("Erro ao adicionar o novo utilizador: " + username, e);
            return false;
        }
    }

    private boolean userExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            logger.error("Erro ao verificar a existência do utilizador: " + username, e);
        }
        return true; // Assumir que existe em caso de erro para evitar duplicados
    }

    /**
     * Atualiza as informações de um utilizador existente, exceto a sua senha.
     *
     * @param userId O ID do utilizador a ser atualizado.
     * @param newUsername O novo nome de utilizador.
     * @param newRole O novo perfil de acesso.
     * @return {@code true} se a atualização for bem-sucedida; {@code false} caso contrário.
     */
    public boolean updateUser(int userId, String newUsername, String newRole) {
        String sql = "UPDATE users SET username = ?, role_id = (SELECT id FROM roles WHERE name = ?) WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newUsername);
            pstmt.setString(2, newRole);
            pstmt.setInt(3, userId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.error("Erro ao atualizar o utilizador com ID: " + userId, e);
            return false;
        }
    }

    public boolean updateUserPassword(int userId, String newPlainPassword) {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, BCrypt.hashpw(newPlainPassword, BCrypt.gensalt()));
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.error("Erro ao atualizar a senha para o utilizador com ID: " + userId, e);
            return false;
        }
    }

    /**
     * Remove um utilizador da base de dados.
     * <p>
     * Inclui uma verificação de segurança para impedir a remoção do utilizador "admin" principal,
     * garantindo que a aplicação não fique bloqueada sem um administrador.
     *
     * @param userId O ID do utilizador a ser removido.
     * @return {@code true} se o utilizador for removido com sucesso; {@code false} se o
     * utilizador for o "admin" ou se ocorrer um erro.
     */
    public boolean removeUser(int userId) {
        String checkAdminSql = "SELECT username FROM users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkAdminSql)) {
            checkStmt.setInt(1, userId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && "admin".equalsIgnoreCase(rs.getString("username"))) {
                JOptionPane.showMessageDialog(null, "O utilizador 'admin' principal não pode ser removido.", "Ação Proibida", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (SQLException e) {
            logger.error("Erro ao verificar o utilizador antes de remover (ID: " + userId + ")", e);
            return false;
        }
        String deleteSql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
            deleteStmt.setInt(1, userId);
            return deleteStmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erro ao remover o utilizador com ID: " + userId, e);
        }
        return false;
    }
}