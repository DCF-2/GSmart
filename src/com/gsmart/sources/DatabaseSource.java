// Localização: src/main/java/com/gsmart/sources/DatabaseSource.java
package com.gsmart.sources;

import com.google.gson.JsonObject;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseSource implements IDataSource {
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

    @Override
    public JsonObject fetchData() throws Exception {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalStateException("O nome da tabela não foi especificado.");
        }
        if (selectedColumns == null || selectedColumns.isEmpty()) {
            throw new IllegalStateException("Nenhuma coluna foi selecionada para busca.");
        }

        // Constrói a query dinamicamente com as colunas selecionadas
        String columns = String.join(", ", selectedColumns);
        String query = String.format("SELECT %s FROM %s ORDER BY timestamp DESC LIMIT 1", columns, tableName);

        System.out.println("Executando query no banco de dados: " + query);
        JsonObject data = new JsonObject();

        try (Connection conn = DriverManager.getConnection(dbUrl, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                // Mapeia os resultados para o JsonObject
                // Este loop genérico adiciona todas as colunas retornadas ao JSON
                ResultSetMetaData rsmd = rs.getMetaData();
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    String colName = rsmd.getColumnName(i);
                    // O Power BI pode ter dificuldade com nomes de coluna em maiúsculas
                    // O ideal é padronizar para os mesmos nomes do ThingsBoard se possível
                    // Ex: "Ptot", "NSerie", etc.
                    Object value = rs.getObject(i);
                    if (value instanceof Number) {
                        data.addProperty(colName, (Number) value);
                    } else if (value instanceof String) {
                        data.addProperty(colName, (String) value);
                    } else if (value != null) {
                        data.addProperty(colName, value.toString());
                    }
                }
            } else {
                throw new Exception("Nenhum dado encontrado no banco de dados com a query: " + query);
            }
        }
        return data;
    }

    /**
     * NOVO MÉTODO: Conecta ao banco e obtém a lista de nomes de colunas de uma tabela.
     */
    public List<String> getAvailableColumns(String tableName) throws Exception {
        System.out.println("Buscando metadados da tabela: " + tableName);
        List<String> columnNames = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(dbUrl, user, password)) {
            DatabaseMetaData metaData = conn.getMetaData();
            // O null nos primeiros parâmetros significa que não estamos filtrando por catálogo ou esquema
            try (ResultSet columns = metaData.getColumns(null, null, tableName, null)) {
                while (columns.next()) {
                    columnNames.add(columns.getString("COLUMN_NAME"));
                }
            }
        }
        if (columnNames.isEmpty()) {
            throw new Exception("Tabela '" + tableName + "' não encontrada ou não possui colunas.");
        }
        return columnNames;
    }

    @Override
    public String getSourceName() {
        return "Banco de Dados Espelho (Tabela: " + this.tableName + ")";
    }
}