// Localização: src/main/java/com/gsmart/GSmartGui.java
package main.java.com.gsmart;

import main.java.com.gsmart.Gui.AlertRuleTableModel;
import main.java.com.gsmart.Gui.InsightRuleTableModel;
import main.java.com.gsmart.Gui.MetricTableModel;
import main.java.com.gsmart.Gui.panels.DashboardPanel;
import main.java.com.gsmart.Gui.windows.*;
import main.java.com.gsmart.config.ConfigManager;
import main.java.com.gsmart.config.MetricConfig;
import main.java.com.gsmart.config.PipelineConfiguration;
import main.java.com.gsmart.config.SerializablePipelineConfig;
import main.java.com.gsmart.controller.ActionController;
import main.java.com.gsmart.controller.DataSourceController;
import main.java.com.gsmart.controller.UIController;
import main.java.com.gsmart.pipeline.PipelineManager;
import main.java.com.gsmart.pipeline.PipelineTask;
import main.java.com.gsmart.resources.IDataSource;
import main.java.com.gsmart.sources.DatabaseSource;
import main.java.com.gsmart.sources.Device;
import main.java.com.gsmart.sources.DeviceProfile;

import main.java.com.gsmart.sources.ThingsBoardSource;
import okhttp3.OkHttpClient;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

import static main.java.com.gsmart.pipeline.DataPipeline.logger;

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
 * @see main.java.com.gsmart.pipeline.PipelineManager
 */
public class GSmartGui extends JFrame {
    // --- Modelos e Gestores ---
    private final PipelineManager pipelineManager;
    private final ConfigManager configManager;
    private final LogViewerWindow globalLogViewer;
    private TaskManagerWindow taskManagerWindow;
    private ReconnectionLogViewer reconnectionLogViewer;

    // --- Componentes da UI (Acessados por múltiplos controladores) ---
    private final DashboardPanel dashboardPanel;
    private final JComboBox<String> sourceSelector;
    private final JComboBox<String> destinationSelector;
    private final JTextField thingsboardUrlField, dbUrlField, dbUserField, pbiUrlField, fabricConnectionStringField, mqttBrokerUrlField, telegramTokenField, telegramChatIdField;
    private final JPasswordField dbPasswordField;
    private final JButton tbConnectButton, dbConnectButton, startButton, stopAllButton, monitoringButton, manageUsersButton;
    private final JLabel tbStatusLabel, dbStatusLabel;
    private final JComboBox<DeviceProfile> deviceProfileSelector;
    private final JComboBox<Device> deviceSelector;
    private final JComboBox<String> dbTableSelector;
    private final JPanel sourceConfigCardPanel, destinationConfigCardPanel, thingsboardConfigPanel, databaseConfigPanel;
    private final JTable metricsTable, alertRulesTable, insightRulesTable;
    private final MetricTableModel metricTableModel;
    private final AlertRuleTableModel alertRuleTableModel;
    private final InsightRuleTableModel insightRuleTableModel;
    private JPanel alertRuleButtons, insightRuleButtons;
    private JCheckBox runInBackgroundCheckBox;

    // --- Outros ---
    private final OkHttpClient sharedOkHttpClient;
    private final String currentUserRole;

