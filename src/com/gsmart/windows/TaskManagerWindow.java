// Localização: src/main/java/com/gsmart/windows/TaskManagerWindow.java
package com.gsmart.windows;

import com.gsmart.TaskStatus;
import com.gsmart.pipeline.PipelineManager;
import com.gsmart.pipeline.PipelineTask;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

        taskTable.getColumnModel().getColumn(1).setCellRenderer(new StatusCellRenderer());

        TableCellRenderer buttonRenderer = new ButtonColumnRenderer();
        TableCellEditor buttonEditor = new ButtonColumnEditor();
        taskTable.getColumnModel().getColumn(2).setCellRenderer(buttonRenderer);
        taskTable.getColumnModel().getColumn(2).setCellEditor(buttonEditor);
        taskTable.getColumnModel().getColumn(3).setCellRenderer(buttonRenderer);
        taskTable.getColumnModel().getColumn(3).setCellEditor(buttonEditor);

        taskTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = taskTable.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    int column = taskTable.columnAtPoint(e.getPoint());
                    if (column < 2) {
                        PipelineTask task = ((TaskManagerTableModel) taskTable.getModel()).getTaskAt(row);
                        if (task.hasAlert()) {
                            task.setHasAlert(false);
                            taskTable.repaint();
                        }
                    }
                }
            }
        });

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

    // --- Início das Classes Internas ---

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
        @Override public Class<?> getColumnClass(int columnIndex) { return getValueAt(0, columnIndex).getClass(); }

        // --- INÍCIO DA CORREÇÃO ---
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            // Permite que as colunas 2 (Visualizar) e 3 (Ação) sejam "editáveis" (clicáveis)
            return columnIndex == 2 || columnIndex == 3;
        }
        // --- FIM DA CORREÇÃO ---

        @Override public Object getValueAt(int rowIndex, int columnIndex) {
            PipelineTask task = tasks.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> task.getDescription();
                case 1 -> task.getStatus();
                case 2 -> "Mostrar Monitor";
                case 3 -> (task.getStatus() == TaskStatus.ERROR) ? "Reiniciar" : "Parar";
                default -> "";
            };
        }

        public PipelineTask getTaskAt(int row) {
            return tasks.get(row);
        }
    }

    private class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            PipelineTask task = ((TaskManagerTableModel) table.getModel()).getTaskAt(row);

            if (task.hasAlert()) {
                setBackground(new Color(255, 255, 224)); // Amarelo claro
            } else {
                setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            }

            if (value instanceof TaskStatus status) {
                setText(status.toString());
                switch (status) {
                    case RUNNING -> setForeground(new Color(0, 128, 0));
                    case ERROR -> setForeground(Color.RED);
                    default -> setForeground(Color.GRAY);
                }
            }
            return this;
        }
    }

    private class ButtonColumnRenderer extends JButton implements TableCellRenderer {
        public ButtonColumnRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(UIManager.getColor("Button.background"));
            }
            setText((value == null) ? "" : value.toString());
            if ("Parar".equals(value)) {
                setForeground(Color.RED);
            } else if ("Reiniciar".equals(value)) {
                setForeground(new Color(0, 100, 0));
            }
            return this;
        }
    }

    private class ButtonColumnEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
        private final JButton button;
        private String label;
        private JTable table;
        private int row;
        private int column;

        public ButtonColumnEditor() {
            button = new JButton();
            button.setOpaque(true);

            button.addActionListener(this);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.label = (value == null) ? "" : value.toString();
            this.table = table;
            this.row = row;
            this.column = column;
            button.setText(label);
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return label;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            fireEditingStopped();
            PipelineTask task = ((TaskManagerTableModel) table.getModel()).getTaskAt(row);

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
    // --- Fim das Classes Internas ---
}