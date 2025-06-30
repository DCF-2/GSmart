// Localização: src/main/java/com/gsmart/windows/TaskManagerWindow.java
package com.gsmart.windows;

import com.gsmart.TaskStatus;
import com.gsmart.pipeline.PipelineManager;
import com.gsmart.pipeline.PipelineTask;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TaskManagerWindow extends JFrame {

    private final PipelineManager pipelineManager;
    private final TaskManagerTableModel tableModel;

    public TaskManagerWindow(PipelineManager pipelineManager) {
        this.pipelineManager = pipelineManager;
        this.tableModel = new TaskManagerTableModel(pipelineManager.getRunningTasks());

        setTitle("Central de Monitoramento de Pipelines");
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JTable taskTable = new JTable(tableModel);
        taskTable.setFillsViewportHeight(true);
        taskTable.setRowHeight(30);
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskTable.getColumnModel().getColumn(0).setPreferredWidth(350);

        // --- Início da Configuração CORRETA dos Renderizadores ---
        taskTable.getColumnModel().getColumn(1).setCellRenderer(new StatusCellRenderer());
        TableCellRenderer buttonRenderer = new ButtonColumnRenderer();
        taskTable.getColumnModel().getColumn(3).setCellRenderer(buttonRenderer);
        taskTable.getColumnModel().getColumn(4).setCellRenderer(buttonRenderer);
        // --- Fim da Configuração ---

        // --- INÍCIO DA CORREÇÃO: Lógica de clique movida para um MouseListener ---
        taskTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = taskTable.rowAtPoint(e.getPoint());
                int column = taskTable.columnAtPoint(e.getPoint());
                if (row < 0 || column < 0) return;

                PipelineTask task = ((TaskManagerTableModel) taskTable.getModel()).getTaskAt(row);

                // Ação para as colunas de botão
                if (column == 3) { // Coluna "Visualizar"
                    pipelineManager.showMonitorFor(task);
                } else if (column == 4) { // Coluna "Ação"
                    if (task.getStatus() == TaskStatus.ERROR) {
                        pipelineManager.relaunchPipeline(task);
                    } else {
                        task.stop();
                    }
                }

                // Ação para limpar o alerta visual
                if (task.hasAlert()) {
                    task.setHasAlert(false);
                    taskTable.repaint();
                }
            }
        });
        // --- FIM DA CORREÇÃO ---

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
        private final String[] columnNames = {"Tarefa", "Status", "Tempo de Execução", "Visualizar", "Ação"};
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
                case 2 -> {
                    long duration = System.currentTimeMillis() - task.getStartTime();
                    long hours = TimeUnit.MILLISECONDS.toHours(duration);
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60;
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60;
                    yield String.format("%02d:%02d:%02d", hours, minutes, seconds);
                }
                case 3 -> "Mostrar Monitor";
                case 4 -> (task.getStatus() == TaskStatus.ERROR) ? "Reiniciar" : "Parar";
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
                setBackground(new Color(255, 255, 224));
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
            } else {
                if(column == 2) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }
            }
            return this;
        }
    }

    // Renderer para as colunas de AÇÃO. A classe É UM BOTÃO.
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
}