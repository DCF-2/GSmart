// Localização: src/main/java/com/gsmart/windows/TaskManagerWindow.java
package com.gsmart.windows;

import com.gsmart.TaskStatus;
import com.gsmart.pipeline.PipelineManager;
import com.gsmart.pipeline.PipelineTask;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class TaskManagerWindow extends JFrame {

    private final PipelineManager pipelineManager;
    private final TaskManagerTableModel tableModel;

    public TaskManagerWindow(PipelineManager pipelineManager) {
        this.pipelineManager = pipelineManager;
        this.tableModel = new TaskManagerTableModel(pipelineManager.getRunningTasks());

        setTitle("Central de Monitoramento de Pipelines");
        setSize(650, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JTable taskTable = new JTable(tableModel);
        taskTable.setFillsViewportHeight(true);
        taskTable.setRowHeight(30);
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskTable.getColumnModel().getColumn(0).setPreferredWidth(300);

        taskTable.setDefaultRenderer(Object.class, new StatusCellRenderer());
        ButtonColumnRenderer buttonRenderer = new ButtonColumnRenderer();
        taskTable.getColumnModel().getColumn(2).setCellRenderer(buttonRenderer);
        taskTable.getColumnModel().getColumn(3).setCellRenderer(buttonRenderer);

        taskTable.addMouseListener(new JTableButtonMouseListener(taskTable));

        JScrollPane scrollPane = new JScrollPane(taskTable);
        add(scrollPane, BorderLayout.CENTER);

        this.pipelineManager.setOnTaskListUpdated(() -> {
            tableModel.updateTasks(this.pipelineManager.getRunningTasks());
        });

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                TaskManagerWindow.this.pipelineManager.setOnTaskListUpdated(null);
            }
        });
    }

    private class TaskManagerTableModel extends AbstractTableModel {
        private final String[] columnNames = {"Tarefa", "Status", "Visualizar", "Ação"};
        private List<PipelineTask> tasks;

        public TaskManagerTableModel(List<PipelineTask> tasks) {
            this.tasks = new ArrayList<>(tasks);
        }

        public void updateTasks(List<PipelineTask> newTasks) {
            this.tasks = new ArrayList<>(newTasks);
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return tasks.size(); }
        @Override public int getColumnCount() { return columnNames.length; }
        @Override public String getColumnName(int column) { return columnNames[column]; }

        @Override public Object getValueAt(int rowIndex, int columnIndex) {
            PipelineTask task = tasks.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> task.getDescription();
                case 1 -> task.getStatus();
                case 2 -> "Mostrar Monitor";
                case 3 -> (task.getStatus() == TaskStatus.ERROR) ? "Reiniciar" : "Parar";
                default -> null;
            };
        }

        public PipelineTask getTaskAt(int row) {
            return tasks.get(row);
        }
    }

    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof TaskStatus status) {
                switch (status) {
                    case RUNNING -> c.setForeground(new Color(0, 128, 0));
                    case ERROR -> c.setForeground(Color.RED);
                    default -> c.setForeground(Color.GRAY);
                }
            } else {
                c.setForeground(table.getForeground());
            }
            return c;
        }
    }

    private static class ButtonColumnRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JButton button = new JButton((String) value);
            if (column == 3 && value.equals("Parar")) {
                button.setForeground(Color.RED);
            }
            if (column == 3 && value.equals("Reiniciar")) {
                button.setForeground(new Color(0, 100, 0));
            }
            return button;
        }
    }

    private class JTableButtonMouseListener extends MouseAdapter {
        private final JTable table;
        public JTableButtonMouseListener(JTable table) { this.table = table; }

        public void mouseClicked(MouseEvent e) {
            int column = table.getColumnModel().getColumnIndexAtX(e.getX());
            int row = e.getY() / table.getRowHeight();
            if (row < table.getRowCount() && row >= 0 && column >= 2 && column <= 3) {
                PipelineTask task = ((TaskManagerTableModel)table.getModel()).getTaskAt(row);
                if (column == 2) {
                    pipelineManager.showMonitorFor(task);
                } else if (column == 3) {
                    if (task.getStatus() == TaskStatus.ERROR) {
                        pipelineManager.relaunchPipeline(task);
                    } else {
                        task.stop();
                    }
                }
            }
        }
    }
}