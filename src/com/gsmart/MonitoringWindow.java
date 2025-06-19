// Localização: src/main/java/com/gsmart/MonitoringWindow.java
package com.gsmart;

import com.gsmart.sources.IDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.List;

public class MonitoringWindow extends JFrame implements GSmartListener {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringWindow.class);

    private final JTextPane insightsTextPane;
    private final JLabel statusLabel;
    private final JButton stopButton;
    private final JButton viewLogsButton;

    // --- MUDANÇA: Agora recebe a janela de logs, não a cria mais ---
    private final LogViewerWindow logViewer;

    private final DataPipeline pipeline;
    private Thread pipelineThread;

    // --- MUDANÇA: Construtor agora recebe o LogViewerWindow global ---
    public MonitoringWindow(IDataSource dataSource, String pbiUrl, String chaveDeAcumulo, List<MetricConfig> metricConfigs, LogicConfig logicConfig, LogViewerWindow logViewer) {
        this.pipeline = new DataPipeline(dataSource, pbiUrl, chaveDeAcumulo, metricConfigs, logicConfig, this);
        this.logViewer = logViewer; // Armazena a referência da janela de log global

        setTitle("Monitoramento: " + dataSource.getSourceName());
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

        statusLabel = new JLabel("Status: INICIANDO...", SwingConstants.LEFT);
        statusLabel.setForeground(Color.BLUE);

        stopButton = new JButton("Parar Pipeline");
        viewLogsButton = new JButton("Ver Logs da Aplicação");

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

        // O listener do botão agora apenas torna a janela de logs global visível
        stopButton.addActionListener(e -> stopPipeline());
        viewLogsButton.addActionListener(e -> this.logViewer.setVisible(true));

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                stopPipeline();
            }
        });
    }

    public void start() {
        pipelineThread = new Thread(() -> {
            try {
                SwingUtilities.invokeLater(()-> {
                    statusLabel.setText("Status: ONLINE");
                    statusLabel.setForeground(new Color(0, 150, 0));
                });
                pipeline.run();
            } catch (InterruptedException ex) {
                logger.info("Pipeline thread (Monitor: {}) interrompida intencionalmente.", getTitle());
                Thread.currentThread().interrupt();
            }
        });
        pipelineThread.start();
    }

    private void stopPipeline() {
        if (pipelineThread != null && pipelineThread.isAlive()) {
            pipelineThread.interrupt();
        }
        statusLabel.setText("Status: OFFLINE");
        statusLabel.setForeground(Color.RED);
        stopButton.setEnabled(false);

        Timer timer = new Timer(1500, e -> dispose());
        timer.setRepeats(false);
        timer.start();
    }

    @Override
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

    @Override
    public void onAlert(String title, String message) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE));
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