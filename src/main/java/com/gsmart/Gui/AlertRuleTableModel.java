// Localização: src/com/gsmart/gui/AlertRuleTableModel.java
package main.java.com.gsmart.Gui;

import main.java.com.gsmart.config.AlertRule;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de dados (TableModel) para a JTable que exibe as regras de alerta.
 * <p>
 * Esta classe faz a ponte entre a lista de objetos {@link main.java.com.gsmart.config.AlertRule} e a
 * tabela na interface gráfica, controlando como os dados são exibidos,
 * formatados e editados pelo utilizador.
 *
 * @see main.java.com.gsmart.config.AlertRule
 * @see javax.swing.table.AbstractTableModel
 */
public class AlertRuleTableModel extends AbstractTableModel {
    private final String[] columnNames = {"Ativa", "Nome da Regra", "Categoria", "Métrica Monitorada", "Condição", "Valor", "MQTT", "Telegram"};
    private List<AlertRule> rules;

    public AlertRuleTableModel() {
        this.rules = new ArrayList<>();
    }

    public List<AlertRule> getRules() {
        return rules;
    }

    /**
     * Substitui a lista de regras atual por uma nova e notifica a tabela para se redesenhar.
     *
     * @param rules A nova lista de {@link AlertRule} a ser exibida.
     */
    public void setRules(List<AlertRule> rules) {
        this.rules = new ArrayList<>(rules);
        fireTableDataChanged();
    }

    /**
     * Adiciona uma nova regra ao final da lista e notifica a tabela.
     *
     * @param rule A {@link AlertRule} a ser adicionada.
     */
    public void addRule(AlertRule rule) {
        this.rules.add(rule);
        fireTableRowsInserted(this.rules.size() - 1, this.rules.size() - 1);
    }

    /**
     * Atualiza uma regra existente numa linha específica e notifica a tabela.
     *
     * @param rowIndex O índice da linha (regra) a ser atualizada.
     * @param rule A {@link AlertRule} com os novos dados.
     */
    public void updateRule(int rowIndex, AlertRule rule) {
        this.rules.set(rowIndex, rule);
        fireTableRowsUpdated(rowIndex, rowIndex);
    }

    /**
     * Remove a regra de uma linha específica e notifica a tabela.
     *
     * @param rowIndex O índice da linha (regra) a ser removida.
     */
    public void removeRule(int rowIndex) {
        this.rules.remove(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }

    /**
     * Retorna o objeto de regra de uma linha específica da tabela.
     *
     * @param rowIndex O índice da linha da qual obter a regra.
     * @return O objeto {@link AlertRule} correspondente.
     */
    public AlertRule getRuleAt(int rowIndex) {
        return rules.get(rowIndex);
    }

    @Override
    public int getRowCount() { return rules.size(); }

    @Override
    public int getColumnCount() { return columnNames.length; }

    @Override
    public String getColumnName(int column) { return columnNames[column]; }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0 || columnIndex == 6 || columnIndex == 7) return Boolean.class;
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 0 || columnIndex == 6 || columnIndex == 7; // Ativa, MQTT, Telegram
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        AlertRule rule = rules.get(rowIndex);
        switch (columnIndex) {
            case 0: return rule.isEnabled();
            case 1: return rule.getRuleName();
            case 2: return rule.getCategory();
            case 3: return rule.getMetricToWatch();
            case 4: return rule.getCondition().toString();
            case 5: return String.valueOf(rule.getThresholdValue());
            case 6: return rule.isSendToMqtt();
            case 7: return rule.isSendToTelegram();
            default: return null;
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        AlertRule rule = rules.get(rowIndex);
        switch (columnIndex) {
            case 0:
                rule.setEnabled((Boolean) aValue);
                break;
            case 6: // Coluna MQTT
                rule.setSendToMqtt((Boolean) aValue);
                break;
            case 7: // Coluna Telegram
                rule.setSendToTelegram((Boolean) aValue);
                break;
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }
    }