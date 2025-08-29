package main.java.com.gsmart.Gui;

import main.java.com.gsmart.config.MetricConfig;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Modelo de dados (TableModel) para a JTable que exibe as métricas.
 *
 * Esta classe interna gerencia a lista de objetos {@code MetricConfig}, controlando
 * quais dados são exibidos na tabela e como eles podem ser editados pelo utilizador
 * (seleção, alias e expressões).
 * @see javax.swing.table.AbstractTableModel
 * @see main.java.com.gsmart.config.MetricConfig
 */
public class MetricTableModel extends AbstractTableModel {
    private final String[] columnNames = {"Enviar", "Nome Original", "Enviar Como (Alias)", "Função/Expressão (usar 'valor')"};
    private List<MetricConfig> metrics = new ArrayList<>();

    public List<MetricConfig> getSelectedMetrics() {
        return metrics.stream().filter(MetricConfig::isSelected).collect(Collectors.toList());
    }
    public void setMetrics(List<MetricConfig> metrics) {
        this.metrics = new ArrayList<>(metrics);
        fireTableDataChanged();
    }
    public void clearMetrics() {
        this.metrics.clear();
        fireTableDataChanged();
    }
    @Override public int getRowCount() { return metrics.size(); }
    @Override public int getColumnCount() { return columnNames.length; }
    @Override public String getColumnName(int column) { return columnNames[column]; }
    @Override public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) return Boolean.class;
        return String.class;
    }
    @Override public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            MetricConfig metric = metrics.get(rowIndex);
            return !metric.isSystemMetric();
        }
        return columnIndex == 2 || columnIndex == 3;
    }
    public MetricConfig getMetricAt(int row) {
        if (row >= 0 && row < metrics.size()) {
            return metrics.get(row);
        }
        return null;
    }
    @Override public Object getValueAt(int rowIndex, int columnIndex) {
        MetricConfig metric = metrics.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> metric.isSelected();
            case 1 -> metric.getOriginalName();
            case 2 -> metric.getAlias();
            case 3 -> metric.getExpression();
            default -> null;
        };
    }
    @Override public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        MetricConfig metric = metrics.get(rowIndex);
        switch (columnIndex) {
            case 0 -> metric.setSelected((Boolean) aValue);
            case 2 -> metric.setAlias((String) aValue);
            case 3 -> metric.setExpression((String) aValue);
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    /**
     * Retorna uma cópia de todas as configurações de métricas atuais na tabela.
     * @return Uma nova lista contendo as métricas.
     */
    public List<MetricConfig> getAllMetrics() {
        return new ArrayList<>(this.metrics);
    }
}