// Localização: src/main/java/com/gsmart/windows/ConnectionErrorDialog.java
package main.java.com.gsmart.Gui.windows;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicLong;


public class ConnectionErrorDialog extends JDialog {

    private final JLabel statusLabel;
    private final JButton reconnectNowButton;
    private Timer countdownTimer;

    public ConnectionErrorDialog(Frame owner, String pipelineName, Runnable onReconnectNow, Runnable onCancelPipeline, Runnable onDispose) {
        super(owner, "Status de Conexão: " + pipelineName, false); // false = não-modal
        setSize(450, 150);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        statusLabel = new JLabel("Iniciando verificação de status...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        reconnectNowButton = new JButton("Reconectar");
        JButton cancelPipelineButton = new JButton("Cancelar Pipeline");
        cancelPipelineButton.setForeground(Color.RED);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(reconnectNowButton);
        buttonPanel.add(cancelPipelineButton);

        setLayout(new BorderLayout(10, 10));
        add(statusLabel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        reconnectNowButton.addActionListener(e -> {
            System.out.println("DEBUG: Botão 'Reconectar' foi clicado. Executando onReconnectNow.");
            if (onReconnectNow != null) {
                onReconnectNow.run();
            }
        });

        cancelPipelineButton.addActionListener(e -> {
            System.out.println("DEBUG: Botão 'Cancelar Pipeline' foi clicado. Executando onCancelPipeline.");
            if (onCancelPipeline != null) {
                onCancelPipeline.run();
            }
            dispose();
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if(countdownTimer != null) countdownTimer.stop();
                if (onDispose != null) onDispose.run();
            }
        });
    }

    public void showConnectionLost(String errorMessage) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("<html><div style='text-align: center;'><b>Falha de Conexão:</b><br/>" + errorMessage + "</div></html>");
            statusLabel.setForeground(Color.RED);
            reconnectNowButton.setVisible(true);
            reconnectNowButton.setEnabled(true); // Manter o botão "Tentar Agora" habilitado
            setVisible(true);
        });
    }

    public void startCountdown(long delayInSeconds) {
        SwingUtilities.invokeLater(() -> {
            if (countdownTimer != null && countdownTimer.isRunning()) countdownTimer.stop();

            AtomicLong countdown = new AtomicLong(delayInSeconds);
            statusLabel.setText(String.format("Tentando reconectar em %d segundos...", countdown.get()));
            statusLabel.setForeground(Color.ORANGE); // Cor para indicar que está tentando

            // Garante que o botão "Tentar Agora" esteja sempre habilitado
            reconnectNowButton.setEnabled(true);

            countdownTimer = new Timer(1000, e -> {
                long remaining = countdown.decrementAndGet();
                if (remaining > 0) {
                    statusLabel.setText(String.format("Tentando reconectar em %d segundos...", remaining));
                } else {
                    // Quando o contador chega a 0, a pipeline vai tentar reconectar.
                    // A ConnectionErrorDialog apenas reflete esse estado.
                    statusLabel.setText("Reconectando...");
                    statusLabel.setForeground(Color.BLUE); // Muda a cor para indicar "em progresso"
                    // O botão "Tentar Agora" permanece habilitado para permitir uma tentativa manual a qualquer momento
                    ((Timer) e.getSource()).stop(); // Para o timer da contagem regressiva
                }
            });
            countdownTimer.setInitialDelay(0); // Inicia a contagem imediatamente
            countdownTimer.start();
            setVisible(true); // Garante que a janela esteja visível
        });
    }

    public void showConnectionRestored() {
        SwingUtilities.invokeLater(() -> {
            if (countdownTimer != null) countdownTimer.stop();
            statusLabel.setText("Conexão Restabelecida!");
            statusLabel.setForeground(new Color(0, 128, 0)); // Verde para sucesso
            reconnectNowButton.setVisible(false); // Oculta o botão de reconexão
            reconnectNowButton.setEnabled(true); // Reabilita para o caso de uma nova falha futura

            // Fecha a janela de status automaticamente após 2 segundos
            Timer closeTimer = new Timer(2000, e -> dispose());
            closeTimer.setRepeats(false);
            closeTimer.start();
        });
    }
}