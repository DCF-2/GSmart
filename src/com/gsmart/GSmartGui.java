// Localização: src/main/java/com/gsmart/GSmartGui.java
package com.gsmart;

import com.gsmart.config.ConfigManager;
import com.gsmart.config.LogicConfig;
import com.gsmart.config.MetricConfig;
import com.gsmart.config.PipelineConfiguration;
import com.gsmart.pipeline.PipelineManager;
//import com.gsmart.pipeline.PipelineTask;
import com.gsmart.sources.*;
import com.gsmart.windows.LogViewerWindow;
//import com.gsmart.windows.MonitoringWindow;
import com.gsmart.windows.TaskManagerWindow;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

public class GSmartGui extends JFrame {

    private static final Logger logger = LoggerFactory.getLogger(GSmartGui.class);

    private final JComboBox<String> sourceSelector;
    private final JButton startButton;
    private final JButton stopAllButton;
    private final JButton monitoringButton;
    private final JButton loadKeysButton;
    private final JTextField pbiUrlField;
    private final JPanel thingsboardConfigPanel;
    private final JTextField thingsboardUrlField;
    private final JButton tbConnectButton;
    private final JLabel tbStatusLabel;
    private final JComboBox<DeviceProfile> deviceProfileSelector;
    private final JComboBox<Device> deviceSelector;
    private final JPanel databaseConfigPanel;
    private final JTextField dbUrlField;
    private final JTextField dbUserField;
    private final JPasswordField dbPasswordField;
    private final JButton dbConnectButton;
    private final JLabel dbStatusLabel;
    private final JComboBox<String> dbTableSelector;
    private final JTable metricsTable;
    private final MetricTableModel tableModel;
    private final JPanel sourceConfigCardPanel;
    private final LogViewerWindow globalLogViewer;
    private final PipelineManager pipelineManager;
    private final ConfigManager configManager;
    private TaskManagerWindow taskManagerWindow;
    private final JCheckBox runLogicCheckBox;
    private final OkHttpClient sharedOkHttpClient;
    private String chaveDeAcumuloSelecionada;
    private LogicConfig logicConfig;