    /**
     * Construtor da janela principal da aplicação GSmart.
     * <p>
     * Inicializa todos os componentes da interface gráfica, configura os painéis,
     * tabelas e listeners de eventos, e carrega as configurações da sessão anterior.
     *
     * @param logViewer       A instância partilhada do visualizador de logs gerais.
     * @param pipelineManager O gestor central que orquestra todas as tarefas de pipeline.
     */
    public GSmartGui(LogViewerWindow logViewer, PipelineManager pipelineManager, String userRole) {
        // --- Inicialização de Variáveis e Gestores ---
        this.globalLogViewer = logViewer;
        this.pipelineManager = pipelineManager;
        this.configManager = new ConfigManager();
        this.currentUserRole = userRole;
        this.pipelineManager.setParentComponent(this);
        this.pipelineManager.setGlobalLogViewer(this.globalLogViewer);
        this.sharedOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        // --- Inicialização de Componentes da UI ---
        this.dashboardPanel = new DashboardPanel();
        this.sourceSelector = new JComboBox<>(new String[]{"Thingsboard API", "Banco de Dados Espelho"});
        this.destinationSelector = new JComboBox<>(new String[]{"Power BI Push URL", "Fabric Eventstream"});
        this.thingsboardUrlField = new JTextField();
        this.dbUrlField = new JTextField();
        this.dbUserField = new JTextField();
        this.dbPasswordField = new JPasswordField();
        this.pbiUrlField = new JTextField(45);
        this.fabricConnectionStringField = new JTextField(45);
        this.mqttBrokerUrlField = new JTextField("tcp://localhost:1883", 20);
        this.telegramTokenField = new JTextField(30);
        this.telegramChatIdField = new JTextField(15);
        this.tbConnectButton = new JButton("Conectar");
        this.dbConnectButton = new JButton("Conectar");
        this.startButton = new JButton("Iniciar Pipeline");
        this.startButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        this.stopAllButton = new JButton("Parar Monitoramento");
        this.stopAllButton.setForeground(Color.RED);
        this.monitoringButton = new JButton("Central de Monitoramento");
        this.manageUsersButton = new JButton("Gerir Utilizadores");
        this.tbStatusLabel = new JLabel("Não conectado");
        this.tbStatusLabel.setForeground(Color.GRAY);
        this.dbStatusLabel = new JLabel("Não conectado");
        this.dbStatusLabel.setForeground(Color.GRAY);
        this.deviceProfileSelector = new JComboBox<>();
        this.deviceProfileSelector.setEnabled(false);
        this.deviceSelector = new JComboBox<>();
        this.deviceSelector.setEnabled(false);
        this.dbTableSelector = new JComboBox<>();
        this.dbTableSelector.setEnabled(false);
        this.sourceConfigCardPanel = new JPanel(new CardLayout());
        this.destinationConfigCardPanel = new JPanel(new CardLayout());
        this.thingsboardConfigPanel = new JPanel();
        this.databaseConfigPanel = new JPanel();
        this.metricTableModel = new MetricTableModel();
        this.metricsTable = new JTable(metricTableModel);
        this.alertRuleTableModel = new AlertRuleTableModel();
        this.alertRulesTable = new JTable(alertRuleTableModel);
        this.insightRuleTableModel = new InsightRuleTableModel();
        this.insightRulesTable = new JTable(insightRuleTableModel);
        this.runInBackgroundCheckBox = new JCheckBox("Executar em 2º plano ao fechar");
        this.runInBackgroundCheckBox.setToolTipText("Se marcado, o GSmart continuará a correr na bandeja do sistema ao invés de fechar.");

        // --- Inicialização dos Controladores ---
        new UIController(this);
        DataSourceController dataSourceController = new DataSourceController(this);
        new ActionController(this, dataSourceController);

        // --- Configuração Final ---
        loadConfiguration();
        applyRolePermissions();
        loadAndStartPipelines();
        setupSystemTray();
    }

    /**
     * Exibe a janela de logs de reconexão.
     * Se a janela ainda não existir, uma nova é criada. Se já existir,
     * ela é trazida para a frente e o seu conteúdo é recarregado.
     */
    public void showReconnectionLog() {
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
        Properties props = configManager.loadProperties();
        thingsboardUrlField.setText(props.getProperty("thingsboard.url", "http://10.8.0.5:8080"));
        destinationSelector.setSelectedItem(props.getProperty("destination.last", "Power BI Push URL"));
        fabricConnectionStringField.setText(props.getProperty("fabric.connectionstring", ""));
        dbUrlField.setText(props.getProperty("db.url", "jdbc:postgresql://localhost:5432/seu_banco"));
        dbUserField.setText(props.getProperty("db.user", "postgres"));
        pbiUrlField.setText(props.getProperty("powerbi.url", ""));
        mqttBrokerUrlField.setText(props.getProperty("mqtt.broker.url", "tcp://localhost:1883"));
        sourceSelector.setSelectedItem(props.getProperty("source.last", "Thingsboard API"));
        telegramTokenField.setText(props.getProperty("telegram.token", ""));
        telegramChatIdField.setText(props.getProperty("telegram.chat_id", ""));
        alertRuleTableModel.setRules(configManager.loadAlertRules());
        insightRuleTableModel.setRules(configManager.loadInsightRules());
        runInBackgroundCheckBox.setSelected(Boolean.parseBoolean(props.getProperty("system.runInBackground", "false")));
    }

