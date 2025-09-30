// Localização: src/main/java/com/gsmart/Gui/panels/DashboardPanel.java
package main.java.com.gsmart.Gui.panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import javax.swing.text.*;
import java.util.Map;

/**
 * Painel de Dashboard que serve como a tela inicial da aplicação GSmart.
 * <p>
 * Exibe informações resumidas do estado do sistema, como o número de pipelines
 * e alertas ativos, e apresenta gráficos em tempo real sobre a atividade recente.
 * Contém também uma área de log para os eventos mais importantes.
 *
 * @see main.java.com.gsmart.services.DashboardLogService
 */
public class DashboardPanel extends JPanel {

    private JLabel activePipelinesLabel, activeAlertsLabel, recentAlarmsLabel;
    private JTextPane logTextPane;
    private final int MAX_LOG_ENTRIES = 50;

    // Agora temos os dois painéis de gráfico e um container ---
    private CardLayout chartLayout;
    private JPanel chartContainerPanel;
    private ActivityBarChartPanel barChartPanel;
    private ActivityLineChartPanel lineChartPanel;


    public DashboardPanel() {
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- Título Principal ---
        JLabel titleLabel = new JLabel("Bem-vindo ao GSmart", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);

        // --- Painel Central com Status e Gráfico ---
        JPanel centerPanel = new JPanel(new BorderLayout(20, 20));

        // --- Painel de Status ---
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 10));
        statusPanel.setBorder(BorderFactory.createTitledBorder("Status do Sistema"));

        activePipelinesLabel = new JLabel("0", SwingConstants.CENTER);
        statusPanel.add(createStatusCard("Pipelines Ativas", activePipelinesLabel));
        activeAlertsLabel = new JLabel("0", SwingConstants.CENTER);
        statusPanel.add(createStatusCard("Alertas Ativos", activeAlertsLabel));
        recentAlarmsLabel = new JLabel("0", SwingConstants.CENTER);
        statusPanel.add(createStatusCard("Alarmes Recentes", recentAlarmsLabel));
        centerPanel.add(statusPanel, BorderLayout.NORTH);

        // --- CONTAINER DE GRÁFICOS COM BOTÃO DE TROCA ---
        JPanel chartWrapperPanel = new JPanel(new BorderLayout());

        // Título e botão
        JPanel chartTitlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        chartTitlePanel.add(new JLabel("Atividade Recente do Sistema"));
        JButton toggleChartButton = new JButton("Alternar Gráfico");
        chartTitlePanel.add(toggleChartButton);
        chartWrapperPanel.add(chartTitlePanel, BorderLayout.NORTH);

        // Container com CardLayout
        chartLayout = new CardLayout();
        chartContainerPanel = new JPanel(chartLayout);

        barChartPanel = new ActivityBarChartPanel();
        lineChartPanel = new ActivityLineChartPanel();

        chartContainerPanel.add(barChartPanel, "BAR"); // Adiciona o gráfico de barras
        chartContainerPanel.add(lineChartPanel, "LINE"); // Adiciona o gráfico de linhas

        chartWrapperPanel.add(chartContainerPanel, BorderLayout.CENTER);
        centerPanel.add(chartWrapperPanel, BorderLayout.CENTER);

        // Ação do botão para trocar os gráficos
        toggleChartButton.addActionListener(e -> chartLayout.next(chartContainerPanel));

        add(centerPanel, BorderLayout.CENTER);

        // --- Painel de Log de Eventos Recentes ---
        logTextPane = new JTextPane();
        logTextPane.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logTextPane);
        logScrollPane.setBorder(BorderFactory.createTitledBorder("Log de Eventos Recentes"));
        logScrollPane.setPreferredSize(new Dimension(0, 150));
        add(logScrollPane, BorderLayout.SOUTH);
    }

    /**
     * Atualiza os dados para ambos os gráficos de atividade (barras e linhas).
     * <p>
     * Este método recebe os dados mais recentes sobre pipelines, alertas e alarmes
     * e passa-os para os componentes de gráfico para que eles se redesenhem.
     *
     * @param pData Os dados de atividade das pipelines.
     * @param aData Os dados de atividade dos alertas.
     * @param alData Os dados de atividade dos alarmes.
     */
    public void updateChartsData(Map<String, Integer> pData, Map<String, Integer> aData, Map<String, Integer> alData) {
        barChartPanel.updateData(pData, aData, alData);
        lineChartPanel.updateData(pData, aData, alData);
    }

    /**
     * Adiciona uma nova mensagem de log ao painel do Dashboard.
     * @param message A mensagem a ser exibida.
     * @param color A cor do texto da mensagem.
     */
    public void addLogMessage(String message, Color color) {
        SwingUtilities.invokeLater(() -> {
            try {
                StyledDocument doc = logTextPane.getStyledDocument();
                Style style = logTextPane.addStyle("Color Style", null);
                StyleConstants.setForeground(style, color);
                doc.insertString(doc.getLength(), message + "\n", style);

                // Limita o número de linhas para evitar sobrecarga de memória, removendo do topo
                if (doc.getDefaultRootElement().getElementCount() > MAX_LOG_ENTRIES) {
                    Element root = doc.getDefaultRootElement();
                    Element firstLine = root.getElement(0);
                    doc.remove(0, firstLine.getEndOffset());
                }
                // --- Força a barra de rolagem a ir para o final ---
                logTextPane.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                System.err.println("Erro ao adicionar mensagem de log ao dashboard: " + e.getMessage());
            }
        });
    }

    private JPanel createStatusCard(String title, JLabel valueLabel) {
        JPanel cardPanel = new JPanel(new BorderLayout(0, 5));
        cardPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        cardPanel.add(titleLabel, BorderLayout.NORTH);
        cardPanel.add(valueLabel, BorderLayout.CENTER);
        return cardPanel;
    }

    public void updateActivePipelinesCount(int count) {
        activePipelinesLabel.setText(String.valueOf(count));
    }

    public void updateActiveAlertsCount(int count) {
        activeAlertsLabel.setText(String.valueOf(count));
        activeAlertsLabel.setForeground(count > 0 ? Color.RED : UIManager.getColor("Label.foreground"));
    }

    public void updateRecentAlarmsCount(int count) {
        recentAlarmsLabel.setText(String.valueOf(count));
    }
}