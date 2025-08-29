package main.java.com.gsmart.Gui;

import main.java.com.gsmart.config.InsightRule;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de dados (TableModel) para a JTable que exibe as regras de alarme.
 *
 * Esta classe faz a ponte entre a lista de objetos {@code InsightRule} e a
 * tabela na interface gráfica, controlando como os dados são exibidos,
 * formatados e editados pelo utilizador.
 *
 * @see main.java.com.gsmart.config.InsightRule
 * @see javax.swing.table.AbstractTableModel
 */
public class InsightRuleTableModel extends AbstractTableModel {
    private final String[] columnNames = {"Ativa", "Nome da Regra", "Métrica Monitorada", "Condição", "Valor", "MQTT", "Telegram", "Tipo de Alarme", "Mensagem Gerada"};
    private List<InsightRule> rules;

    public InsightRuleTableModel() {
        this.rules = new ArrayList<>();
    }

    public List<InsightRule> getRules() {
        return rules;
    }

    public void setRules(List<InsightRule> rules) {
        this.rules = new ArrayList<>(rules);
        fireTableDataChanged();
    }

    public void addRule(InsightRule rule) {
        this.rules.add(rule);
        fireTableRowsInserted(this.rules.size() - 1, this.rules.size() - 1);
    }

    public void updateRule(int rowIndex, InsightRule rule) {
        this.rules.set(rowIndex, rule);
        fireTableRowsUpdated(rowIndex, rowIndex);
    }

    public void removeRule(int rowIndex) {
        this.rules.remove(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }

    public InsightRule getRuleAt(int rowIndex) {
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
        return columnIndex == 0 || columnIndex == 5 || columnIndex == 6;
    }


    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        InsightRule rule = rules.get(rowIndex);
        switch (columnIndex) {
            case 0: return rule.isEnabled();
            case 1: return rule.getRuleName();
            case 2: return rule.getMetricToWatch();
            case 3: return rule.getCondition().toString();
            case 4: return String.valueOf(rule.getThresholdValue());
            case 5: return rule.isSendToMqtt();
            case 6: return rule.isSendToTelegram();
            case 7: return rule.getInsightType();
            case 8: return rule.getMessageToSend();
            default: return null;
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            InsightRule rule = rules.get(rowIndex);
            // --- SUBSTITUA O CONTEÚDO DO MÉTODO POR ESTE ---
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
}