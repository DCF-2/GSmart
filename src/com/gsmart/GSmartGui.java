// Localização: src/main/java/com/gsmart/GSmartGui.java
package com.gsmart;

import com.gsmart.sources.DatabaseSource;
import com.gsmart.sources.IDataSource;
import com.gsmart.sources.ThingsBoardSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GSmartGui extends JFrame {

    private static final Logger logger = LoggerFactory.getLogger(GSmartGui.class);

    private final JComboBox<String> sourceSelector;
    private final JButton startButton;
    private final JButton stopButton;
    private final JButton viewLogsButton;
    private final JLabel statusLabel;
    private final JButton loadKeysButton;
    private final JPanel keysPanel;
    private final List<JCheckBox> keyCheckBoxes = new ArrayList<>();
    private final LogViewerWindow logViewer;

    private Thread pipelineThread;
    private String lastUsedTableName;
    private String chaveDeAcumuloSelecionada;

    public GSmartGui() {
        setTitle("GSmart - Processador de Dados");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        logViewer = new LogViewerWindow();
        logViewer.redirectSystemStreams();
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        controlPanel.add(new JLabel("Fonte de Dados:"));
        String[] sources = {"Thingsboard API", "Banco de Dados Espelho"};
        sourceSelector = new JComboBox<>(sources);
        controlPanel.add(sourceSelector);
        loadKeysButton = new JButton("Carregar Métricas da Fonte");
        controlPanel.add(loadKeysButton);
        keysPanel = new JPanel();
        keysPanel.setLayout(new BoxLayout(keysPanel, BoxLayout.Y_AXIS));
        JScrollPane keysScrollPane = new JScrollPane(keysPanel);
        keysScrollPane.setBorder(BorderFactory.createTitledBorder("Métricas/Colunas para Enviar ao Power BI"));
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        startButton = new JButton("Iniciar Pipeline");
        stopButton = new JButton("Parar Pipeline");
        viewLogsButton = new JButton("Ver Logs");
        stopButton.setEnabled(false);
        actionPanel.add(startButton);
        actionPanel.add(stopButton);
        actionPanel.add(viewLogsButton);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        statusLabel = new JLabel("Status: OFFLINE", SwingConstants.LEFT);
        statusLabel.setForeground(Color.RED);
        bottomPanel.add(statusLabel, BorderLayout.WEST);
        bottomPanel.add(actionPanel, BorderLayout.EAST);
        setLayout(new BorderLayout(5, 5));
        add(controlPanel, BorderLayout.NORTH);
        add(keysScrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        loadKeysButton.addActionListener(e -> loadAvailableKeys());
        startButton.addActionListener(e -> startPipeline());
        stopButton.addActionListener(e -> stopPipeline());
        viewLogsButton.addActionListener(e -> logViewer.setVisible(true));
    }

    private void loadAvailableKeys() {
        loadKeysButton.setEnabled(false);
        loadKeysButton.setText("Carregando...");
        String selectedSource = (String) sourceSelector.getSelectedItem();
        if ("Banco de Dados Espelho".equals(selectedSource)) {
            String tableName = JOptionPane.showInputDialog(this, "Digite o nome da tabela:", "Nome da Tabela", JOptionPane.QUESTION_MESSAGE);
            if (tableName == null || tableName.trim().isEmpty()) { logger.warn("Nenhum nome de tabela fornecido."); loadKeysButton.setEnabled(true); loadKeysButton.setText("Carregar Métricas"); return; }
            this.lastUsedTableName = tableName.trim();
            new SwingWorker<List<String>, Void>() {
                @Override protected List<String> doInBackground() throws Exception {
                    DatabaseSource tempSource = new DatabaseSource("jdbc:postgresql://localhost:5432/seu_banco", "postgres", "sua_senha", lastUsedTableName, null);
                    return tempSource.getAvailableColumns(lastUsedTableName);
                }
                @Override protected void done() { handleKeysLoaded(this); }
            }.execute();
        } else if ("Thingsboard API".equals(selectedSource)) {
            new SwingWorker<List<String>, Void>() {
                @Override protected List<String> doInBackground() throws Exception {
                    ThingsBoardSource tempSource = new ThingsBoardSource("http://10.8.0.5:8080", "06e109e0-e8ff-11ee-bb8b-2563c61db9b0", null);
                    return tempSource.getAvailableKeys();
                }
                @Override protected void done() { handleKeysLoaded(this); }
            }.execute();
        }
    }

    private void handleKeysLoaded(SwingWorker<List<String>, Void> worker) {
        try {
            List<String> keys = worker.get();
            if (keys.isEmpty()) {
                logger.warn("Nenhuma chave/coluna foi encontrada na fonte de dados.");
                JOptionPane.showMessageDialog(this, "Nenhuma métrica ou coluna foi encontrada!", "Aviso", JOptionPane.WARNING_MESSAGE);
                keysPanel.removeAll(); keyCheckBoxes.clear(); keysPanel.revalidate(); keysPanel.repaint();
                return;
            }
            logger.info("{} chaves/colunas encontradas. Populando a interface.", keys.size());
            keysPanel.removeAll(); keyCheckBoxes.clear();
            for (String key : keys) {
                JCheckBox checkBox = new JCheckBox(key, true);
                keysPanel.add(checkBox);
                keyCheckBoxes.add(checkBox);
            }
            Object[] possibilities = keys.toArray();
            String selectedKey = (String) JOptionPane.showInputDialog(this, "Selecione a métrica a ser usada para o 'Acúmulo por Hora':", "Configuração da Métrica de Acúmulo", JOptionPane.PLAIN_MESSAGE, null, possibilities, keys.get(0));
            if (selectedKey != null && !selectedKey.isEmpty()) { this.chaveDeAcumuloSelecionada = selectedKey; logger.info("Usuário selecionou '{}' como a chave de acúmulo.", selectedKey); }
            else { logger.warn("Nenhuma chave de acúmulo foi selecionada. O acúmulo será zero."); this.chaveDeAcumuloSelecionada = null; }
            keysPanel.revalidate(); keysPanel.repaint();
        } catch (Exception e) {
            logger.error("Falha ao buscar chaves da fonte de dados: {}", e.getMessage(), e);
            JOptionPane.showMessageDialog(GSmartGui.this, "Falha ao buscar métricas da fonte:\n" + e.getCause().getMessage(), "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
        } finally {
            loadKeysButton.setEnabled(true);
            loadKeysButton.setText("Carregar Métricas da Fonte");
        }
    }

    private void startPipeline() {
        try {
            // Coleta a lista de chaves selecionadas dos checkboxes
            List<String> selectedKeys = new ArrayList<>();
            for (JCheckBox checkBox : keyCheckBoxes) {
                if (checkBox.isSelected()) {
                    selectedKeys.add(checkBox.getText());
                }
            }
            if (selectedKeys.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nenhuma métrica ou coluna foi selecionada!", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Cria a fonte de dados com base na seleção
            IDataSource selectedDataSource = createSelectedDataSource(selectedKeys);
            String pbiUrl = "https://api.powerbi.com/beta/f06a7122-3027-4312-b120-38f60897fba4/datasets/b8512173-e419-4a24-9559-2f9f52935190/rows?experience=power-bi&key=%2F7U6mAvLW4ddN8tfVZJfT30CTl6LGrU4wmW%2FdotkmHnoj5eaVfUZh9fzlWFCgFnsSWI55MecpelCBmyb9reDpw%3D%3D";

            // Cria a pipeline, passando a fonte, a url e as duas configurações dinâmicas
            DataPipeline pipeline = new DataPipeline(selectedDataSource, pbiUrl, this.chaveDeAcumuloSelecionada, selectedKeys);

            pipelineThread = new Thread(() -> {
                try {
                    pipeline.run();
                } catch (InterruptedException ex) {
                    logger.info("Pipeline thread interrompida intencionalmente.");
                    Thread.currentThread().interrupt();
                }
            });

            setControlsEnabled(false);
            statusLabel.setText("Status: ONLINE");
            statusLabel.setForeground(new Color(0, 150, 0));
            pipelineThread.start();

        } catch (Exception e) {
            logger.error("Falha ao iniciar a pipeline: {}", e.getMessage());
            setControlsEnabled(true);
            statusLabel.setText("Status: ERRO AO INICIAR");
            statusLabel.setForeground(Color.RED);
        }
    }

    private IDataSource createSelectedDataSource(List<String> selectedKeys) {
        String selectedSource = (String) sourceSelector.getSelectedItem();
        logger.info("Fonte de dados selecionada: {}", selectedSource);
        logger.info("Enviando os seguintes campos para o Power BI: {}", selectedKeys);
        if ("Thingsboard API".equals(selectedSource)) {
            String url = "http://10.8.0.5:8080";
            String deviceId = "06e109e0-e8ff-11ee-bb8b-2563c61db9b0";
            return new ThingsBoardSource(url, deviceId, selectedKeys);
        } else if ("Banco de Dados Espelho".equals(selectedSource)) {
            if (this.lastUsedTableName == null || this.lastUsedTableName.isEmpty()) { throw new IllegalStateException("Nome da tabela não carregado. Clique em 'Carregar Métricas' primeiro."); }
            String dbUrl = "jdbc:postgresql://localhost:5432/seu_banco";
            String dbUser = "postgres";
            String dbPassword = "sua_senha";
            return new DatabaseSource(dbUrl, dbUser, dbPassword, this.lastUsedTableName, selectedKeys);
        }
        throw new IllegalStateException("Nenhuma fonte de dados válida foi selecionada.");
    }

    private void stopPipeline() {
        if (pipelineThread != null && pipelineThread.isAlive()) {
            pipelineThread.interrupt();
        }
        setControlsEnabled(true);
        statusLabel.setText("Status: OFFLINE");
        statusLabel.setForeground(Color.RED);
        logger.info("Sinal de parada enviado pelo usuário.");
    }

    private void setControlsEnabled(boolean enabled) {
        startButton.setEnabled(enabled);
        loadKeysButton.setEnabled(enabled);
        sourceSelector.setEnabled(enabled);
        stopButton.setEnabled(!enabled);
    }
}