    /**
     * Salva as configurações atuais (URLs, etc.) no ficheiro gsmart.properties.
     * Este método é chamado automaticamente quando a janela da aplicação é fechada.
     */
    private void saveConfiguration() {
        Properties props = new Properties();
        props.setProperty("thingsboard.url", thingsboardUrlField.getText());
        props.setProperty("db.url", dbUrlField.getText());
        props.setProperty("db.user", dbUserField.getText());
        props.setProperty("powerbi.url", pbiUrlField.getText());
        props.setProperty("source.last", (String) sourceSelector.getSelectedItem());
        props.setProperty("destination.last", (String) destinationSelector.getSelectedItem());
        props.setProperty("fabric.connectionstring", fabricConnectionStringField.getText());
        props.setProperty("mqtt.broker.url", mqttBrokerUrlField.getText());
        props.setProperty("telegram.token", telegramTokenField.getText());
        props.setProperty("telegram.chat_id", telegramChatIdField.getText());
        props.setProperty("system.runInBackground", String.valueOf(runInBackgroundCheckBox.isSelected()));
        configManager.saveProperties(props);
        configManager.saveRules(alertRuleTableModel.getRules(), insightRuleTableModel.getRules());
        configManager.saveMetricConfigs(metricTableModel.getAllMetrics());
        List<SerializablePipelineConfig> activePipelinesToSave = new ArrayList<>();
        for (PipelineTask task : pipelineManager.getRunningTasks()) {
            activePipelinesToSave.add(convertToSerializable(task.getOriginalConfig()));
        }
        configManager.saveActivePipelines(activePipelinesToSave);
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
    public void showTaskManager() {
        if (taskManagerWindow == null || !taskManagerWindow.isDisplayable()) {
            taskManagerWindow = new TaskManagerWindow(this.pipelineManager);
            taskManagerWindow.setLocationRelativeTo(this);
            this.pipelineManager.addTaskListUpdatedListener(() -> {
                if (taskManagerWindow != null && taskManagerWindow.isDisplayable()) {
                    taskManagerWindow.updateTasks(this.pipelineManager.getRunningTasks());
                }
            });
        }
        taskManagerWindow.setVisible(true);
        taskManagerWindow.toFront();
    }

    /**
     * Alterna a visibilidade dos painéis de configuração de fonte de dados (ThingsBoard ou Base de Dados)
     * com base na seleção do utilizador no JComboBox principal.
     */
    public void toggleSourceFields() {
        CardLayout cl = (CardLayout) (sourceConfigCardPanel.getLayout());
        cl.show(sourceConfigCardPanel, (String) sourceSelector.getSelectedItem());
    }

    /**
     * Alterna a visibilidade dos painéis de configuração de envio de dados (POWER_BI ou MICROSFT_FABRIC)
     * com base na seleção do utilizador no JComboBox principal.
     */
    public void toggleDestinationFields() {
        CardLayout cl = (CardLayout) (destinationConfigCardPanel.getLayout());
        cl.show(destinationConfigCardPanel, (String) destinationSelector.getSelectedItem());
    }

    /**
     * Aplica permissões à interface com base no perfil do utilizador.
     * Desativa funcionalidades de edição e criação para perfis que não sejam "ADMINISTRATOR".
     */
    private void applyRolePermissions() {
        setTitle(getTitle() + " (Utilizador: " + this.currentUserRole + ")");
        boolean isAdmin = "ADMINISTRATOR".equals(this.currentUserRole);
        manageUsersButton.setVisible(isAdmin);
        if (!isAdmin) {
            if (alertRuleButtons != null) {
                for (Component comp : alertRuleButtons.getComponents()) {
                    comp.setEnabled(false);
                }
            }
            if (insightRuleButtons != null) {
                for (Component comp : insightRuleButtons.getComponents()) {
                    comp.setEnabled(false);
                }
            }
        }
    }

    /**
     * Cria e exibe a janela de gestão de utilizadores.
     */
    public void showUserManagementWindow() {
        new UserManagementWindow(this).setVisible(true);
    }

    public void showHelpWindow() {
        new HelpWindow(this).setVisible(true);
    }

    public void showAutoStartManager() {
        new AutoStartManagerWindow(this, configManager).setVisible(true);
    }

    /**
     * Converte uma PipelineConfiguration completa para a sua versão serializável.
     *
     * @param config A configuração original da pipeline.
     * @return Um objeto SerializablePipelineConfig pronto para ser guardado.
     */
    private SerializablePipelineConfig convertToSerializable(PipelineConfiguration config) {
        String dataSourceType = "";
        Map<String, String> dataSourceParams = new HashMap<>();

        if (config.dataSource() instanceof ThingsBoardSource tbSource) {
            dataSourceType = "ThingsBoard";
            dataSourceParams.put("url", tbSource.getThingsboardUrl());
            dataSourceParams.put("deviceId", tbSource.getDeviceId());
            dataSourceParams.put("deviceName", tbSource.getDeviceName());
        } else if (config.dataSource() instanceof DatabaseSource dbSource) {
            dataSourceType = "Database";
            dataSourceParams.put("url", dbSource.getDbUrl());
            dataSourceParams.put("user", dbSource.getUser());
            dataSourceParams.put("password", dbSource.getPassword());
            dataSourceParams.put("tableName", dbSource.getTableName());
        }

        return new SerializablePipelineConfig(
                config.destinationType(),
                config.destinationEndpoint(),
                config.mqttBrokerUrl(),
                config.telegramToken(),
                config.telegramChatId(),
                config.metricConfigs(),
                config.alertRules(),
                config.insightRules(),
                dataSourceType,
                dataSourceParams
        );
    }

    /**
     * Carrega as configurações das pipelines que estavam ativas na última sessão
     * e inicia-as automaticamente.
     */
    private void loadAndStartPipelines() {
        List<SerializablePipelineConfig> configsToStart = configManager.loadActivePipelines();
        logger.info("Encontradas {} pipelines para iniciar automaticamente.", configsToStart.size());

        for (SerializablePipelineConfig sConfig : configsToStart) {
            try {
                IDataSource dataSource = null;
                Map<String, String> params = sConfig.getDataSourceParams();

                // Reconstrói a IDataSource com base no tipo e nos parâmetros salvos
                if ("ThingsBoard".equals(sConfig.getDataSourceType())) {
                    List<String> keys = sConfig.getMetricConfigs().stream().map(MetricConfig::getOriginalName).collect(Collectors.toList());
                    dataSource = new ThingsBoardSource(params.get("url"), params.get("deviceId"), params.get("deviceName"), keys, sharedOkHttpClient);
                } else if ("Database".equals(sConfig.getDataSourceType())) {
                    List<String> keys = sConfig.getMetricConfigs().stream().map(MetricConfig::getOriginalName).collect(Collectors.toList());
                    dataSource = new DatabaseSource(params.get("url"), params.get("user"), params.get("password"), params.get("tableName"), keys);
                }

                if (dataSource != null) {
                    // Reconstrói a PipelineConfiguration completa
                    PipelineConfiguration fullConfig = new PipelineConfiguration(
                            dataSource,
                            sConfig.getDestinationType(),
                            sConfig.getDestinationEndpoint(),
                            sConfig.getMetricConfigs(),
                            this.globalLogViewer,
                            sConfig.getAlertRules(),
                            sConfig.getInsightRules(),
                            sConfig.getTelegramToken(),
                            sConfig.getTelegramChatId(),
                            sConfig.getMqttBrokerUrl()
                    );
                    // Lança a pipeline
                    pipelineManager.launchPipeline(fullConfig);
                }
            } catch (Exception e) {
                logger.error("Falha ao tentar reiniciar automaticamente uma pipeline salva.", e);
            }
            if (!configsToStart.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        configsToStart.size() + " pipeline(s) da sessão anterior foram iniciadas automaticamente.",
                        "Início Automático",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        }
    }

    /**
     * Gere o processo de fecho da aplicação, perguntando ao utilizador se deseja
     * salvar as pipelines ativas.
     */
    public void handleWindowExit() {
        if (!pipelineManager.getRunningTasks().isEmpty()) {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Existem pipelines em execução. Deseja salvá-las para que iniciem automaticamente da próxima vez?",
                    "Confirmar Saída e Salvar Sessão",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (choice == JOptionPane.YES_OPTION) {
                saveConfiguration();
                System.exit(0);
            } else if (choice == JOptionPane.NO_OPTION) {
                configManager.saveActivePipelines(new ArrayList<>());
                System.exit(0);
            }
            // Se for CANCEL_OPTION, não faz nada.
        } else {
            saveConfiguration();
            System.exit(0);
        }
    }

    /**
     * Configura o ícone da aplicação na bandeja do sistema (System Tray).
     */
    private void setupSystemTray() {
        if (!SystemTray.isSupported()) {
            logger.warn("System Tray não é suportado neste sistema.");
            return;
        }

        PopupMenu trayPopupMenu = new PopupMenu();

        MenuItem openItem = new MenuItem("Abrir GSmart");
        openItem.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                setVisible(true);
                setState(Frame.NORMAL);
            });
        });
        trayPopupMenu.add(openItem);

        MenuItem closeItem = new MenuItem("Sair");
        closeItem.addActionListener(e -> {
            // CORREÇÃO: Chama diretamente o metodo de fecho seguro
            handleWindowExit();
        });
        trayPopupMenu.add(closeItem);

        // O ideal é ter um ficheiro de imagem para o ícone.
        // Por agora, usaremos null para evitar erros.
        TrayIcon trayIcon = new TrayIcon(getIconImage(), "GSmart", trayPopupMenu);
        trayIcon.setImageAutoSize(true);

        try {
            SystemTray.getSystemTray().add(trayIcon);
            logger.info("Ícone do GSmart adicionado à bandeja do sistema.");
        } catch (AWTException e) {
            logger.error("Não foi possível adicionar o ícone à bandeja do sistema.", e);
        }
    }

    /**
     * Cria e retorna um DocumentListener que valida o conteúdo de um JTextField em tempo real.
     * Altera a cor de fundo do campo para indicar se o conteúdo é válido ou inválido.
     *
     * @param textField O campo de texto a ser validado.
     * @param validator Uma função (lambda) que recebe o texto e retorna true se for válido, false caso contrário.
     */
    public javax.swing.event.DocumentListener createRealtimeValidator(JTextField textField, java.util.function.Predicate<String> validator) {
        return new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { validate(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { validate(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { validate(); }

            private void validate() {
                String text = textField.getText().trim();
                // A validação só é acionada se o campo não estiver vazio
                if (!text.isEmpty() && !validator.test(text)) {
                    textField.setBackground(new Color(255, 220, 220)); // Vermelho claro para inválido
                } else {
                    textField.setBackground(UIManager.getColor("TextField.background")); // Cor padrão
                }
            }
        };
    }

    // --- GETTERS E SETTERS ---
    // Métodos para os controladores acederem e modificarem os componentes
    public PipelineManager getPipelineManager() { return pipelineManager; }
    public ConfigManager getConfigManager() { return configManager; }
    public LogViewerWindow getGlobalLogViewer() { return globalLogViewer; }
    public DashboardPanel getDashboardPanel() { return dashboardPanel; }
    public JComboBox<String> getSourceSelector() { return sourceSelector; }
    public JComboBox<String> getDestinationSelector() { return destinationSelector; }
    public JTextField getThingsboardUrlField() { return thingsboardUrlField; }
    public JTextField getDbUrlField() { return dbUrlField; }
    public JTextField getDbUserField() { return dbUserField; }
    public JPasswordField getDbPasswordField() { return dbPasswordField; }
    public JTextField getPbiUrlField() { return pbiUrlField; }
    public JTextField getFabricConnectionStringField() { return fabricConnectionStringField; }
    public JTextField getMqttBrokerUrlField() { return mqttBrokerUrlField; }
    public JTextField getTelegramTokenField() { return telegramTokenField; }
    public JTextField getTelegramChatIdField() { return telegramChatIdField; }
    public JButton getTbConnectButton() { return tbConnectButton; }
    public JButton getDbConnectButton() { return dbConnectButton; }
    public JButton getStartButton() { return startButton; }
    public JButton getStopAllButton() { return stopAllButton; }
    public JButton getMonitoringButton() { return monitoringButton; }
    public JButton getManageUsersButton() { return manageUsersButton; }
    public JLabel getTbStatusLabel() { return tbStatusLabel; }
    public JLabel getDbStatusLabel() { return dbStatusLabel; }
    public JComboBox<DeviceProfile> getDeviceProfileSelector() { return deviceProfileSelector; }
    public JComboBox<Device> getDeviceSelector() { return deviceSelector; }
    public JComboBox<String> getDbTableSelector() { return dbTableSelector; }
    public JPanel getSourceConfigCardPanel() { return sourceConfigCardPanel; }
    public JPanel getDestinationConfigCardPanel() { return destinationConfigCardPanel; }
    public JPanel getThingsboardConfigPanel() { return thingsboardConfigPanel; }
    public JPanel getDatabaseConfigPanel() { return databaseConfigPanel; }
    public JTable getMetricsTable() { return metricsTable; }
    public JTable getAlertRulesTable() { return alertRulesTable; }
    public JTable getInsightRulesTable() { return insightRulesTable; }
    public MetricTableModel getMetricTableModel() { return metricTableModel; }
    public AlertRuleTableModel getAlertRuleTableModel() { return alertRuleTableModel; }
    public InsightRuleTableModel getInsightRuleTableModel() { return insightRuleTableModel; }
    public JCheckBox getRunInBackgroundCheckBox() { return runInBackgroundCheckBox; }
    public OkHttpClient getSharedOkHttpClient() { return sharedOkHttpClient; }
    public JPanel getAlertRuleButtons() { return alertRuleButtons; }
    public void setAlertRuleButtons(JPanel alertRuleButtons) { this.alertRuleButtons = alertRuleButtons; }
    public JPanel getInsightRuleButtons() { return insightRuleButtons; }
    public void setInsightRuleButtons(JPanel insightRuleButtons) { this.insightRuleButtons = insightRuleButtons; }
}