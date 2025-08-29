// Localização: src/main/java/com/gsmart/Gui/panels/DashboardPanel.java
package main.java.com.gsmart.Gui.panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Painel de Dashboard que serve como a tela inicial da aplicação GSmart.
 * Exibe informações resumidas e atalhos para as principais funcionalidades.
 */
public class DashboardPanel extends JPanel {

    private JLabel activePipelinesLabel;
    private JButton openTaskManagerButton;

    public DashboardPanel() {
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- Título Principal ---
        JLabel titleLabel = new JLabel("Bem-vindo ao GSmart", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);

        // --- Painel de Status (Centro) ---
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 10));
        statusPanel.setBorder(BorderFactory.createTitledBorder("Status do Sistema"));

        // Card para Pipelines Ativas
        JPanel pipelineStatusCard = new JPanel(new BorderLayout());
        pipelineStatusCard.setBorder(new EmptyBorder(10, 15, 10, 15));
        activePipelinesLabel = new JLabel("0", SwingConstants.CENTER);
        activePipelinesLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        pipelineStatusCard.add(new JLabel("Pipelines Ativas", SwingConstants.CENTER), BorderLayout.NORTH);
        pipelineStatusCard.add(activePipelinesLabel, BorderLayout.CENTER);

        statusPanel.add(pipelineStatusCard);
        add(statusPanel, BorderLayout.CENTER);


        // --- Painel de Atalhos (Sul) ---
        JPanel shortcutsPanel = new JPanel();
        shortcutsPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        shortcutsPanel.setBorder(BorderFactory.createTitledBorder("Ações Rápidas"));

        openTaskManagerButton = new JButton("Abrir Central de Monitoramento");
        shortcutsPanel.add(openTaskManagerButton);

        add(shortcutsPanel, BorderLayout.SOUTH);
    }

    /**
     * Atualiza o contador de pipelines ativas exibido no dashboard.
     * @param count O número atual de pipelines em execução.
     */
    public void updateActivePipelinesCount(int count) {
        activePipelinesLabel.setText(String.valueOf(count));
    }

    /**
     * Retorna o botão de abrir o gestor de tarefas para que um ActionListener
     * possa ser adicionado externamente.
     * @return O JButton para abrir o TaskManager.
     */
    public JButton getOpenTaskManagerButton() {
        return openTaskManagerButton;
    }
}