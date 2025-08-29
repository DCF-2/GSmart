// Localização: src/com/gsmart/Gui/windows/AutoStartManagerWindow.java
package main.java.com.gsmart.Gui.windows;

import main.java.com.gsmart.Gui.AutoStartTableModel;
import main.java.com.gsmart.config.ConfigManager;
import main.java.com.gsmart.config.SerializablePipelineConfig;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Janela para a gestão das pipelines configuradas para o início automático.
 * Permite ao utilizador visualizar e remover pipelines da lista de auto-start.
 */
public class AutoStartManagerWindow extends JDialog {

    private final ConfigManager configManager;
    private final AutoStartTableModel tableModel;
    private final JTable pipelineTable;

    public AutoStartManagerWindow(Frame owner, ConfigManager configManager) {
        super(owner, "Gerir Pipelines de Início Automático", true);
        this.configManager = configManager;

        // --- Configuração da Janela ---
        setSize(600, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // --- Tabela de Pipelines (Centro) ---
        tableModel = new AutoStartTableModel();
        pipelineTable = new JTable(tableModel);
        pipelineTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(pipelineTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Pipelines salvas para iniciar com o GSmart"));
        add(scrollPane, BorderLayout.CENTER);

        // --- Painel de Botões (Sul) ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton removeButton = new JButton("Remover Selecionada");
        JButton closeButton = new JButton("Fechar");

        buttonPanel.add(removeButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Ações dos Botões ---
        closeButton.addActionListener(e -> dispose());

        removeButton.addActionListener(e -> {
            int selectedRow = pipelineTable.getSelectedRow();

            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Por favor, selecione uma pipeline na tabela para remover.", "Nenhuma Pipeline Selecionada", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Tem a certeza que deseja remover esta pipeline do início automático?",
                    "Confirmar Remoção",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                // Remove a linha do modelo da tabela
                tableModel.removeRow(selectedRow);
                // Salva a lista atualizada de volta no ficheiro
                configManager.saveActivePipelines(tableModel.getPipelineConfigs());
                JOptionPane.showMessageDialog(this, "Pipeline removida do início automático com sucesso.", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Carrega os dados na tabela ao abrir a janela
        loadSavedPipelines();
    }

    /**
     * Busca as configurações de pipeline do ConfigManager e atualiza a tabela.
     */
    private void loadSavedPipelines() {
        List<SerializablePipelineConfig> savedConfigs = configManager.loadActivePipelines();
        tableModel.setPipelineConfigs(savedConfigs);
    }
}