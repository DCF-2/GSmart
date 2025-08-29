package main.java.com.gsmart.Gui;

import main.java.com.gsmart.config.MetricConfig;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Renderizador de células customizado para a tabela de métricas.
 *
 * A sua principal função é alterar a aparência (fonte e cor) das métricas
 * que são consideradas "de sistema" (como timestamp), diferenciando-as
 * visualmente das métricas normais para uma melhor legibilidade.
 * @see javax.swing.table.DefaultTableCellRenderer
 */
public class SystemMetricCellRenderer extends DefaultTableCellRenderer {
    private final Font defaultFont = new Font("Segoe UI", Font.PLAIN, 12);
    private final Font systemFont = new Font("Segoe UI", Font.ITALIC, 12);

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        MetricTableModel model = (MetricTableModel) table.getModel();
        MetricConfig metric = model.getMetricAt(row);

        if (metric != null && metric.isSystemMetric()) {
            setFont(systemFont);
            setForeground(Color.BLUE);
        } else {
            setFont(defaultFont);
            setForeground(table.getForeground());
        }

        if (column == 0) {
            setHorizontalAlignment(SwingConstants.CENTER);
        } else {
            setHorizontalAlignment(SwingConstants.LEFT);
        }
        return this;
    }
}