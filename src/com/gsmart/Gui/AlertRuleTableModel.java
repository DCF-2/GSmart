// Localização: src/com/gsmart/gui/AlertRuleTableModel.java
package com.gsmart.Gui;

import com.gsmart.config.AlertRule;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de dados (TableModel) para a JTable que exibe as regras de alerta.
 *
 * Esta classe faz a ponte entre a lista de objetos {@code AlertRule} e a
 * tabela na interface gráfica, controlando como os dados são exibidos,
 * formatados e editados pelo utilizador.
 *
 * @see com.gsmart.config.AlertRule
 * @see javax.swing.table.AbstractTableModel
 */
public class AlertRuleTableModel extends AbstractTableModel {
    private final String[] columnNames = {"Ativa", "Nome da Regra", "Métrica Monitorada", "Condição", "Valor", "MQTT", "Telegram"};
    private List<AlertRule> rules;

    public AlertRuleTableModel() {
        this.rules = new ArrayList<>();
    }

    public List<AlertRule> getRules() {
        return rules;
    }

    public void setRules(List<AlertRule> rules) {
        this.rules = new ArrayList<>(rules);
        fireTableDataChanged();
    }

    public void addRule(AlertRule rule) {
        this.rules.add(rule);
        fireTableRowsInserted(this.rules.size() - 1, this.rules.size() - 1);
    }

    public void updateRule(int rowIndex, AlertRule rule) {
        this.rules.set(rowIndex, rule);
        fireTableRowsUpdated(rowIndex, rowIndex);
    }

    public void removeRule(int rowIndex) {
        this.rules.remove(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }

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
        if (columnIndex == 0 || columnIndex == 5 || columnIndex == 6) return Boolean.class;
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 0 || columnIndex == 5 || columnIndex == 6; // Ativa, MQTT, Telegram
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        AlertRule rule = rules.get(rowIndex);
        switch (columnIndex) {
            case 0: return rule.isEnabled();
            case 1: return rule.getRuleName();
            case 2: return rule.getMetricToWatch();
            case 3: return rule.getCondition().toString();
            case 4: return String.valueOf(rule.getThresholdValue());
            case 5: return rule.isSendToMqtt();
            case 6: return rule.isSendToTelegram();
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
            case 5:
                rule.setSendToMqtt((Boolean) aValue);
                break;
            case 6:
                rule.setSendToTelegram((Boolean) aValue);
                break;
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }
}