// Localização: src/main/java/com/gsmart/Gui/panels/SideMenuPanel.java
package main.java.com.gsmart.Gui.panels;

import main.java.com.gsmart.GSmartGui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Painel de navegação lateral retrátil para a aplicação GSmart.
 * <p>
 * Este painel contém os botões para navegar entre as diferentes secções da
 * aplicação (Dashboard, Configuração, Regras), bem como atalhos para ações
 * rápidas e funções de gestão. O painel pode ser expandido ou recolhido
 * para otimizar o espaço no ecrã.
 */
public class SideMenuPanel extends JPanel {

    private boolean isExpanded = true;
    private final int EXPANDED_WIDTH = 220;
    private final int COLLAPSED_WIDTH = 60;

    // Botões
    private JButton toggleButton;
    private JButton dashboardButton, pipelineConfigButton, alertRulesButton, insightRulesButton;
    private JButton userManagementButton, taskManagerButton, settingsButton;
    private JButton stopAllButton, logsButton, helpButton, logoffButton, toggleThemeButton;
    private JButton quickStartPipelineButton, quickOpenTaskManagerButton;

    private final List<JButton> navButtons = new ArrayList<>();

    public SideMenuPanel(GSmartGui view, String currentUserRole) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        setPreferredSize(new Dimension(EXPANDED_WIDTH, 0));

        // --- Botão de Controlo (Hambúrguer) ---
        toggleButton = createNavButton("☰", "Recolher Menu");
        toggleButton.setHorizontalAlignment(SwingConstants.CENTER);
        add(toggleButton);
        add(Box.createVerticalStrut(10));
        add(new JSeparator());
        add(Box.createVerticalStrut(10));

        // --- Navegação Principal ---
        addSectionTitle("Navegação");
        dashboardButton = createNavButton("⌂", "Dashboard");
        pipelineConfigButton = createNavButton("", "Configurar Pipeline");
        alertRulesButton = createNavButton("!", "Regras de Alerta");
        insightRulesButton = createNavButton("💡", "Regras de Alarme");
        add(dashboardButton);
        add(pipelineConfigButton);
        add(alertRulesButton);
        add(insightRulesButton);
        add(Box.createVerticalStrut(20));
        add(new JSeparator());

        // --- AÇÕES RÁPIDAS (NOVA SEÇÃO) ---
        add(Box.createVerticalStrut(20));
        addSectionTitle("Ações Rápidas");
        quickStartPipelineButton = createNavButton("▶", "Iniciar Nova Pipeline");
        quickOpenTaskManagerButton = createNavButton("📊", "Ver Central de Monitoramento");
        add(quickStartPipelineButton);
        add(quickOpenTaskManagerButton);
        add(Box.createVerticalStrut(20));
        add(new JSeparator());

        boolean isAdmin = "ADMINISTRATOR".equals(currentUserRole);
        // --- Gestão ---
        add(Box.createVerticalStrut(20));
        addSectionTitle("Gestão");
        userManagementButton = createNavButton("👥", "Gerir Utilizadores");
        userManagementButton.setVisible(isAdmin);
        taskManagerButton = createNavButton("🔧", "Central de Monitoramento");
        settingsButton = createNavButton("⚙️", "Configurações");
        add(userManagementButton);
        add(taskManagerButton);
        add(settingsButton);

        add(Box.createVerticalGlue());
        add(new JSeparator());
        add(Box.createVerticalStrut(10));

        // --- Sistema e Ações ---
        stopAllButton = createNavButton("⛔", "Parar Tudo");
        stopAllButton.setForeground(Color.RED);
        logsButton = createNavButton("📋", "Ver Logs");
        helpButton = createNavButton("?", "Ajuda");
        logoffButton = createNavButton("🚪", "Terminar Sessão (Logoff)");
        toggleThemeButton = createNavButton("🎨", "Alterar Tema");
        add(stopAllButton);
        add(logoffButton);
        add(toggleThemeButton);
        add(logsButton);
        add(helpButton);

        add(Box.createVerticalStrut(10));


        toggleButton.addActionListener(e -> toggleMenu());
    }

    private void toggleMenu() {
        isExpanded = !isExpanded;
        setPreferredSize(new Dimension(isExpanded ? EXPANDED_WIDTH : COLLAPSED_WIDTH, 0));
        for (JButton button : navButtons) {
            button.setText(isExpanded ? button.getToolTipText() : button.getName());
            button.setHorizontalAlignment(isExpanded ? SwingConstants.LEFT : SwingConstants.CENTER);
        }
        toggleButton.setToolTipText(isExpanded ? "Recolher Menu" : "Expandir Menu");
        revalidate();
        repaint();
    }

    private void addSectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 0));
        add(label);
    }

    private JButton createNavButton(String iconText, String toolTipText) {
        JButton button = new JButton(toolTipText);
        button.setName(iconText);
        button.setToolTipText(toolTipText);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        styleNavButton(button, iconText, toolTipText);
        navButtons.add(button);
        return button;
    }

    private void styleNavButton(JButton button, String iconText, String toolTipText) {
        button.setName(iconText);
        button.setToolTipText(toolTipText);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        // Não adiciona a navButtons, para que o texto não mude ao expandir/recolher
    }



    // --- Getters para TODOS os botões ---
    public JButton getDashboardButton() { return dashboardButton; }
    public JButton getPipelineConfigButton() { return pipelineConfigButton; }
    public JButton getAlertRulesButton() { return alertRulesButton; }
    public JButton getInsightRulesButton() { return insightRulesButton; }
    public JButton getUserManagementButton() { return userManagementButton; }
    public JButton getSettingsButton() { return settingsButton; }
    public JButton getTaskManagerButton() { return taskManagerButton; }
    public JButton getStopAllButton() { return stopAllButton; }
    public JButton getLogsButton() { return logsButton; }
    public JButton getHelpButton() { return helpButton; }
    public JButton getLogoffButton() { return logoffButton; }
    public JButton getQuickStartPipelineButton() { return quickStartPipelineButton; }
    public JButton getQuickOpenTaskManagerButton() { return quickOpenTaskManagerButton; }
    public JButton getToggleThemeButton() { return toggleThemeButton; }
}