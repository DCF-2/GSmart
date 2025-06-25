// Localização: src/main/java/com/gsmart/windows/ReconnectionDialog.java
package com.gsmart.windows;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicLong;

public class ReconnectionDialog extends JDialog {

    private final JLabel statusLabel;
    private final JButton reconnectNowButton;
    private final JButton cancelPipelineButton;
    private Timer countdownTimer;

    public ReconnectionDialog(Frame owner, String pipelineName, Runnable onReconnectNow, Runnable onCancelPipeline) {
        super(owner, "Status de Conexão: " + pipelineName, false); // false = não-modal
        setSize(450, 150);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        statusLabel = new JLabel("Iniciando verificação de status...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        reconnectNowButton = new JButton("Tentar Agora");
        cancelPipelineButton = new JButton("Cancelar Pipeline");
        cancelPipelineButton.setForeground(Color.RED);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(reconnectNowButton);
        buttonPanel.add(cancelPipelineButton);

        setLayout(new BorderLayout(10, 10));
        add(statusLabel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        reconnectNowButton.addActionListener(e -> {
            if (onReconnectNow != null) {
                onReconnectNow.run();
            }
        });

        cancelPipelineButton.addActionListener(e -> {
            if (onCancelPipeline != null) {
                onCancelPipeline.run();
            }
            dispose();
        });
    }

    public void showConnectionLost(String errorMessage) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("<html><div style='text-align: center;'><b>Falha de Conexão:</b><br/>" + errorMessage + "</div></html>");
            statusLabel.setForeground(Color.RED);
            reconnectNowButton.setVisible(true);
            setVisible(true);
        });
    }

    public void startCountdown(long delayInSeconds) {
        SwingUtilities.invokeLater(() -> {
            if (countdownTimer != null && countdownTimer.isRunning()) {
                countdownTimer.stop();
            }

            AtomicLong countdown = new AtomicLong(delayInSeconds);
            statusLabel.setText(String.format("Tentando reconectar em %d segundos...", countdown.get()));

            countdownTimer = new Timer(1000, e -> {
                long remaining = countdown.decrementAndGet();
                if (remaining > 0) {
                    statusLabel.setText(String.format("Tentando reconectar em %d segundos...", remaining));
                } else {
                    statusLabel.setText("Reconectando...");
                    statusLabel.setForeground(Color.ORANGE);
                    reconnectNowButton.setEnabled(false);
                    ((Timer) e.getSource()).stop();
                }
            });
            countdownTimer.setInitialDelay(1000);
            countdownTimer.start();
        });
    }

    public void showConnectionRestored() {
        SwingUtilities.invokeLater(() -> {
            if (countdownTimer != null) {
                countdownTimer.stop();
            }
            statusLabel.setText("Conexão Restabelecida!");
            statusLabel.setForeground(new Color(0, 128, 0));
            // Fecha a janela de status automaticamente após 2 segundos
            Timer closeTimer = new Timer(2000, e -> dispose());
            closeTimer.setRepeats(false);
            closeTimer.start();
        });
    }
}