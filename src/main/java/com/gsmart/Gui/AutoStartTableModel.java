// Localização: src/com/gsmart/Gui/AutoStartTableModel.java
package main.java.com.gsmart.Gui;

import main.java.com.gsmart.config.SerializablePipelineConfig;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Modelo de dados (TableModel) para a JTable que exibe as pipelines
 * configuradas para o início automático.
 * <p>
 * Esta classe adapta a lista de objetos {@link main.java.com.gsmart.config.SerializablePipelineConfig}
 * para serem exibidos numa tabela, mostrando informações descritivas sobre a fonte
 * e o destino de cada pipeline guardada.
 *
 * @see main.java.com.gsmart.config.SerializablePipelineConfig
 * @see main.java.com.gsmart.Gui.windows.AutoStartManagerWindow
 */
public class AutoStartTableModel extends AbstractTableModel {

    private final String[] columnNames = {"Fonte da Pipeline", "Destino"};
    private List<SerializablePipelineConfig> pipelineConfigs;

    public AutoStartTableModel() {
        this.pipelineConfigs = new ArrayList<>();
    }

    /**
     * Substitui a lista de configurações de pipeline atual por uma nova e notifica a tabela.
     *
     * @param configs A nova lista de {@link SerializablePipelineConfig} a ser exibida.
     */
    public void setPipelineConfigs(List<SerializablePipelineConfig> configs) {
        this.pipelineConfigs = new ArrayList<>(configs);
        fireTableDataChanged(); // Notifica a tabela que os dados mudaram
    }

    public List<SerializablePipelineConfig> getPipelineConfigs() {
        return pipelineConfigs;
    }

    public SerializablePipelineConfig getConfigAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < pipelineConfigs.size()) {
            return pipelineConfigs.get(rowIndex);
        }
        return null;
    }

    /**
     * Remove a configuração de pipeline de uma linha específica e notifica a tabela.
     *
     * @param rowIndex O índice da linha (configuração) a ser removida.
     */
    public void removeRow(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < pipelineConfigs.size()) {
            pipelineConfigs.remove(rowIndex);
            fireTableRowsDeleted(rowIndex, rowIndex);
        }
    }

    @Override
    public int getRowCount() {
        return pipelineConfigs.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        SerializablePipelineConfig config = pipelineConfigs.get(rowIndex);
        Map<String, String> params = config.getDataSourceParams();

        switch (columnIndex) {
            case 0:
                // Tenta criar um nome descritivo para a fonte
                String dataSourceType = config.getDataSourceType();
                if ("ThingsBoard".equals(dataSourceType)) {
                    return "ThingsBoard: " + params.getOrDefault("deviceName", params.get("deviceId"));
                } else if ("Database".equals(dataSourceType)) {
                    return "Banco de Dados: " + params.getOrDefault("tableName", "Tabela Desconhecida");
                }
                return "Fonte Desconhecida";
            case 1:
                // Mostra o tipo de destino
                return config.getDestinationType().toString();
            default:
                return null;
        }
    }


}