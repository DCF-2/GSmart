// Localização: src/main/java/com/gsmart/sources/DatabaseSource.java
package com.gsmart.sources;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gsmart.resources.IDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseSource implements IDataSource {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseSource.class);

    private final String dbUrl;
    private final String user;
    private final String password;
    private final String tableName;
    private final List<String> selectedColumns;

    public DatabaseSource(String dbUrl, String user, String password, String tableName, List<String> selectedColumns) {
        this.dbUrl = dbUrl;
        this.user = user;
        this.password = password;
        this.tableName = tableName;
        this.selectedColumns = selectedColumns;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, user, password);
    }

    /**
     * Tenta estabelecer uma conexão com o banco de dados para validar as credenciais.
     * @return true se a conexão for bem-sucedida, false caso contrário.
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            boolean isValid = conn.isValid(5); // Timeout de 5 segundos
            logger.info("Teste de conexão com o banco de dados bem-sucedido.");
            return isValid;
        } catch (SQLException e) {
            logger.error("Falha no teste de conexão com o banco de dados: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Busca e retorna uma lista com os nomes de todas as tabelas visíveis no schema 'public'.
     * @return Uma lista de nomes de tabelas.
     * @throws SQLException se ocorrer um erro de acesso ao banco.
     */
    public List<String> getAvailableTables() throws SQLException {
        logger.info("Buscando tabelas disponíveis no banco de dados...");
        List<String> tables = new ArrayList<>();
        try (Connection conn = getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            // Busca tabelas do schema 'public', que é o padrão no PostgreSQL
            try (ResultSet rs = metaData.getTables(null, "public", "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME"));
                }
            }
        }
        logger.info("Encontradas {} tabelas.", tables.size());
        return tables;
    }

    @Override
    public JsonObject fetchData() throws Exception {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalStateException("O nome da tabela não foi especificado.");
        }
        if (selectedColumns == null || selectedColumns.isEmpty()) {
            throw new IllegalStateException("Nenhuma coluna foi selecionada para busca.");
        }

        String columnsToSelect = String.join(", ", selectedColumns);
        String query = String.format("SELECT %s FROM \"%s\" ORDER BY timestamp DESC LIMIT 1", columnsToSelect, tableName);

        logger.debug("Executando query no banco de dados: {}", query);
        JsonObject data = new JsonObject();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                ResultSetMetaData rsmd = rs.getMetaData();
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    String colName = rsmd.getColumnName(i);
                    Object value = rs.getObject(i);

                    JsonArray valueArray = new JsonArray();
                    JsonObject valueObject = new JsonObject();
                    valueObject.addProperty("ts", System.currentTimeMillis());

                    if (value instanceof Number num) {
                        valueObject.addProperty("value", num);
                    } else if (value instanceof Boolean bool) {
                        valueObject.addProperty("value", bool);
                    } else if (value != null) {
                        valueObject.addProperty("value", value.toString());
                    } else {
                        valueObject.add("value", null);
                    }
                    valueArray.add(valueObject);
                    data.add(colName, valueArray);
                }
            } else {
                logger.warn("Nenhum dado encontrado no banco de dados com a query: {}", query);
            }
        }
        return data;
    }

    public List<String> getAvailableColumns(String tableName) throws SQLException {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new SQLException("Nome da tabela não pode ser nulo ou vazio para buscar colunas.");
        }
        logger.info("Buscando metadados da tabela: {}", tableName);
        List<String> columnNames = new ArrayList<>();
        String query = String.format("SELECT * FROM \"%s\" LIMIT 1", tableName);
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                columnNames.add(metaData.getColumnName(i));
            }
        }
        if (columnNames.isEmpty()) {
            throw new SQLException("Tabela '" + tableName + "' não encontrada ou não possui colunas.");
        }
        return columnNames;
    }

    @Override
    public String getSourceName() {
        return "Banco de Dados (Tabela: " + this.tableName + ")";
    }

    public void testConnectionAndThrow() throws SQLException {
        try (Connection conn = getConnection()) {
            if (!conn.isValid(5)) {
                throw new SQLException("Não foi possível validar a conexão com o banco de dados (timeout).");
            }
            logger.info("Teste de conexão com o banco de dados bem-sucedido.");
        } catch (SQLException e) {
            logger.error("Falha no teste de conexão com o banco de dados: {}", e.getMessage());
            throw e; // Re-lança a exceção para ser capturada pela GSmartGui
        }
    }
}