    public GSmartGui(LogViewerWindow logViewer, PipelineManager pipelineManager) {
        this.globalLogViewer = logViewer;
        this.pipelineManager = pipelineManager;
        this.configManager = new ConfigManager();
        this.pipelineManager.setParentComponent(this);
        this.pipelineManager.setGlobalLogViewer(this.globalLogViewer);
        this.sharedOkHttpClient = new OkHttpClient();

        setTitle("GSmart - Configurador de Pipeline v4.4");
        setSize(850, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel topConfigurationPanel = new JPanel();
        topConfigurationPanel.setLayout(new BoxLayout(topConfigurationPanel, BoxLayout.Y_AXIS));
        JPanel sourceSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sourceSelectionPanel.setBorder(BorderFactory.createTitledBorder("Selecione a Fonte de Dados"));
        sourceSelectionPanel.add(new JLabel("Tipo de Fonte:"));
        String[] sources = {"Thingsboard API", "Banco de Dados Espelho"};
        sourceSelector = new JComboBox<>(sources);
        sourceSelectionPanel.add(sourceSelector);

        sourceConfigCardPanel = new JPanel(new CardLayout());
        thingsboardConfigPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbcTb = new GridBagConstraints();
        gbcTb.insets = new Insets(4, 5, 4, 5); gbcTb.anchor = GridBagConstraints.WEST;
        gbcTb.gridx = 0; gbcTb.gridy = 0; gbcTb.gridwidth = 1; gbcTb.fill = GridBagConstraints.NONE; thingsboardConfigPanel.add(new JLabel("URL do Servidor:"), gbcTb);
        gbcTb.gridx = 1; gbcTb.gridy = 0; gbcTb.weightx = 1.0; gbcTb.fill = GridBagConstraints.HORIZONTAL; thingsboardUrlField = new JTextField(); thingsboardConfigPanel.add(thingsboardUrlField, gbcTb);
        gbcTb.gridx = 2; gbcTb.gridy = 0; gbcTb.weightx = 0; gbcTb.fill = GridBagConstraints.NONE; tbConnectButton = new JButton("Conectar"); thingsboardConfigPanel.add(tbConnectButton, gbcTb);
        gbcTb.gridx = 3; gbcTb.gridy = 0; tbStatusLabel = new JLabel("Não conectado"); tbStatusLabel.setForeground(Color.GRAY); thingsboardConfigPanel.add(tbStatusLabel, gbcTb);
        gbcTb.gridx = 0; gbcTb.gridy = 1; thingsboardConfigPanel.add(new JLabel("Perfil de Dispositivo (Tipo):"), gbcTb);
        gbcTb.gridx = 1; gbcTb.gridy = 1; gbcTb.gridwidth = 3; gbcTb.fill = GridBagConstraints.HORIZONTAL; deviceProfileSelector = new JComboBox<>(); deviceProfileSelector.setEnabled(false); thingsboardConfigPanel.add(deviceProfileSelector, gbcTb);
        gbcTb.gridx = 0; gbcTb.gridy = 2; thingsboardConfigPanel.add(new JLabel("Dispositivo:"), gbcTb);
        gbcTb.gridx = 1; gbcTb.gridy = 2; gbcTb.gridwidth = 3; gbcTb.fill = GridBagConstraints.HORIZONTAL; deviceSelector = new JComboBox<>(); deviceSelector.setEnabled(false); thingsboardConfigPanel.add(deviceSelector, gbcTb);

        databaseConfigPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbcDb = new GridBagConstraints();
        gbcDb.insets = new Insets(2, 5, 2, 5); gbcDb.anchor = GridBagConstraints.WEST;
        gbcDb.gridx = 0; gbcDb.gridy = 0; databaseConfigPanel.add(new JLabel("URL do Banco (JDBC):"), gbcDb);
        gbcDb.gridx = 1; gbcDb.gridy = 0; gbcDb.weightx = 1.0; gbcDb.fill = GridBagConstraints.HORIZONTAL; dbUrlField = new JTextField(); databaseConfigPanel.add(dbUrlField, gbcDb);
        gbcDb.gridx = 2; gbcDb.gridy = 0; gbcDb.weightx = 0; gbcDb.gridheight = 3; gbcDb.fill = GridBagConstraints.VERTICAL;
        dbConnectButton = new JButton("Conectar");
        gbcDb.gridx = 2; gbcDb.gridy = 0; gbcDb.weightx = 0; gbcDb.gridheight = 1;
        gbcDb.fill = GridBagConstraints.NONE;
        databaseConfigPanel.add(dbConnectButton, gbcDb);
        gbcDb.gridx = 3; gbcDb.gridy = 0; gbcDb.gridheight = 1;
        dbStatusLabel = new JLabel("Não conectado");
        dbStatusLabel.setForeground(Color.GRAY);
        databaseConfigPanel.add(dbStatusLabel, gbcDb);
        gbcDb.gridx = 0; gbcDb.gridy = 1; gbcDb.gridheight = 1; gbcDb.fill = GridBagConstraints.NONE; databaseConfigPanel.add(new JLabel("Usuário:"), gbcDb);
        gbcDb.gridx = 1; gbcDb.gridy = 1; gbcDb.fill = GridBagConstraints.HORIZONTAL; dbUserField = new JTextField(); databaseConfigPanel.add(dbUserField, gbcDb);
        gbcDb.gridx = 0; gbcDb.gridy = 2; databaseConfigPanel.add(new JLabel("Senha:"), gbcDb);
        gbcDb.gridx = 1; gbcDb.gridy = 2; gbcDb.fill = GridBagConstraints.HORIZONTAL; dbPasswordField = new JPasswordField(""); databaseConfigPanel.add(dbPasswordField, gbcDb);
        gbcDb.gridx = 0; gbcDb.gridy = 3; databaseConfigPanel.add(new JLabel("Tabela:"), gbcDb);
        gbcDb.gridx = 1; gbcDb.gridy = 3; gbcDb.fill = GridBagConstraints.HORIZONTAL;
        dbTableSelector = new JComboBox<>();
        dbTableSelector.setEnabled(false);
        databaseConfigPanel.add(dbTableSelector, gbcDb);

        sourceConfigCardPanel.add(thingsboardConfigPanel, "Thingsboard API");
        sourceConfigCardPanel.add(databaseConfigPanel, "Banco de Dados Espelho");

        JPanel loadKeysPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        loadKeysButton = new JButton("Carregar Métricas da Fonte");
        loadKeysPanel.add(loadKeysButton);
        JPanel destinationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        destinationPanel.setBorder(BorderFactory.createTitledBorder("Configurar Destino dos Dados"));
        destinationPanel.add(new JLabel("URL de Push do Power BI:"));
        pbiUrlField = new JTextField(45);
        destinationPanel.add(pbiUrlField);
        topConfigurationPanel.add(sourceSelectionPanel);
        topConfigurationPanel.add(sourceConfigCardPanel);
        topConfigurationPanel.add(loadKeysPanel);
        topConfigurationPanel.add(destinationPanel);

        tableModel = new MetricTableModel();
        metricsTable = new JTable(tableModel);
        metricsTable.setFillsViewportHeight(true);
        metricsTable.getColumnModel().getColumn(0).setMaxWidth(60);
        metricsTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        metricsTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        metricsTable.getColumnModel().getColumn(3).setPreferredWidth(180);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        metricsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        JScrollPane keysScrollPane = new JScrollPane(metricsTable);
        keysScrollPane.setBorder(BorderFactory.createTitledBorder("Selecionar, Mapear e Transformar Métricas"));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15));
        startButton = new JButton("Iniciar Pipeline");
        startButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        runLogicCheckBox = new JCheckBox("Executar Lógica de Negócio", true);
        runLogicCheckBox.setToolTipText("Se marcado, a lógica de insights será configurada e executada.");
        actionPanel.add(startButton);
        actionPanel.add(runLogicCheckBox);

