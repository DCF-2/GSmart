// Localização: src/main/java/com/gsmart/windows/MonitoringWindow.java
package com.gsmart.windows;

import com.gsmart.GSmartListener;
import com.gsmart.TaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class MonitoringWindow extends JFrame implements GSmartListener {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringWindow.class);

    private final JTextPane insightsTextPane;
    private final JLabel statusLabel;
    private final JButton stopButton;
    private final JButton reconnectButton;
    private Timer countdownTimer;

    public MonitoringWindow(String title, LogViewerWindow logViewer, Runnable onStopRequest, Runnable onReconnectRequest, Consumer<MonitoringWindow> onDisposeRequest) {
        setTitle(title);
        setSize(700, 500);
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

        reconnectButton = new JButton("Tentar Agora");
        reconnectButton.setVisible(false);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.add(reconnectButton);
        actionPanel.add(stopButton);
        actionPanel.add(viewLogsButton);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        bottomPanel.add(statusLabel, BorderLayout.WEST);
        bottomPanel.add(actionPanel, BorderLayout.EAST);

        setLayout(new BorderLayout(5, 5));
        add(insightsPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        stopButton.addActionListener(e -> {
            if (onStopRequest != null) onStopRequest.run();
        });

        reconnectButton.addActionListener(e -> {
            if (onReconnectRequest != null) onReconnectRequest.run();
        });

        viewLogsButton.addActionListener(e -> logViewer.setVisible(true));

        // --- MUDANÇA IMPORTANTE AQUI ---
        // Usamos o evento windowClosing, que é disparado ANTES de a janela ser destruída.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                logger.info("Janela de monitoramento '{}' fechada pelo usuário via 'X'.", getTitle());
                if (onDisposeRequest != null) {
                    onDisposeRequest.accept(MonitoringWindow.this);
                }
                // O dispose() será chamado automaticamente pela configuração DISPOSE_ON_CLOSE
            }
        });
    }

    @Override
    public void onInsight(String message, String type) {
        SwingUtilities.invokeLater(() -> appendColoredText(message + "\n\n", getColorForType(type)));
    }

    @Override
    public void onAlert(String title, String message) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE));
    }

    @Override
    public void onStatusUpdate(TaskStatus status) {
        updateStatus(status);
    }

    @Override
    public void onConnectionLost(String errorMessage) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Status: " + errorMessage);
            statusLabel.setForeground(Color.RED);
            reconnectButton.setVisible(true);
        });
    }

    @Override
    public void onReconnectionAttempt(long delayInSeconds) {
        if (countdownTimer != null && countdownTimer.isRunning()) countdownTimer.stop();

        AtomicLong countdown = new AtomicLong(delayInSeconds);

        countdownTimer = new Timer(1000, e -> {
            long remaining = countdown.decrementAndGet();
            if (remaining > 0) {
                statusLabel.setText(String.format("Status: Tentando reconectar em %d segundos...", remaining));
            } else {
                statusLabel.setText("Status: Reconectando...");
                statusLabel.setForeground(Color.ORANGE);
                reconnectButton.setVisible(false);
                ((Timer)e.getSource()).stop();
            }
        });
        countdownTimer.setInitialDelay(0);
        countdownTimer.start();
    }

    @Override
    public void onConnectionRestored() {
        SwingUtilities.invokeLater(() -> {
            if (countdownTimer != null) countdownTimer.stop();
            reconnectButton.setVisible(false);
            updateStatus(TaskStatus.RUNNING);
        });
    }

    public void updateStatus(TaskStatus status) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Status: " + status);
            switch (status) {
                case RUNNING -> {
                    statusLabel.setForeground(new Color(0, 150, 0));
                    stopButton.setEnabled(true);
                }
                case STOPPING, FINISHED, ERROR -> {
                    statusLabel.setForeground(Color.RED);
                    stopButton.setEnabled(false);
                    reconnectButton.setVisible(false);
                    if (this.isDisplayable()) {
                        Timer timer = new Timer(2000, e -> dispose());
                        timer.setRepeats(false);
                        timer.start();
                    }
                }
            }
        });
    }

    private Color getColorForType(String type) {
        return switch (type) {
            case "CUSTO" -> new Color(0, 100, 0);
            case "MANUTENÇÃO" -> new Color(255, 140, 0);
            case "FALHA" -> Color.RED;
            case "INFO" -> Color.BLUE;
            default -> Color.BLACK;
        };
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