// Localização: src/main/java/com/gsmart/GSmartGui.java
package com.gsmart;

import com.gsmart.sources.DatabaseSource;
import com.gsmart.sources.IDataSource;
import com.gsmart.sources.ThingsBoardSource;

import org.slf4j.Logger; // <-- IMPORT ADICIONADO
import org.slf4j.LoggerFactory; // <-- IMPORT ADICIONADO

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class GSmartGui extends JFrame {

    // LINHA ADICIONADA: Criação do objeto logger
    private static final Logger logger = LoggerFactory.getLogger(GSmartGui.class);

    // --- Componentes da Interface ---
    private final JComboBox<String> sourceSelector;
    private final JButton startButton;
    private final JButton stopButton;
    private final JTextArea logArea;
    private final JLabel statusLabel;

    // --- Controle da Pipeline ---
    private Thread pipelineThread;

    public GSmartGui() {
        // --- Configuração da Janela Principal ---
        setTitle("GSmart - Processador de Dados");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // --- Painel Superior com Controles ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Fonte de Dados:"));

        String[] sources = {"ThingsBoard - Produção", "Banco de Dados Espelho"};
        sourceSelector = new JComboBox<>(sources);
        topPanel.add(sourceSelector);

        startButton = new JButton("Iniciar");
        stopButton = new JButton("Parar");
        stopButton.setEnabled(false);

        topPanel.add(startButton);
        topPanel.add(stopButton);

        // --- Área de Texto para Logs ---
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);

        // --- Painel Inferior com Status ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("Status: Parado");
        statusLabel.setForeground(Color.RED);
        bottomPanel.add(statusLabel);

        // --- Adicionando painéis à janela ---
        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // --- Redirecionar o Console para a Área de Texto ---
        redirectSystemStreams();

        // --- Ações dos Botões ---
        startButton.addActionListener(e -> startPipeline());
        stopButton.addActionListener(e -> stopPipeline());
    }

    private void startPipeline() {
        startButton.setEnabled(false);
        sourceSelector.setEnabled(false);
        stopButton.setEnabled(true);
        statusLabel.setText("Status: Rodando...");
        statusLabel.setForeground(new Color(0, 150, 0)); // Verde escuro
        logArea.setText("");

        IDataSource selectedDataSource = createSelectedDataSource();
        // IMPORTANTE: Substitua pela sua URL real do Power BI
        String pbiUrl ="https://api.powerbi.com/beta/f06a7122-3027-4312-b120-38f60897fba4/datasets/b8512173-e419-4a24-9559-2f9f52935190/rows?experience=power-bi&key=%2F7U6mAvLW4ddN8tfVZJfT30CTl6LGrU4wmW%2FdotkmHnoj5eaVfUZh9fzlWFCgFnsSWI55MecpelCBmyb9reDpw%3D%3D";
        DataPipeline pipeline = new DataPipeline(selectedDataSource, pbiUrl);

        pipelineThread = new Thread(() -> {
            try {
                logger.info("Iniciando a thread da pipeline...");
                pipeline.run();
            } catch (InterruptedException ex) {
                logger.info("Pipeline thread interrompida intencionalmente.");
                Thread.currentThread().interrupt(); // Preserva o status de interrupção
            }
        });
        pipelineThread.start();
    }

    private void stopPipeline() {
        if (pipelineThread != null && pipelineThread.isAlive()) {
            pipelineThread.interrupt(); // Interrompe o Thread.sleep() e finaliza o loop
        }
        startButton.setEnabled(true);
        sourceSelector.setEnabled(true);
        stopButton.setEnabled(false);
        statusLabel.setText("Status: Parado");
        statusLabel.setForeground(Color.RED);
        logger.info("Sinal de parada enviado para a pipeline.");
    }

    private IDataSource createSelectedDataSource() {
        String selected = (String) sourceSelector.getSelectedItem();
        logger.info("Fonte de dados selecionada: {}", selected);
        if ("ThingsBoard - Produção".equals(selected)) {
            String url = "http://10.8.0.5:8080";
            String deviceId = "06e109e0-e8ff-11ee-bb8b-2563c61db9b0";
            return new ThingsBoardSource(url, deviceId);
        } else if ("Banco de Dados Espelho".equals(selected)) {
            String dbUrl = "jdbc:postgresql://SEU_SERVIDOR:5432/SEU_BANCO";
            String dbUser = "SEU_USUARIO";
            String dbPassword = "SUA_SENHA";
            String dbQuery = "SELECT * FROM sua_tabela ORDER BY timestamp DESC LIMIT 1";
            return new DatabaseSource(dbUrl, dbUser, dbPassword, dbQuery);
        }
        throw new IllegalStateException("Nenhuma fonte de dados válida foi selecionada.");
    }

    private void redirectSystemStreams() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                // Garante que a atualização da GUI ocorra na Event Dispatch Thread
                SwingUtilities.invokeLater(() -> {
                    logArea.append(String.valueOf((char) b));
                    logArea.setCaretPosition(logArea.getDocument().getLength()); // Auto-scroll
                });
            }
        };
        // Redireciona tanto a saída padrão quanto a saída de erro
        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }
}