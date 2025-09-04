package main.java.com.gsmart.Gui;

import main.java.com.gsmart.config.MetricConfig;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Modelo de dados (TableModel) para a JTable que exibe as métricas disponíveis.
 * <p>
 * Esta classe gere a lista de objetos {@link main.java.com.gsmart.config.MetricConfig}, controlando
 * quais dados são exibidos na tabela e como eles podem ser editados pelo utilizador
 * (seleção, alias e expressões matemáticas).
 *
 * @see javax.swing.table.AbstractTableModel
 * @see main.java.com.gsmart.config.MetricConfig
 */
public class MetricTableModel extends AbstractTableModel {
    private final String[] columnNames = {"Enviar", "Nome Original", "Enviar Como (Alias)", "Função/Expressão (usar 'valor')"};
    private List<MetricConfig> metrics = new ArrayList<>();

    /**
     * Retorna uma lista contendo apenas as métricas que foram selecionadas pelo utilizador.
     * <p>
     * Filtra a lista interna de métricas para incluir apenas aquelas em que a caixa de
     * seleção "Enviar" está marcada.
     *
     * @return Uma lista de {@link MetricConfig} selecionadas.
     */
    public List<MetricConfig> getSelectedMetrics() {
        return metrics.stream().filter(MetricConfig::isSelected).collect(Collectors.toList());
    }

    /**
     * Substitui a lista de métricas atual por uma nova e notifica a tabela para se redesenhar.
     *
     * @param metrics A nova lista de {@link MetricConfig} a ser exibida.
     */
    public void setMetrics(List<MetricConfig> metrics) {
        this.metrics = new ArrayList<>(metrics);
        fireTableDataChanged();
    }

    /**
     * Remove todas as métricas do modelo e notifica a tabela.
     * <p>
     * Este método é normalmente chamado quando a fonte de dados é alterada, limpando
     * a tabela antes de carregar as novas métricas.
     */
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

    /**
     * Retorna o objeto de configuração de métrica de uma linha específica da tabela.
     *
     * @param row O índice da linha da qual obter a configuração.
     * @return O objeto {@link MetricConfig} correspondente, ou {@code null} se o
     * índice da linha for inválido.
     */
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
     * Retorna uma cópia de todas as configurações de métricas atualmente no modelo.
     *
     * @return Uma nova lista contendo todas as métricas.
     */
    public List<MetricConfig> getAllMetrics() {
        return new ArrayList<>(this.metrics);
    }
}