// Localização: src/main/java/com/gsmart/windows/ConnectionErrorDialog.java
package main.java.com.gsmart.Gui.windows;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Uma janela de diálogo não-modal que informa o utilizador sobre o estado da conexão de uma pipeline.
 * <p>
 * Esta janela é exibida quando uma {@link main.java.com.gsmart.pipeline.DataPipeline}
 * perde a sua conexão com a fonte de dados. Ela fornece feedback visual sobre o estado
 * (ex: "Falha de Conexão", "Tentando reconectar em X segundos...") e permite que o
 * utilizador intervenha, forçando uma reconexão imediata ou cancelando a pipeline.
 */
public class ConnectionErrorDialog extends JDialog {

    private final JLabel statusLabel;
    private final JButton reconnectNowButton;
    private Timer countdownTimer;

    /**
     * Constrói a janela de diálogo de erro de conexão.
     *
     * @param owner O Frame pai ao qual este diálogo está associado.
     * @param pipelineName O nome da pipeline, exibido no título da janela.
     * @param onReconnectNow Um {@link Runnable} a ser executado quando o utilizador clica em "Reconectar".
     * @param onCancelPipeline Um {@link Runnable} a ser executado quando o utilizador clica em "Cancelar Pipeline".
     * @param onDispose Um {@link Runnable} a ser executado quando a janela é fechada, para limpar referências.
     */
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

    /**
     * Exibe o estado de "Conexão Perdida" na janela de diálogo.
     * <p>
     * Atualiza o texto do rótulo com a mensagem de erro, altera a cor para vermelho
     * para indicar uma falha e garante que o botão de reconexão manual esteja visível e ativo.
     *
     * @param errorMessage A mensagem de erro específica que causou a perda de conexão.
     */
    public void showConnectionLost(String errorMessage) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("<html><div style='text-align: center;'><b>Falha de Conexão:</b><br/>" + errorMessage + "</div></html>");
            statusLabel.setForeground(Color.RED);
            reconnectNowButton.setVisible(true);
            reconnectNowButton.setEnabled(true); // Manter o botão "Tentar Agora" habilitado
            setVisible(true);
        });
    }

    /**
     * Inicia uma contagem decrescente, mostrando ao utilizador o tempo até à próxima tentativa de reconexão.
     * <p>
     * Atualiza o rótulo de estado a cada segundo com o tempo restante. A cor do texto é
     * alterada para indicar que uma tentativa de reconexão está pendente. O botão para
     * forçar uma reconexão manual permanece ativo durante a contagem.
     *
     * @param delayInSeconds O tempo total, em segundos, para a contagem decrescente.
     */
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

    /**
     * Exibe o estado de "Conexão Restabelecida" e fecha a janela automaticamente.
     * <p>
     * Altera o texto e a cor do rótulo para verde, indicando sucesso. A janela é então
     * fechada após um curto período para não poluir a tela do utilizador.
     */
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