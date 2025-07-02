// Localização: src/main/java/com/gsmart/windows/MonitoringWindow.java
package com.gsmart.windows;

import com.gsmart.resources.GSmartListener;
import com.gsmart.resources.TaskStatus;
import com.gsmart.pipeline.PipelineTask; // Import adicionado
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.TimeUnit; // Import adicionado
import java.util.function.Consumer;

public class MonitoringWindow extends JFrame implements GSmartListener {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringWindow.class);

    private final JTextPane insightsTextPane;
    private final JLabel statusLabel;
    private final JLabel timerLabel; // Label para o cronômetro
    private final Timer executionTimer; // Timer para atualizar o cronômetro

    // Construtor foi simplificado
    public MonitoringWindow(PipelineTask task, Consumer<MonitoringWindow> onDisposeRequest) {
        setTitle("Monitor: " + task.getDescription());
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationByPlatform(true);

        insightsTextPane = new JTextPane();
        insightsTextPane.setEditable(false);
        insightsTextPane.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane insightsScrollPane = new JScrollPane(insightsTextPane);
        insightsScrollPane.setBorder(BorderFactory.createTitledBorder("Alertas e Insights do Processo"));

        // O botão "Limpar Insights" agora fica no painel de botões de insights
        JButton clearInsightsButton = new JButton("Limpar Insights");
        clearInsightsButton.addActionListener(e -> insightsTextPane.setText(""));
        JPanel insightsButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        insightsButtonPanel.add(clearInsightsButton);

        JPanel insightsPanel = new JPanel(new BorderLayout());
        insightsPanel.add(insightsScrollPane, BorderLayout.CENTER);
        insightsPanel.add(insightsButtonPanel, BorderLayout.SOUTH);

        // --- Novo Painel Inferior ---
        statusLabel = new JLabel("Status: INICIANDO...");
        statusLabel.setForeground(Color.BLUE);

        timerLabel = new JLabel("Tempo de Execução: 00:00:00");
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        bottomPanel.add(statusLabel, BorderLayout.WEST);
        bottomPanel.add(timerLabel, BorderLayout.CENTER);
        // Não há mais painel de ação à direita

        setLayout(new BorderLayout(5, 5));
        add(insightsPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // --- Novo Cronômetro ---
        long startTime = task.getStartTime();
        this.executionTimer = new Timer(1000, e -> {
            long duration = System.currentTimeMillis() - startTime;
            long hours = TimeUnit.MILLISECONDS.toHours(duration);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60;
            long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60;
            timerLabel.setText(String.format("Tempo de Execução: %02d:%02d:%02d", hours, minutes, seconds));
        });
        this.executionTimer.start();
        // --- Fim do Cronômetro ---

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                logger.info("Janela de monitoramento '{}' fechada pelo usuário via 'X'.", getTitle());
                if (executionTimer != null) executionTimer.stop(); // Para o timer para evitar memory leak
                if (onDisposeRequest != null) {
                    onDisposeRequest.accept(MonitoringWindow.this);
                }
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

    // Os métodos de reconexão permanecem, pois a interface GSmartListener os exige,
    // mas eles não terão mais um efeito visual visível NESTA janela.
    @Override public void onConnectionLost(String errorMessage) { /* Não faz nada visualmente nesta tela */ }
    @Override public void onReconnectionAttempt(long delayInSeconds) { /* Não faz nada visualmente nesta tela */ }
    @Override public void onConnectionRestored() { /* Não faz nada visualmente nesta tela */ }

    public void updateStatus(TaskStatus status) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Status: " + status);
            switch (status) {
                case RUNNING -> statusLabel.setForeground(new Color(0, 150, 0));
                case STOPPING, FINISHED, ERROR -> {
                    statusLabel.setForeground(Color.RED);
                    if (executionTimer != null) executionTimer.stop(); // Para o timer quando a pipeline para
                    if (this.isDisplayable()) {
                        Timer closeTimer = new Timer(2000, e -> dispose());
                        closeTimer.setRepeats(false);
                        closeTimer.start();
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