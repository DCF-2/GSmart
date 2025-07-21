// Localização: src/com/gsmart/GSmartGui.java
package com.gsmart;

// Imports de configuração e lógica
import com.gsmart.Gui.InsightRuleTableModel;
import com.gsmart.Gui.windows.*;
import com.gsmart.config.*;

// Imports de Alertas e insights
import com.gsmart.Gui.AlertRuleTableModel;
import com.gsmart.Gui.MetricTableModel;
import com.gsmart.Gui.SystemMetricCellRenderer;



// Imports do pipeline e recursos
import com.gsmart.pipeline.PipelineManager;
import com.gsmart.resources.IDataSource;
import com.gsmart.sources.*;

// Imports das janelas auxiliares

// Imports de bibliotecas externas e do Java
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Classe principal da interface gráfica (GUI) e ponto de controlo central da aplicação GSmart.
 *
 * Esta janela orquestra toda a interação com o utilizador, permitindo a configuração
 * e gestão completa dos pipelines de dados. As suas principais responsabilidades incluem:
 * <ul>
 * <li>Configurar a fonte de dados (ThingsBoard ou Base de Dados).</li>
 * <li>Gerir a seleção e transformação de métricas a serem processadas.</li>
 * <li>Permitir a criação e edição de Regras de Alerta (notificações críticas).</li>
 * <li>Permitir a criação e edição de Regras de Alarme (insights inteligentes).</li>
 * <li>Iniciar, parar e monitorizar as tarefas de pipeline através do {@link PipelineManager}.</li>
 * </ul>
 *
 * @see com.gsmart.pipeline.PipelineManager
 */
