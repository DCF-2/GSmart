// Localização: src/main/java/com/gsmart/MonitoringWindow.java
package com.gsmart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;

public class MonitoringWindow extends JFrame {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringWindow.class);

    private final JTextPane insightsTextPane;
    private final JLabel statusLabel;
    private final JButton stopButton;

    public MonitoringWindow(String title, LogViewerWindow logViewer, Runnable onStopRequest, Consumer<MonitoringWindow> onDisposeRequest) {
        setTitle(title);
        setSize(700, 500);
        // Agora a janela pode ser fechada sem parar o processo de fundo.
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationByPlatform(true);

        insightsTextPane = new JTextPane();
        insightsTextPane.setEditable(false);
        insightsTextPane.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane insightsScrollPane = new JScrollPane(insightsTextPane);
        insightsScrollPane.setBorder(BorderFactory.createTitledBorder("Alertas e Insights do Processo"));

        JButton clearInsightsButton = new JButton("Limpar Insights");
        clearInsightsButton.addActionListener(e -> insightsTextPane.setText(""));
        JPanel insightsButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        insightsButtonPanel.add(clearInsightsButton);

        JPanel insightsPanel = new JPanel(new BorderLayout());
        insightsPanel.add(insightsScrollPane, BorderLayout.CENTER);
        insightsPanel.add(insightsButtonPanel, BorderLayout.SOUTH);

        statusLabel = new JLabel("Status: INICIANDO...");
        statusLabel.setForeground(Color.BLUE);

        stopButton = new JButton("Parar Pipeline");
        JButton viewLogsButton = new JButton("Ver Logs da Aplicação");

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.add(stopButton);
        actionPanel.add(viewLogsButton);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        bottomPanel.add(statusLabel, BorderLayout.WEST);
        bottomPanel.add(actionPanel, BorderLayout.EAST);

        setLayout(new BorderLayout(5, 5));
        add(insightsPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // O botão "Parar" agora apenas executa o callback fornecido pelo PipelineManager
        stopButton.addActionListener(e -> {
            if (onStopRequest != null) {
                onStopRequest.run();
            }
        });

        viewLogsButton.addActionListener(e -> logViewer.setVisible(true));

        // O "X" da janela agora notifica o gerenciador que a *janela* foi fechada.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                logger.info("Janela de monitoramento '{}' fechada pelo usuário.", getTitle());
                if (onDisposeRequest != null) {
                    onDisposeRequest.accept(MonitoringWindow.this);
                }
            }
        });
    }

    // --- MÉTODOS PÚBLICOS PARA RECEBER DADOS (API DA JANELA) ---

    public void onInsight(String message, String type) {
        SwingUtilities.invokeLater(() -> {
            Color color = switch (type) {
                case "CUSTO" -> new Color(0, 100, 0);
                case "MANUTENCAO" -> new Color(255, 140, 0);
                case "FALHA" -> Color.RED;
                case "INFO" -> Color.BLUE;
                default -> Color.BLACK;
            };
            appendColoredText(message + "\n\n", color);
        });
    }

    public void onAlert(String title, String message) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE));
    }

    public void setStatus(TaskStatus status) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Status: " + status);
            switch (status) {
                case RUNNING:
                    statusLabel.setForeground(new Color(0, 150, 0));
                    stopButton.setEnabled(true);
                    break;
                case STOPPING:
                case FINISHED:
                case ERROR:
                    statusLabel.setForeground(Color.RED);
                    stopButton.setEnabled(false);
                    // Fecha a janela automaticamente 2 segundos após a tarefa ser finalizada
                    Timer timer = new Timer(2000, e -> dispose());
                    timer.setRepeats(false);
                    timer.start();
                    break;
            }
        });
    }

    private void appendColoredText(String text, Color color) {
        StyledDocument doc = insightsTextPane.getStyledDocument();
        Style style = insightsTextPane.addStyle("Color Style", null);
        StyleConstants.setForeground(style, color);
        try {
            doc.insertString(doc.getLength(), text, style);
            insightsTextPane.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            logger.error("Falha ao adicionar texto estilizado.", e);
        }
    }
}