        JPanel bottomPanel = new JPanel(new BorderLayout(10, 0));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        bottomPanel.add(actionPanel, BorderLayout.WEST);

        JPanel adminPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        monitoringButton = new JButton("Central de Monitoramento");
        stopAllButton = new JButton("Parar Monitoramento");
        stopAllButton.setForeground(Color.RED);
        JButton viewLogsButton = new JButton("Ver Logs");
        adminPanel.add(monitoringButton);
        adminPanel.add(stopAllButton);
        adminPanel.add(viewLogsButton);
        bottomPanel.add(adminPanel, BorderLayout.CENTER);

        setLayout(new BorderLayout(5, 5));
        add(topConfigurationPanel, BorderLayout.NORTH);
        add(keysScrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        sourceSelector.addItemListener(e -> toggleSourceFields());
        tbConnectButton.addActionListener(e -> connectToThingsboard());
        dbConnectButton.addActionListener(e -> connectToDatabase());
        deviceProfileSelector.addItemListener(e -> { if (e.getStateChange() == ItemEvent.SELECTED) loadDevicesByProfile(); });
        loadKeysButton.addActionListener(e -> loadAvailableKeys());
        startButton.addActionListener(e -> launchPipeline());
        stopAllButton.addActionListener(e -> stopAllPipelines());
        monitoringButton.addActionListener(e -> showTaskManager());
        viewLogsButton.addActionListener(e -> this.globalLogViewer.setVisible(true));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveConfiguration();
            }
        });


        toggleSourceFields();
        loadConfiguration();
    }

    private void loadConfiguration() {
        logger.info("Carregando configurações salvas...");
        Properties props = configManager.loadProperties();
        thingsboardUrlField.setText(props.getProperty("thingsboard.url", "http://10.8.0.5:8080"));
        dbUrlField.setText(props.getProperty("db.url", "jdbc:postgresql://localhost:5432/seu_banco"));
        dbUserField.setText(props.getProperty("db.user", "postgres"));
        pbiUrlField.setText(props.getProperty("powerbi.url", ""));
        sourceSelector.setSelectedItem(props.getProperty("source.last", "Thingsboard API"));
        runLogicCheckBox.setSelected(Boolean.parseBoolean(props.getProperty("logic.run", "true")));
    }

    private void saveConfiguration() {
        logger.info("Salvando configurações antes de fechar...");
        Properties props = new Properties();
        props.setProperty("thingsboard.url", thingsboardUrlField.getText());
        props.setProperty("db.url", dbUrlField.getText());
        props.setProperty("db.user", dbUserField.getText());
        props.setProperty("powerbi.url", pbiUrlField.getText());
        props.setProperty("source.last", (String) sourceSelector.getSelectedItem());
        props.setProperty("logic.run", String.valueOf(runLogicCheckBox.isSelected()));
        configManager.saveProperties(props);
    }

    private void launchPipeline() {

        if (metricsTable.isEditing()) {
            metricsTable.getCellEditor().stopCellEditing();
        }
        try {
            String pbiUrl = pbiUrlField.getText().trim();
            if (pbiUrl.isEmpty() || !pbiUrl.toLowerCase().startsWith("http")) {
                JOptionPane.showMessageDialog(this,
                        "Por favor, insira uma URL de Push do Power BI válida (deve começar com http ou https).",
                        "Erro de Configuração",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            List<MetricConfig> selectedConfigs = tableModel.getSelectedMetrics();
            if (selectedConfigs.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nenhuma métrica foi selecionada para envio!", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean runLogic = runLogicCheckBox.isSelected();

            if (runLogic && (this.logicConfig == null || this.chaveDeAcumuloSelecionada == null)) {
                JOptionPane.showMessageDialog(this, "A caixa 'Executar Lógica de Negócio' está marcada, mas a configuração não foi feita.\nPor favor, carregue as métricas novamente e configure a lógica.", "Erro de Configuração", JOptionPane.ERROR_MESSAGE);
                return;
            }


            IDataSource selectedDataSource = createSelectedDataSource(selectedConfigs.stream().map(MetricConfig::getOriginalName).collect(Collectors.toList()));

            PipelineConfiguration config = new PipelineConfiguration(
                    selectedDataSource, pbiUrl, this.chaveDeAcumuloSelecionada,
                    selectedConfigs, this.logicConfig, this.globalLogViewer, runLogic);

            pipelineManager.launchPipeline(config);

            JOptionPane.showMessageDialog(this, "Pipeline para '" + selectedDataSource.getSourceName() + "' iniciada em segundo plano.\nAbra a 'Central de Monitoramento' para visualizar.", "Pipeline Iniciada", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            logger.error("Falha ao preparar a pipeline: {}", e.getMessage(), e);
            JOptionPane.showMessageDialog(this, "Falha ao preparar a pipeline:\n" + e.getMessage(), "Erro Crítico", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void stopAllPipelines() { pipelineManager.stopAllPipelines(); }

    private void showTaskManager() {
        if (taskManagerWindow == null || !taskManagerWindow.isDisplayable()) {
            taskManagerWindow = new TaskManagerWindow(this.pipelineManager);
            taskManagerWindow.setLocationRelativeTo(this);
        }
        taskManagerWindow.setVisible(true);
        taskManagerWindow.toFront();
    }

    private void toggleSourceFields() {
        CardLayout cl = (CardLayout) (sourceConfigCardPanel.getLayout());
        String selectedSource = (String) sourceSelector.getSelectedItem();
        if (selectedSource != null) {
            cl.show(sourceConfigCardPanel, selectedSource);
        }
    }

    private String getThingsboardUrl() {
        String url = thingsboardUrlField.getText().trim();
        if (url.isEmpty()) {
            JOptionPane.showMessageDialog(this, "A URL do Servidor ThingsBoard não pode estar vazia.", "Erro de Configuração", JOptionPane.ERROR_MESSAGE);
            throw new IllegalStateException("URL do ThingsBoard não fornecida.");
        }
        return url;
    }

    private void connectToThingsboard() {
        tbStatusLabel.setText("Conectando...");
        tbStatusLabel.setForeground(Color.ORANGE);
        tbConnectButton.setEnabled(false);
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                try { return new ThingsBoardSource(getThingsboardUrl(), null, null,sharedOkHttpClient).testConnection(); }
                catch (Exception e) { return false; }
            }
            @Override
            protected void done() {
                try {
                    if (get()) {
                        tbStatusLabel.setText("Conectado");
                        tbStatusLabel.setForeground(new Color(0, 128, 0));
                        deviceProfileSelector.setEnabled(true);
                        loadDeviceProfiles();
                    } else { throw new Exception("Falha na autenticação ou URL incorreta."); }
                } catch (Exception e) {
                    tbStatusLabel.setText("Falha!");
                    tbStatusLabel.setForeground(Color.RED);
                } finally {
                    tbConnectButton.setEnabled(true);
                }
            }
        }.execute();
    }

    private void connectToDatabase() {
        dbStatusLabel.setText("Conectando...");
        dbStatusLabel.setForeground(Color.ORANGE);
        dbConnectButton.setEnabled(false);
        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                DatabaseSource tempSource = new DatabaseSource(dbUrlField.getText().trim(), dbUserField.getText().trim(), new String(dbPasswordField.getPassword()), null, null);
                if (tempSource.testConnection()) {
                    return tempSource.getAvailableTables();
                } else {
                    throw new SQLException("Não foi possível validar a conexão com o banco de dados.");
                }
            }
            @Override
            protected void done() {
                try {
                    List<String> tables = get();
                    dbStatusLabel.setText("Conectado");
                    dbStatusLabel.setForeground(new Color(0, 128, 0));
                    dbTableSelector.removeAllItems();
                    tables.forEach(dbTableSelector::addItem);
                    dbTableSelector.setEnabled(true);
                } catch (Exception e) {
                    dbStatusLabel.setText("Falha!");
                    dbStatusLabel.setForeground(Color.RED);
                    JOptionPane.showMessageDialog(GSmartGui.this, "Não foi possível conectar ao Banco de Dados:\n" + e.getCause().getMessage(), "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
                } finally {
                    dbConnectButton.setEnabled(true);
                }
            }
        }.execute();
    }

    private void loadAvailableKeys() {
        loadKeysButton.setEnabled(false);
        loadKeysButton.setText("Carregando...");
        String selectedSource = (String) sourceSelector.getSelectedItem();
        try {
            if ("Thingsboard API".equals(selectedSource)) {
                Device selectedDevice = (Device) deviceSelector.getSelectedItem();
                if (selectedDevice == null) {
                    JOptionPane.showMessageDialog(this, "Por favor, conecte e selecione um dispositivo primeiro.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    setLoadButtonReady();
                    return;
                }
                new SwingWorker<List<String>, Void>() {
                    @Override protected List<String> doInBackground() throws Exception {
                        return new ThingsBoardSource(getThingsboardUrl(), selectedDevice.id(), null, sharedOkHttpClient).getAvailableKeys();
                    }
                    @Override protected void done() { handleKeysLoaded(this); }
                }.execute();
            } else if ("Banco de Dados Espelho".equals(selectedSource)) {
                String selectedTable = (String) dbTableSelector.getSelectedItem();
                if (selectedTable == null) {
                    JOptionPane.showMessageDialog(this, "Por favor, conecte e selecione uma tabela primeiro.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    setLoadButtonReady();
                    return;
                }
                new SwingWorker<List<String>, Void>() {
                    @Override protected List<String> doInBackground() throws Exception {
                        String dbUrl = dbUrlField.getText().trim();
                        String dbUser = dbUserField.getText().trim();
                        String dbPassword = new String(dbPasswordField.getPassword());
                        return new DatabaseSource(dbUrl, dbUser, dbPassword, selectedTable, null).getAvailableColumns(selectedTable);
                    }
                    @Override protected void done() { handleKeysLoaded(this); }
                }.execute();
            }
        } catch (IllegalStateException e) {
            logger.error("Pré-condição para carregar chaves falhou: {}", e.getMessage());
            setLoadButtonReady();
        }
    }

    private void handleKeysLoaded(SwingWorker<List<String>, Void> worker) {
        try {
            List<String> keys = worker.get();
            if (keys.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nenhuma métrica ou coluna foi encontrada para esta fonte!", "Aviso", JOptionPane.WARNING_MESSAGE);
                tableModel.clearMetrics();
                return;
            }

            int option = JOptionPane.showConfirmDialog(this,
                    "Deseja ativar e configurar a Lógica de Negócio (Alertas, Insights) para esta pipeline?",
                    "Configuração da Lógica de Negócio",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (option == JOptionPane.YES_OPTION) {
                runLogicCheckBox.setSelected(true);
                String acumuloKey = showDropdownDialog(keys, "Configuração (1/4)", "Selecione a métrica para o 'Acúmulo por Hora':");
                if (acumuloKey == null) { logger.warn("Usuário cancelou a configuração."); tableModel.clearMetrics(); return; }
                this.chaveDeAcumuloSelecionada = acumuloKey;

                String tempKey = showDropdownDialog(keys, "Mapeamento da Lógica (2/4)", "Selecione a métrica de 'Temperatura':\n(Pode cancelar se não aplicável)");
                String fpKey = showDropdownDialog(keys, "Mapeamento da Lógica (3/4)", "Selecione a métrica de 'Fator de Potência':\n(Pode cancelar se não aplicável)");
                String ptotKey = showDropdownDialog(keys, "Mapeamento da Lógica (4/4)", "Selecione a métrica de 'Potência Ativa':\n(Pode cancelar se não aplicável)");
                this.logicConfig = new LogicConfig(tempKey, fpKey, ptotKey);
                logger.info("Lógica de negócio configurada pelo usuário.");

            } else {
                runLogicCheckBox.setSelected(false);
                this.chaveDeAcumuloSelecionada = null;
                this.logicConfig = null;
                logger.info("Usuário optou por não configurar a lógica de negócio.");
            }

            List<MetricConfig> configs = keys.stream().map(MetricConfig::new).collect(Collectors.toList());
            tableModel.setMetrics(configs);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Falha ao carregar as métricas/colunas:\n" + e.getCause().getMessage(), "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
        } finally {
            setLoadButtonReady();
        }
    }

    private void setLoadButtonReady() {
        loadKeysButton.setEnabled(true);
        loadKeysButton.setText("Carregar Métricas da Fonte");
    }

    private IDataSource createSelectedDataSource(List<String> originalKeys) throws Exception { // Adiciona "throws Exception"
        String selectedSource = (String) sourceSelector.getSelectedItem();
        if ("Thingsboard API".equals(selectedSource)) {
            String tbUrl = getThingsboardUrl();
            Device selectedDevice = (Device) deviceSelector.getSelectedItem();
            if (selectedDevice == null) { throw new IllegalStateException("Nenhum dispositivo do ThingsBoard foi selecionado."); }
            ThingsBoardSource tbSource = new ThingsBoardSource(tbUrl, selectedDevice.id(), originalKeys, sharedOkHttpClient);
            tbSource.testConnectionAndThrow(); // Testa a conexão; lança exceção se falhar
            return tbSource;

        } else if ("Banco de Dados Espelho".equals(selectedSource)) {
            String dbUrl = dbUrlField.getText().trim();
            String dbUser = dbUserField.getText().trim();
            String dbPassword = new String(dbPasswordField.getPassword());
            String dbTable = (String) dbTableSelector.getSelectedItem();
            if (dbTable == null) { throw new IllegalStateException("Nenhuma tabela do banco de dados foi selecionada.");}
            DatabaseSource dbSource = new DatabaseSource(dbUrl, dbUser, dbPassword, dbTable, originalKeys);
            dbSource.testConnectionAndThrow(); // Testa a conexão; lança exceção se falhar
            return dbSource;
        }
        throw new IllegalStateException("Nenhuma fonte de dados válida foi selecionada.");
    }

    private String showDropdownDialog(List<String> options, String title, String message) {
        if (options == null || options.isEmpty()) return null;
        Object[] possibilities = options.toArray();
        return (String) JOptionPane.showInputDialog(this, message, title, JOptionPane.PLAIN_MESSAGE, null, possibilities, options.get(0));
    }

    private void loadDeviceProfiles() {
        Object previouslySelected = deviceProfileSelector.getSelectedItem();
        tbConnectButton.setEnabled(false);
        new SwingWorker<List<DeviceProfile>, Void>() {
            @Override
            protected List<DeviceProfile> doInBackground() throws Exception {
                return new ThingsBoardSource(getThingsboardUrl(), null, null, sharedOkHttpClient).getDeviceProfiles();
            }
            @Override
            protected void done() {
                try {
                    List<DeviceProfile> profiles = get();
                    deviceProfileSelector.removeAllItems();
                    profiles.forEach(deviceProfileSelector::addItem);
                    if (previouslySelected != null) {
                        for (int i = 0; i < deviceProfileSelector.getItemCount(); i++) {
                            if (Objects.equals(deviceProfileSelector.getItemAt(i), previouslySelected)) {
                                deviceProfileSelector.setSelectedIndex(i);
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("Falha ao buscar perfis de dispositivo: {}", e.getMessage(), e);
                    tbStatusLabel.setText("Falha!");
                    tbStatusLabel.setForeground(Color.RED);
                } finally {
                    tbConnectButton.setEnabled(true);
                }
            }
        }.execute();
    }

    private void loadDevicesByProfile() {
        DeviceProfile selectedProfile = (DeviceProfile) deviceProfileSelector.getSelectedItem();
        if (selectedProfile == null) { deviceSelector.removeAllItems(); return; }
        deviceSelector.setEnabled(false);
        new SwingWorker<List<Device>, Void>() {
            @Override
            protected List<Device> doInBackground() throws Exception {
                return new ThingsBoardSource(getThingsboardUrl(), null, null, sharedOkHttpClient).getDevicesByProfileId(selectedProfile.id());
            }
            @Override
            protected void done() {
                try {
                    List<Device> devices = get();
                    deviceSelector.removeAllItems();
                    devices.forEach(deviceSelector::addItem);
                    deviceSelector.setEnabled(true);
                } catch (Exception e) {
                    logger.error("Falha ao buscar dispositivos para o perfil '{}': {}", selectedProfile.name(), e.getMessage(), e);
                }
            }
        }.execute();
    }
}

class MetricTableModel extends AbstractTableModel {
    private final String[] columnNames = {"Enviar", "Nome Original", "Enviar Como (Alias)", "Função/Expressão (usar 'valor')"};
    private List<MetricConfig> metrics = new ArrayList<>();

    public List<MetricConfig> getSelectedMetrics() {
        return metrics.stream().filter(MetricConfig::isSelected).collect(Collectors.toList());
    }
    public void setMetrics(List<MetricConfig> metrics) {
        this.metrics = new ArrayList<>(metrics);
        fireTableDataChanged();
    }
    public void clearMetrics() {
        this.metrics.clear();
        fireTableDataChanged();
    }
    @Override public int getRowCount() { return metrics.size(); }
    @Override public int getColumnCount() { return columnNames.length; }
    @Override public String getColumnName(int column) { return columnNames[column]; }
    @Override public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) return Boolean.class;
        return String.class;
    }
    @Override public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 0 || columnIndex == 2 || columnIndex == 3;
    }
    @Override public Object getValueAt(int rowIndex, int columnIndex) {
        MetricConfig metric = metrics.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> metric.isSelected();
            case 1 -> metric.getOriginalName();
            case 2 -> metric.getAlias();
            case 3 -> metric.getExpression();
            default -> null;
        };
    }
    @Override public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        MetricConfig metric = metrics.get(rowIndex);
        switch (columnIndex) {
            case 0 -> metric.setSelected((Boolean) aValue);
            case 2 -> metric.setAlias((String) aValue);
            case 3 -> metric.setExpression((String) aValue);
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }
}