public class GSmartGui extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(GSmartGui.class);

    // --- Componentes da UI (Pipeline) ---
    private final JComboBox<String> sourceSelector;
    private final JButton startButton;
    private final JButton stopAllButton;
    private final JButton monitoringButton;
    private final JButton loadKeysButton;
    private final JTextField pbiUrlField;
    private final JTextField mqttBrokerUrlField;
    private final JTextField telegramTokenField;
    private final JTextField telegramChatIdField;
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

    // --- Componentes da UI (Alertas) ---
    private JTable alertRulesTable;
    private AlertRuleTableModel alertRuleTableModel;
    private List<AlertRule> alertRules;// A nossa lista principal de regras

    // --- Componentes da UI (Insights) ---
    private JTable insightRulesTable;
    private InsightRuleTableModel insightRuleTableModel;
    private List<InsightRule> insightRules;


    // --- Classes de Gestão e Janelas ---
    private final LogViewerWindow globalLogViewer;
    private final PipelineManager pipelineManager;
    private final ConfigManager configManager;
    private TaskManagerWindow taskManagerWindow;
    private ReconnectionLogViewer reconnectionLogViewer;

    // --- Outros ---
    private final OkHttpClient sharedOkHttpClient;

    /**
     * Construtor da janela principal da aplicação GSmart.
     *
     * Inicializa todos os componentes da interface gráfica, configura os painéis,
     * tabelas e listeners de eventos, e carrega as configurações da sessão anterior.
     *
     * @param logViewer A instância partilhada do visualizador de logs gerais.
     * @param pipelineManager O gestor central que orquestra todas as tarefas de pipeline.
     */
    public GSmartGui(LogViewerWindow logViewer, PipelineManager pipelineManager) {
        // --- Inicialização de Variáveis ---
        this.globalLogViewer = logViewer;
        this.pipelineManager = pipelineManager;
        this.configManager = new ConfigManager();
        this.pipelineManager.setParentComponent(this);
        this.pipelineManager.setGlobalLogViewer(this.globalLogViewer);
        this.alertRules = new ArrayList<>(); // Inicializa a lista de regras
        this.insightRules = new ArrayList<>();
        this.sharedOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // Define o timeout de conexão para 30 segundos
                .writeTimeout(30, TimeUnit.SECONDS)   // Define o timeout de escrita para 30 segundos
                .readTimeout(30, TimeUnit.SECONDS)    // Define o timeout de leitura para 30 segundos
                .build();

        // --- Configuração da Janela Principal ---
        setTitle("GSmart - Configurador de Pipeline e Alertas v5.0");
        setSize(850, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // =================================================================
        //  PAINEL DE CONFIGURAÇÃO DA PIPELINE (Separador 1)
        // =================================================================

        // --- Painel Superior (Fonte de Dados e Destino) ---
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
        gbcTb.gridx = 0; gbcTb.gridy = 0; thingsboardConfigPanel.add(new JLabel("URL do Servidor:"), gbcTb);
        gbcTb.gridx = 1; thingsboardUrlField = new JTextField(); gbcTb.weightx = 1.0; gbcTb.fill = GridBagConstraints.HORIZONTAL; thingsboardConfigPanel.add(thingsboardUrlField, gbcTb);
        gbcTb.gridx = 2; tbConnectButton = new JButton("Conectar"); gbcTb.weightx = 0; gbcTb.fill = GridBagConstraints.NONE; thingsboardConfigPanel.add(tbConnectButton, gbcTb);
        gbcTb.gridx = 3; tbStatusLabel = new JLabel("Não conectado"); tbStatusLabel.setForeground(Color.GRAY); thingsboardConfigPanel.add(tbStatusLabel, gbcTb);
        gbcTb.gridx = 0; gbcTb.gridy = 1; thingsboardConfigPanel.add(new JLabel("Perfil de Dispositivo (Tipo):"), gbcTb);
        gbcTb.gridx = 1; deviceProfileSelector = new JComboBox<>(); gbcTb.gridwidth = 3; gbcTb.fill = GridBagConstraints.HORIZONTAL; deviceProfileSelector.setEnabled(false); thingsboardConfigPanel.add(deviceProfileSelector, gbcTb);
        gbcTb.gridx = 0; gbcTb.gridy = 2; gbcTb.gridwidth = 1; thingsboardConfigPanel.add(new JLabel("Dispositivo:"), gbcTb);
        gbcTb.gridx = 1; deviceSelector = new JComboBox<>(); gbcTb.gridwidth = 3; deviceSelector.setEnabled(false); thingsboardConfigPanel.add(deviceSelector, gbcTb);

        databaseConfigPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbcDb = new GridBagConstraints();
        gbcDb.insets = new Insets(2, 5, 2, 5); gbcDb.anchor = GridBagConstraints.WEST;
        gbcDb.gridx = 0; gbcDb.gridy = 0; gbcDb.gridwidth = 1; databaseConfigPanel.add(new JLabel("URL do Banco (JDBC):"), gbcDb);
        gbcDb.gridx = 1; gbcDb.gridy = 0; gbcDb.gridwidth = 3; gbcDb.weightx = 1.0; gbcDb.fill = GridBagConstraints.HORIZONTAL; dbUrlField = new JTextField(); databaseConfigPanel.add(dbUrlField, gbcDb);
        gbcDb.gridx = 4; gbcDb.gridy = 0; gbcDb.gridwidth = 1; gbcDb.fill = GridBagConstraints.NONE; dbConnectButton = new JButton("Conectar"); databaseConfigPanel.add(dbConnectButton, gbcDb);
        gbcDb.gridx = 5; gbcDb.gridy = 0; dbStatusLabel = new JLabel("Não conectado"); dbStatusLabel.setForeground(Color.GRAY); databaseConfigPanel.add(dbStatusLabel, gbcDb);
        gbcDb.gridx = 0; gbcDb.gridy = 1; gbcDb.gridwidth = 1; databaseConfigPanel.add(new JLabel("Usuário:"), gbcDb);
        gbcDb.gridx = 1; gbcDb.gridy = 1; gbcDb.gridwidth = 3; gbcDb.weightx = 1.0; gbcDb.fill = GridBagConstraints.HORIZONTAL; dbUserField = new JTextField(); databaseConfigPanel.add(dbUserField, gbcDb);
        gbcDb.gridx = 0; gbcDb.gridy = 2; gbcDb.gridwidth = 1; databaseConfigPanel.add(new JLabel("Senha:"), gbcDb);
        gbcDb.gridx = 1; gbcDb.gridy = 2; gbcDb.gridwidth = 3; gbcDb.weightx = 1.0; gbcDb.fill = GridBagConstraints.HORIZONTAL; dbPasswordField = new JPasswordField(""); databaseConfigPanel.add(dbPasswordField, gbcDb);
        gbcDb.gridx = 0; gbcDb.gridy = 3; gbcDb.gridwidth = 1; databaseConfigPanel.add(new JLabel("Tabela:"), gbcDb);
        gbcDb.gridx = 1; gbcDb.gridy = 3; gbcDb.gridwidth = 3; gbcDb.weightx = 1.0; gbcDb.fill = GridBagConstraints.HORIZONTAL; dbTableSelector = new JComboBox<>(); dbTableSelector.setEnabled(false); databaseConfigPanel.add(dbTableSelector, gbcDb);

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

        destinationPanel.add(new JLabel("URL do Broker MQTT:"));
        mqttBrokerUrlField = new JTextField("tcp://localhost:1883");
        destinationPanel.add(mqttBrokerUrlField);

        JPanel telegramPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        telegramPanel.setBorder(BorderFactory.createTitledBorder("Configurar Notificações do Telegram"));
        telegramPanel.add(new JLabel("Token do Bot:"));
        telegramTokenField = new JTextField(30);
        telegramPanel.add(telegramTokenField);
        telegramPanel.add(new JLabel("Chat ID:"));
        telegramChatIdField = new JTextField(15);
        telegramPanel.add(telegramChatIdField);


        //painel principal de configuração
        topConfigurationPanel.add(sourceSelectionPanel);
        topConfigurationPanel.add(sourceConfigCardPanel);
        topConfigurationPanel.add(loadKeysPanel);
        topConfigurationPanel.add(destinationPanel);
        topConfigurationPanel.add(telegramPanel);

        // --- Tabela de Métricas ---
        tableModel = new MetricTableModel();
        metricsTable = new JTable(tableModel);
        SystemMetricCellRenderer systemMetricRenderer = new SystemMetricCellRenderer();
        metricsTable.setDefaultRenderer(Object.class, systemMetricRenderer);
        metricsTable.setDefaultRenderer(Boolean.class, systemMetricRenderer);
        metricsTable.setFillsViewportHeight(true);
        metricsTable.getColumnModel().getColumn(0).setMaxWidth(60);
        metricsTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        metricsTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        metricsTable.getColumnModel().getColumn(3).setPreferredWidth(180);
        JScrollPane keysScrollPane = new JScrollPane(metricsTable);
        keysScrollPane.setBorder(BorderFactory.createTitledBorder("Selecionar, Mapear e Transformar Métricas"));

        // --- Painel de Ações da Pipeline ---
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15));
        startButton = new JButton("Iniciar Pipeline");
        startButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        actionPanel.add(startButton);


        JPanel adminPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        monitoringButton = new JButton("Central de Monitoramento");
        stopAllButton = new JButton("Parar Monitoramento");
        stopAllButton.setForeground(Color.RED);
        JPopupMenu logsPopupMenu = new JPopupMenu();
        JMenuItem generalLogItem = new JMenuItem("Log Geral");
        JMenuItem reconexLogItem = new JMenuItem("Log de Reconexão");
        logsPopupMenu.add(generalLogItem);
        logsPopupMenu.add(reconexLogItem);
        JButton logsButton = new JButton("Ver Logs");
        logsButton.addActionListener(e -> logsPopupMenu.show(logsButton, 0, logsButton.getHeight()));
        adminPanel.add(monitoringButton);
        adminPanel.add(stopAllButton);
        adminPanel.add(logsButton);

        JPanel bottomPanel = new JPanel(new BorderLayout(10, 0));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        bottomPanel.add(actionPanel, BorderLayout.WEST);
        bottomPanel.add(adminPanel, BorderLayout.CENTER);

        // --- Montagem do Painel do Separador 1 ---
        JPanel pipelineConfigPanel = new JPanel(new BorderLayout(5, 5));
        pipelineConfigPanel.add(topConfigurationPanel, BorderLayout.NORTH);
        pipelineConfigPanel.add(keysScrollPane, BorderLayout.CENTER);
        pipelineConfigPanel.add(bottomPanel, BorderLayout.SOUTH);

        // =================================================================
        //  PAINEL DE REGRAS DE ALERTA (Separador 2)
        // =================================================================

        JPanel alertRulesPanel = new JPanel(new BorderLayout(5, 5));
        alertRulesPanel.setBorder(BorderFactory.createTitledBorder("Configurador de Alertas Customizados"));

        alertRuleTableModel = new AlertRuleTableModel();
        alertRuleTableModel.setRules(this.alertRules);
        alertRulesTable = new JTable(alertRuleTableModel);
        alertRulesTable.setFillsViewportHeight(true);
        alertRulesTable.getColumnModel().getColumn(0).setMaxWidth(50);

        JScrollPane rulesScrollPane = new JScrollPane(alertRulesTable);
        alertRulesPanel.add(rulesScrollPane, BorderLayout.CENTER);

        JPanel ruleButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addRuleButton = new JButton("Adicionar Regra");
        JButton editRuleButton = new JButton("Editar Regra");
        JButton removeRuleButton = new JButton("Remover Regra");
        ruleButtonsPanel.add(addRuleButton);
        ruleButtonsPanel.add(editRuleButton);
        ruleButtonsPanel.add(removeRuleButton);
        alertRulesPanel.add(ruleButtonsPanel, BorderLayout.SOUTH);

        // =================================================================
        //  PAINEL DE REGRAS DE INSIGHT (Separador 3)
        // =================================================================
        JPanel insightRulesPanel = new JPanel(new BorderLayout(5, 5));
        insightRulesPanel.setBorder(BorderFactory.createTitledBorder("Configurador de Alarmes Inteligentes"));

        insightRuleTableModel = new InsightRuleTableModel();
        insightRuleTableModel.setRules(this.insightRules);
        insightRulesTable = new JTable(insightRuleTableModel);
        insightRulesTable.setFillsViewportHeight(true);
        insightRulesTable.getColumnModel().getColumn(0).setMaxWidth(50);

        JScrollPane insightScrollPane = new JScrollPane(insightRulesTable);
        insightRulesPanel.add(insightScrollPane, BorderLayout.CENTER);

        JPanel insightButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addInsightButton = new JButton("Adicionar Regra de Alarme");
        JButton editInsightButton = new JButton("Editar Regra de Alarme");
        JButton removeInsightButton = new JButton("Remover Regra de Alarme");
        insightButtonsPanel.add(addInsightButton);
        insightButtonsPanel.add(editInsightButton);
        insightButtonsPanel.add(removeInsightButton);
        insightRulesPanel.add(insightButtonsPanel, BorderLayout.SOUTH);

        // =================================================================
        //  MONTAGEM FINAL COM SEPARADORES
        // =================================================================

        JTabbedPane mainTabbedPane = new JTabbedPane();
        mainTabbedPane.addTab("Configuração da Pipeline", pipelineConfigPanel);
        mainTabbedPane.addTab("Regras de Alerta", alertRulesPanel);
        mainTabbedPane.addTab("Regras de Alarmes", insightRulesPanel); // Adiciona o novo separador


        // Adiciona o painel de separadores à janela principal
        this.setContentPane(mainTabbedPane);

        // =================================================================
        //  LISTENERS DE EVENTOS
        // =================================================================

        // Listeners da Pipeline
        sourceSelector.addItemListener(e -> toggleSourceFields());
        tbConnectButton.addActionListener(e -> connectToThingsboard());
        dbConnectButton.addActionListener(e -> connectToDatabase());
        deviceProfileSelector.addItemListener(e -> { if (e.getStateChange() == ItemEvent.SELECTED) loadDevicesByProfile(); });
        loadKeysButton.addActionListener(e -> loadAvailableKeys());
        startButton.addActionListener(e -> launchPipeline());
        stopAllButton.addActionListener(e -> stopAllPipelines());
        monitoringButton.addActionListener(e -> showTaskManager());
        generalLogItem.addActionListener(e -> this.globalLogViewer.setVisible(true));
        reconexLogItem.addActionListener(e -> showReconnectionLog());

        // --- Listeners das Regras de Alerta ---

        addRuleButton.addActionListener(e -> {
            // Pega a lista de métricas disponíveis da outra tabela
            List<String> availableMetrics = tableModel.getSelectedMetrics().stream()
                    .map(MetricConfig::getOriginalName)
                    .collect(Collectors.toList());

            if (availableMetrics.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor, carregue e selecione as métricas no separador 'Configuração da Pipeline' primeiro.", "Métricas não encontradas", JOptionPane.WARNING_MESSAGE);
                return;
            }

            AlertRuleDialog dialog = new AlertRuleDialog(this, "Adicionar Nova Regra de Alerta", availableMetrics);
            dialog.setVisible(true);

            AlertRule newRule = dialog.getAlertRule();
            if (newRule != null) {
                alertRuleTableModel.addRule(newRule);
            }
        });

        editRuleButton.addActionListener(e -> {
            int selectedRow = alertRulesTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Por favor, selecione uma regra na tabela para editar.", "Nenhuma Regra Selecionada", JOptionPane.WARNING_MESSAGE);
                return;
            }

            AlertRule ruleToEdit = alertRuleTableModel.getRuleAt(selectedRow);
            List<String> availableMetrics = tableModel.getSelectedMetrics().stream()
                    .map(MetricConfig::getOriginalName)
                    .collect(Collectors.toList());

            AlertRuleDialog dialog = new AlertRuleDialog(this, "Editar Regra de Alerta", availableMetrics);
            dialog.setAlertRule(ruleToEdit); // Pré-preenche o formulário
            dialog.setVisible(true);

            AlertRule updatedRule = dialog.getAlertRule();
            if (updatedRule != null) {
                alertRuleTableModel.updateRule(selectedRow, updatedRule);
            }
        });

        removeRuleButton.addActionListener(e -> {
            int selectedRow = alertRulesTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Por favor, selecione uma regra na tabela para remover.", "Nenhuma Regra Selecionada", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, "Tem a certeza que deseja remover a regra selecionada?", "Confirmar Remoção", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                alertRuleTableModel.removeRule(selectedRow);
            }
        });

        // --- Listeners das Regras de Insight ---
        addInsightButton.addActionListener(e -> {
            List<String> availableMetrics = tableModel.getSelectedMetrics().stream()
                    .map(MetricConfig::getOriginalName)
                    .collect(Collectors.toList());
            if (availableMetrics.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor, carregue e selecione as métricas no separador 'Configuração da Pipeline' primeiro.", "Métricas não encontradas", JOptionPane.WARNING_MESSAGE);
                return;
            }
            InsightRuleDialog dialog = new InsightRuleDialog(this, "Adicionar Nova Regra de Insight", availableMetrics);
            dialog.setVisible(true);
            InsightRule newRule = dialog.getInsightRule();
            if (newRule != null) {
                insightRuleTableModel.addRule(newRule);
            }
        });

        editInsightButton.addActionListener(e -> {
            int selectedRow = insightRulesTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Por favor, selecione uma regra na tabela para editar.", "Nenhuma Regra Selecionada", JOptionPane.WARNING_MESSAGE);
                return;
            }
            InsightRule ruleToEdit = insightRuleTableModel.getRuleAt(selectedRow);
            List<String> availableMetrics = tableModel.getSelectedMetrics().stream()
                    .map(MetricConfig::getOriginalName)
                    .collect(Collectors.toList());
            InsightRuleDialog dialog = new InsightRuleDialog(this, "Editar Regra de Insight", availableMetrics);
            dialog.setInsightRule(ruleToEdit);
            dialog.setVisible(true);
            InsightRule updatedRule = dialog.getInsightRule();
            if (updatedRule != null) {
                insightRuleTableModel.updateRule(selectedRow, updatedRule);
            }
        });

        removeInsightButton.addActionListener(e -> {
            int selectedRow = insightRulesTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Por favor, selecione uma regra para remover.", "Nenhuma Regra Selecionada", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "Tem a certeza que deseja remover a regra selecionada?", "Confirmar Remoção", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                insightRuleTableModel.removeRule(selectedRow);
            }
        });

        // Listener da Janela
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveConfiguration();
            }
        });


        // Carregamento inicial
        toggleSourceFields();
        loadConfiguration();
    }

    /**
     * Exibe a janela de logs de reconexão.
     * Se a janela ainda não existir, uma nova é criada. Se já existir,
     * ela é trazida para a frente e o seu conteúdo é recarregado.
     */
    private void showReconnectionLog() {
        if (reconnectionLogViewer == null || !reconnectionLogViewer.isDisplayable()) {
            reconnectionLogViewer = new ReconnectionLogViewer();
        }
        reconnectionLogViewer.loadLogFile();
        reconnectionLogViewer.setVisible(true);
        reconnectionLogViewer.toFront();
    }

    /**
     * Carrega as configurações da última sessão a partir do ficheiro gsmart.properties.
     * Isto inclui URLs e a última fonte de dados selecionada, melhorando a experiência do utilizador.
     */
    private void loadConfiguration() {
        logger.info("Carregando configurações salvas...");
        Properties props = configManager.loadProperties();
        thingsboardUrlField.setText(props.getProperty("thingsboard.url", "http://10.8.0.5:8080"));
        dbUrlField.setText(props.getProperty("db.url", "jdbc:postgresql://localhost:5432/seu_banco"));
        dbUserField.setText(props.getProperty("db.user", "postgres"));
        pbiUrlField.setText(props.getProperty("powerbi.url", ""));
        mqttBrokerUrlField.setText(props.getProperty("mqtt.broker.url", "tcp://localhost:1883"));
        sourceSelector.setSelectedItem(props.getProperty("source.last", "Thingsboard API"));
        sourceSelector.setSelectedItem(props.getProperty("source.last", "Thingsboard API"));
        telegramTokenField.setText(props.getProperty("telegram.token", ""));
        telegramChatIdField.setText(props.getProperty("telegram.chat_id", ""));
    }

    /**
     * Salva as configurações atuais (URLs, etc.) no ficheiro gsmart.properties.
     * Este método é chamado automaticamente quando a janela da aplicação é fechada.
     */
    private void saveConfiguration() {
        logger.info("Salvando configurações antes de fechar...");
        Properties props = new Properties();
        props.setProperty("thingsboard.url", thingsboardUrlField.getText());
        props.setProperty("db.url", dbUrlField.getText());
        props.setProperty("db.user", dbUserField.getText());
        props.setProperty("powerbi.url", pbiUrlField.getText());
        props.setProperty("source.last", (String) sourceSelector.getSelectedItem());
        props.setProperty("mqtt.broker.url", mqttBrokerUrlField.getText());
        props.setProperty("source.last", (String) sourceSelector.getSelectedItem());
        props.setProperty("telegram.token", telegramTokenField.getText());
        props.setProperty("telegram.chat_id", telegramChatIdField.getText());
        configManager.saveProperties(props);
    }

    /**
     * Orquestra o lançamento de uma nova tarefa de pipeline.
     * Recolhe todas as configurações da interface (fonte de dados, métricas, regras de alerta e alarme),
     * cria um objeto {@code PipelineConfiguration} e entrega-o ao {@code PipelineManager} para execução.
     */
    private void launchPipeline() {
        if (metricsTable.isEditing()) {
            metricsTable.getCellEditor().stopCellEditing();
        }
        try {
            String pbiUrl = pbiUrlField.getText().trim();
            if (pbiUrl.isEmpty() || !pbiUrl.toLowerCase().startsWith("http")) {
                JOptionPane.showMessageDialog(this, "Por favor, insira uma URL de Push do Power BI válida (deve começar com http ou https).", "Erro de Configuração", JOptionPane.ERROR_MESSAGE);
                return;
            }
            List<MetricConfig> selectedConfigs = tableModel.getSelectedMetrics();
            if (selectedConfigs.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nenhuma métrica foi selecionada para envio!", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }



            IDataSource selectedDataSource = createSelectedDataSource(selectedConfigs.stream().map(MetricConfig::getOriginalName).collect(Collectors.toList()));
            // Obtenha as regras atuais da tabela
            List<AlertRule> currentAlertRules = alertRuleTableModel.getRules();
            List<InsightRule> currentInsightRules = insightRuleTableModel.getRules();
            String mqttBrokerUrl = mqttBrokerUrlField.getText().trim();
            String telegramToken = telegramTokenField.getText().trim();
            String telegramChatId = telegramChatIdField.getText().trim();// Obtenha as regras de insight
            PipelineConfiguration config = new PipelineConfiguration(selectedDataSource, pbiUrl, selectedConfigs, this.globalLogViewer, currentAlertRules, currentInsightRules, telegramToken, telegramChatId, mqttBrokerUrl);
            pipelineManager.launchPipeline(config);
            JOptionPane.showMessageDialog(this, "Pipeline para '" + selectedDataSource.getSourceName() + "' iniciada em segundo plano.\nAbra a 'Central de Monitoramento' para visualizar.", "Pipeline Iniciada", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            logger.error("Falha ao preparar a pipeline: {}", e.getMessage(), e);
            JOptionPane.showMessageDialog(this, "Falha ao preparar a pipeline:\n" + e.getMessage(), "Erro Crítico", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Delega ao PipelineManager a tarefa de parar todas as pipelines em execução,
     * geralmente após uma confirmação do utilizador.
     */
    private void stopAllPipelines() {
        pipelineManager.stopAllPipelines();
    }

    /**
     * Exibe a "Central de Monitoramento".
     * Se a janela ainda não existir, uma nova é criada. Se já existir,
     * é simplesmente trazida para a frente.
     */
    private void showTaskManager() {
        if (taskManagerWindow == null || !taskManagerWindow.isDisplayable()) {
            taskManagerWindow = new TaskManagerWindow(this.pipelineManager);
            taskManagerWindow.setLocationRelativeTo(this);
        }
        taskManagerWindow.setVisible(true);
        taskManagerWindow.toFront();
    }

    /**
     * Alterna a visibilidade dos painéis de configuração de fonte de dados (ThingsBoard ou Base de Dados)
     * com base na seleção do utilizador no JComboBox principal.
     */
    private void toggleSourceFields() {
        CardLayout cl = (CardLayout) (sourceConfigCardPanel.getLayout());
        String selectedSource = (String) sourceSelector.getSelectedItem();
        if (selectedSource != null) {
            cl.show(sourceConfigCardPanel, selectedSource);
        }
    }

    /**
     * Obtém e valida a URL do servidor ThingsBoard a partir do campo de texto correspondente.
     * Lança uma IllegalStateException se o campo estiver vazio.
     * @return A URL do ThingsBoard como uma String.
     */
    private String getThingsboardUrl() {
        String url = thingsboardUrlField.getText().trim();
        if (url.isEmpty()) {
            JOptionPane.showMessageDialog(this, "A URL do Servidor ThingsBoard não pode estar vazia.", "Erro de Configuração", JOptionPane.ERROR_MESSAGE);
            throw new IllegalStateException("URL do ThingsBoard não fornecida.");
        }
        return url;
    }

    /**
     * Tenta estabelecer uma conexão com o servidor ThingsBoard.
     * Se bem-sucedido, ativa os seletores de perfil de dispositivo e carrega os perfis disponíveis.
     * Utiliza um {@code SwingWorker} para não bloquear a interface durante a conexão.
     */
    private void connectToThingsboard() {
        tbStatusLabel.setText("Conectando...");
        tbStatusLabel.setForeground(Color.ORANGE);
        tbConnectButton.setEnabled(false);
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                try {
                    return new ThingsBoardSource(getThingsboardUrl(), null, null, sharedOkHttpClient).testConnection();
                } catch (Exception e) {
                    return false;
                }
            }
            @Override
            protected void done() {
                try {
                    if (get()) {
                        tbStatusLabel.setText("Conectado");
                        tbStatusLabel.setForeground(new Color(0, 128, 0));
                        deviceProfileSelector.setEnabled(true);
                        loadDeviceProfiles();
                    } else {
                        throw new Exception("Falha na autenticação ou URL incorreta.");
                    }
                } catch (Exception e) {
                    tbStatusLabel.setText("Falha!");
                    tbStatusLabel.setForeground(Color.RED);
                } finally {
                    tbConnectButton.setEnabled(true);
                }
            }
        }.execute();
    }

    /**
     * Tenta estabelecer uma conexão com a base de dados configurada via JDBC.
     * Se bem-sucedido, ativa o seletor de tabelas e carrega as tabelas disponíveis.
     * Utiliza um {@code SwingWorker} para não bloquear a interface durante a conexão.
     */
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

    /**
     * Carrega as chaves de telemetria (ThingsBoard) ou os nomes das colunas (Base de Dados)
     * da fonte de dados selecionada e popula a tabela de métricas na interface.
     * Utiliza um {@code SwingWorker} para executar a operação em segundo plano.
     */
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
                    @Override
                    protected List<String> doInBackground() throws Exception {
                        return new ThingsBoardSource(getThingsboardUrl(), selectedDevice.id(), null, sharedOkHttpClient).getAvailableKeys();
                    }
                    @Override
                    protected void done() {
                        handleKeysLoaded(this);
                    }
                }.execute();
            } else if ("Banco de Dados Espelho".equals(selectedSource)) {
                String selectedTable = (String) dbTableSelector.getSelectedItem();
                if (selectedTable == null) {
                    JOptionPane.showMessageDialog(this, "Por favor, conecte e selecione uma tabela primeiro.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    setLoadButtonReady();
                    return;
                }
                new SwingWorker<List<String>, Void>() {
                    @Override
                    protected List<String> doInBackground() throws Exception {
                        String dbUrl = dbUrlField.getText().trim();
                        String dbUser = dbUserField.getText().trim();
                        String dbPassword = new String(dbPasswordField.getPassword());
                        return new DatabaseSource(dbUrl, dbUser, dbPassword, selectedTable, null).getAvailableColumns(selectedTable);
                    }
                    @Override
                    protected void done() {
                        handleKeysLoaded(this);
                    }
                }.execute();
            }
        } catch (IllegalStateException e) {
            logger.error("Pré-condição para carregar chaves falhou: {}", e.getMessage());
            setLoadButtonReady();
        }
    }

    /**
     * Processa o resultado do SwingWorker que busca as métricas/colunas.
     * Popula a tabela de métricas com os dados recebidos ou exibe uma mensagem de erro.
     * @param worker O SwingWorker que completou a sua execução.
     */
    private void handleKeysLoaded(SwingWorker<List<String>, Void> worker) {
        try {
            List<String> keys = worker.get();
            if (keys.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nenhuma métrica ou coluna foi encontrada para esta fonte!", "Aviso", JOptionPane.WARNING_MESSAGE);
                tableModel.clearMetrics();
                return;
            }





            List<MetricConfig> configs = keys.stream().map(MetricConfig::new).collect(Collectors.toList());
            configs.add(0, new MetricConfig("OrigemDados", true, true));
            //configs.add(0, new MetricConfig("HdDev", true, true));
            configs.add(0, new MetricConfig("HoraDev", true, true));
            configs.add(0, new MetricConfig("DataDev", true, true));
            configs.add(0, new MetricConfig("timestamp", true, true));
            configs.add(0, new MetricConfig("AlertaCritico", true, true));
            tableModel.setMetrics(configs);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Falha ao carregar as métricas/colunas:\n" + e.getCause().getMessage(), "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
        } finally {
            setLoadButtonReady();
        }
    }

    /**
     * Restaura o estado do botão "Carregar Métricas", reativando-o e
     * redefinindo o seu texto para o estado inicial.
     */
    private void setLoadButtonReady() {
        loadKeysButton.setEnabled(true);
        loadKeysButton.setText("Carregar Métricas da Fonte");
    }

    /**
     * Cria e retorna uma instância da fonte de dados (IDataSource) apropriada
     * com base na seleção do utilizador na interface.
     * @param originalKeys A lista de métricas/colunas que a fonte de dados deve buscar.
     * @return Uma instância de ThingsBoardSource ou DatabaseSource.
     * @throws Exception Se a conexão com a fonte de dados falhar ou a configuração for inválida.
     */
    private IDataSource createSelectedDataSource(List<String> originalKeys) throws Exception {
        String selectedSource = (String) sourceSelector.getSelectedItem();
        if ("Thingsboard API".equals(selectedSource)) {
            String tbUrl = getThingsboardUrl();
            Device selectedDevice = (Device) deviceSelector.getSelectedItem();
            if (selectedDevice == null) {
                throw new IllegalStateException("Nenhum dispositivo do ThingsBoard foi selecionado.");
            }
            ThingsBoardSource tbSource = new ThingsBoardSource(tbUrl, selectedDevice.id(), originalKeys, sharedOkHttpClient);
            tbSource.testConnectionAndThrow();
            return tbSource;

        } else if ("Banco de Dados Espelho".equals(selectedSource)) {
            String dbUrl = dbUrlField.getText().trim();
            String dbUser = dbUserField.getText().trim();
            String dbPassword = new String(dbPasswordField.getPassword());
            String dbTable = (String) dbTableSelector.getSelectedItem();
            if (dbTable == null) {
                throw new IllegalStateException("Nenhuma tabela do banco de dados foi selecionada.");
            }
            DatabaseSource dbSource = new DatabaseSource(dbUrl, dbUser, dbPassword, dbTable, originalKeys);
            dbSource.testConnectionAndThrow();
            return dbSource;
        }
        throw new IllegalStateException("Nenhuma fonte de dados válida foi selecionada.");
    }

    /**
     * Exibe um diálogo de seleção (JOptionPane) com uma lista de opções.
     * @param options A lista de strings a serem exibidas no dropdown.
     * @param title O título da janela de diálogo.
     * @param message A mensagem a ser exibida ao utilizador.
     * @return A string selecionada pelo utilizador ou null se o diálogo for cancelado.
     */
    private String showDropdownDialog(List<String> options, String title, String message) {
        if (options == null || options.isEmpty()) return null;
        Object[] possibilities = options.toArray();
        return (String) JOptionPane.showInputDialog(this, message, title, JOptionPane.PLAIN_MESSAGE, null, possibilities, options.get(0));
    }

    /**
     * Carrega a lista de Perfis de Dispositivo do servidor ThingsBoard e popula
     * o JComboBox correspondente.
     */
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

    /**
     * Carrega a lista de Dispositivos associados a um Perfil de Dispositivo específico
     * do servidor ThingsBoard e popula o JComboBox correspondente.
     */
    private void loadDevicesByProfile() {
        DeviceProfile selectedProfile = (DeviceProfile) deviceProfileSelector.getSelectedItem();
        if (selectedProfile == null) {
            deviceSelector.removeAllItems();
            return;
        }
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