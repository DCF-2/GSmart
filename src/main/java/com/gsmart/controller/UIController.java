package main.java.com.gsmart.controller;

import main.java.com.gsmart.GSmartGui;
import main.java.com.gsmart.Gui.AlertRuleTableModel;
import main.java.com.gsmart.Gui.InsightRuleTableModel;
import main.java.com.gsmart.Gui.MetricTableModel;
import main.java.com.gsmart.Gui.SystemMetricCellRenderer;
import main.java.com.gsmart.Gui.panels.DashboardPanel;
import javax.imageio.ImageIO;

import javax.swing.*;
import java.awt.*;

/**
 * Controlador responsável pela construção e montagem de todos os componentes
 * da interface gráfica (UI) da aplicação GSmart.
 */
public class UIController {

    private final GSmartGui view;

    public UIController(GSmartGui view) {
        this.view = view;
        initializeUI();
    }

    private void initializeUI() {
        // --- Configuração da Janela Principal ---
        view.setTitle("GSmart - Configurador de Pipeline e Alertas v6.0");
        view.setExtendedState(JFrame.MAXIMIZED_BOTH);
        view.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        view.setLocationRelativeTo(null);

        try {
            Image icon = ImageIO.read(view.getClass().getResource("/gsmart_icon.png"));
            view.setIconImage(icon);
        } catch (Exception e) {
            // Se não encontrar o ícone, a aplicação continua a funcionar, apenas regista o erro.
            System.err.println("Erro ao carregar o ícone da aplicação: " + e.getMessage());
        }

        // --- Montagem dos Painéis ---
        JPanel pipelineConfigPanel = createPipelineConfigPanel();
        JPanel alertRulesPanel = createAlertRulesPanel();
        JPanel insightRulesPanel = createInsightRulesPanel();

        // --- Montagem Final com Separadores ---
        JTabbedPane mainTabbedPane = new JTabbedPane();
        mainTabbedPane.addTab("Dashboard", view.getDashboardPanel());
        mainTabbedPane.addTab("Configuração da Pipeline", pipelineConfigPanel);
        mainTabbedPane.addTab("Regras de Alerta", alertRulesPanel);
        mainTabbedPane.addTab("Regras de Alarmes", insightRulesPanel);

        view.setContentPane(mainTabbedPane);
    }

    private JPanel createPipelineConfigPanel() {
        // --- Painel Superior (Fonte de Dados e Destino) ---
        JPanel topConfigurationPanel = createTopConfigurationPanel();

        // --- Tabela de Métricas ---
        JScrollPane keysScrollPane = createMetricsTablePanel();

        // --- Painel de Ações ---
        JPanel bottomPanel = createBottomActionPanel();

        // --- Montagem Final ---
        JPanel pipelineConfigPanel = new JPanel(new BorderLayout(5, 5));
        pipelineConfigPanel.add(topConfigurationPanel, BorderLayout.NORTH);
        pipelineConfigPanel.add(keysScrollPane, BorderLayout.CENTER);
        pipelineConfigPanel.add(bottomPanel, BorderLayout.SOUTH);
        return pipelineConfigPanel;
    }

    private JPanel createTopConfigurationPanel() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        // Fonte de Dados
        JPanel sourceSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sourceSelectionPanel.setBorder(BorderFactory.createTitledBorder("Selecione a Fonte de Dados"));
        sourceSelectionPanel.add(new JLabel("Tipo de Fonte:"));
        sourceSelectionPanel.add(view.getSourceSelector());
        topPanel.add(sourceSelectionPanel);

        // Painel CardLayout para Configurações da Fonte
        createDataSourceConfigPanels();
        topPanel.add(view.getSourceConfigCardPanel());

        // Destino
        JPanel destinationPanel = createDestinationPanel();
        topPanel.add(destinationPanel);

        // Serviços Auxiliares
        JPanel auxiliaryServicesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        auxiliaryServicesPanel.setBorder(BorderFactory.createTitledBorder("Configurar Serviços Auxiliares"));
        auxiliaryServicesPanel.add(new JLabel("URL do Broker MQTT:"));
        auxiliaryServicesPanel.add(view.getMqttBrokerUrlField());
        topPanel.add(auxiliaryServicesPanel);

        // Telegram
        JPanel telegramPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        telegramPanel.setBorder(BorderFactory.createTitledBorder("Configurar Notificações do Telegram"));
        telegramPanel.add(new JLabel("Token do Bot:"));
        telegramPanel.add(view.getTelegramTokenField());
        telegramPanel.add(new JLabel("Chat ID:"));
        telegramPanel.add(view.getTelegramChatIdField());
        topPanel.add(telegramPanel);

        return topPanel;
    }

    private void createDataSourceConfigPanels() {
        // Painel ThingsBoard
        JPanel tbPanel = view.getThingsboardConfigPanel();
        tbPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbcTb = new GridBagConstraints();
        gbcTb.insets = new Insets(4, 5, 4, 5);
        gbcTb.anchor = GridBagConstraints.WEST;
        gbcTb.gridx = 0; gbcTb.gridy = 0; tbPanel.add(new JLabel("URL do Servidor:"), gbcTb);
        gbcTb.gridx = 1; gbcTb.weightx = 1.0; gbcTb.fill = GridBagConstraints.HORIZONTAL; tbPanel.add(view.getThingsboardUrlField(), gbcTb);
        gbcTb.gridx = 2; gbcTb.weightx = 0; gbcTb.fill = GridBagConstraints.NONE; tbPanel.add(view.getTbConnectButton(), gbcTb);
        gbcTb.gridx = 3; tbPanel.add(view.getTbStatusLabel(), gbcTb);
        gbcTb.gridx = 0; gbcTb.gridy = 1; tbPanel.add(new JLabel("Perfil de Dispositivo (Tipo):"), gbcTb);
        gbcTb.gridx = 1; gbcTb.gridwidth = 3; gbcTb.fill = GridBagConstraints.HORIZONTAL; tbPanel.add(view.getDeviceProfileSelector(), gbcTb);
        gbcTb.gridx = 0; gbcTb.gridy = 2; gbcTb.gridwidth = 1; tbPanel.add(new JLabel("Dispositivo:"), gbcTb);
        gbcTb.gridx = 1; gbcTb.gridwidth = 3; tbPanel.add(view.getDeviceSelector(), gbcTb);

        // Painel Banco de Dados
        JPanel dbPanel = view.getDatabaseConfigPanel();
        dbPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbcDb = new GridBagConstraints();
        gbcDb.insets = new Insets(2, 5, 2, 5); gbcDb.anchor = GridBagConstraints.WEST;
        gbcDb.gridx = 0; gbcDb.gridy = 0; gbcDb.gridwidth = 1; dbPanel.add(new JLabel("URL do Banco (JDBC):"), gbcDb);
        gbcDb.gridx = 1; gbcDb.gridwidth = 3; gbcDb.weightx = 1.0; gbcDb.fill = GridBagConstraints.HORIZONTAL; dbPanel.add(view.getDbUrlField(), gbcDb);
        gbcDb.gridx = 4; gbcDb.gridwidth = 1; gbcDb.fill = GridBagConstraints.NONE; dbPanel.add(view.getDbConnectButton(), gbcDb);
        gbcDb.gridx = 5; dbPanel.add(view.getDbStatusLabel(), gbcDb);
        gbcDb.gridx = 0; gbcDb.gridy = 1; gbcDb.gridwidth = 1; dbPanel.add(new JLabel("Usuário:"), gbcDb);
        gbcDb.gridx = 1; gbcDb.gridwidth = 3; gbcDb.weightx = 1.0; gbcDb.fill = GridBagConstraints.HORIZONTAL; dbPanel.add(view.getDbUserField(), gbcDb);
        gbcDb.gridx = 0; gbcDb.gridy = 2; gbcDb.gridwidth = 1; dbPanel.add(new JLabel("Senha:"), gbcDb);
        gbcDb.gridx = 1; gbcDb.gridwidth = 3; gbcDb.weightx = 1.0; gbcDb.fill = GridBagConstraints.HORIZONTAL; dbPanel.add(view.getDbPasswordField(), gbcDb);
        gbcDb.gridx = 0; gbcDb.gridy = 3; gbcDb.gridwidth = 1; dbPanel.add(new JLabel("Tabela:"), gbcDb);
        gbcDb.gridx = 1; gbcDb.gridwidth = 3; gbcDb.weightx = 1.0; gbcDb.fill = GridBagConstraints.HORIZONTAL; dbPanel.add(view.getDbTableSelector(), gbcDb);

        view.getSourceConfigCardPanel().add(tbPanel, "Thingsboard API");
        view.getSourceConfigCardPanel().add(dbPanel, "Banco de Dados Espelho");
    }

    private JPanel createDestinationPanel() {
        JPanel destinationPanel = new JPanel(new BorderLayout(5, 5));
        destinationPanel.setBorder(BorderFactory.createTitledBorder("Configurar Destino dos Dados"));
        JPanel destinationSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        destinationSelectionPanel.add(new JLabel("Tipo de Destino:"));
        destinationSelectionPanel.add(view.getDestinationSelector());
        destinationPanel.add(destinationSelectionPanel, BorderLayout.NORTH);

        JPanel pbiPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pbiPanel.add(new JLabel("URL de Push:"));
        pbiPanel.add(view.getPbiUrlField());
        JPanel fabricPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fabricPanel.add(new JLabel("Connection String:"));
        fabricPanel.add(view.getFabricConnectionStringField());

        view.getDestinationConfigCardPanel().add(pbiPanel, "Power BI Push URL");
        view.getDestinationConfigCardPanel().add(fabricPanel, "Fabric Eventstream");
        destinationPanel.add(view.getDestinationConfigCardPanel(), BorderLayout.CENTER);

        return destinationPanel;
    }

    private JScrollPane createMetricsTablePanel() {
        JTable metricsTable = view.getMetricsTable();
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
        return keysScrollPane;
    }

    private JPanel createBottomActionPanel() {
        // Painel de Ações da Pipeline (Esquerda)
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15));
        actionPanel.add(view.getStartButton());

        // Painel de Administração (Direita)
        JPanel adminPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        adminPanel.add(view.getRunInBackgroundCheckBox());
        adminPanel.add(new JSeparator(SwingConstants.VERTICAL));
        adminPanel.add(view.getManageUsersButton());

        JButton autoStartButton = new JButton("Gerir Início Automático");
        autoStartButton.addActionListener(e -> view.showAutoStartManager());
        adminPanel.add(autoStartButton);

        adminPanel.add(view.getMonitoringButton());
        adminPanel.add(view.getStopAllButton());

        JPopupMenu logsPopupMenu = new JPopupMenu();
        JMenuItem generalLogItem = new JMenuItem("Log Geral");
        generalLogItem.addActionListener(e -> view.getGlobalLogViewer().setVisible(true));
        JMenuItem reconexLogItem = new JMenuItem("Log de Reconexão");
        reconexLogItem.addActionListener(e -> view.showReconnectionLog());
        logsPopupMenu.add(generalLogItem);
        logsPopupMenu.add(reconexLogItem);
        JButton logsButton = new JButton("Ver Logs");
        logsButton.addActionListener(e -> logsPopupMenu.show(logsButton, 0, logsButton.getHeight()));
        adminPanel.add(logsButton);

        adminPanel.add(new JSeparator(SwingConstants.VERTICAL));
        JButton helpButton = new JButton("Ajuda");
        helpButton.addActionListener(e -> view.showHelpWindow());
        adminPanel.add(helpButton);

        // Painel Inferior Completo
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 0));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        bottomPanel.add(actionPanel, BorderLayout.WEST);
        bottomPanel.add(adminPanel, BorderLayout.CENTER);
        return bottomPanel;
    }

    private JPanel createAlertRulesPanel() {
        JPanel alertRulesPanel = new JPanel(new BorderLayout(5, 5));
        alertRulesPanel.setBorder(BorderFactory.createTitledBorder("Configurador de Alertas Customizados"));

        JTable alertRulesTable = view.getAlertRulesTable();
        alertRulesTable.setFillsViewportHeight(true);
        alertRulesTable.getColumnModel().getColumn(0).setMaxWidth(50);
        alertRulesPanel.add(new JScrollPane(alertRulesTable), BorderLayout.CENTER);

        JPanel ruleButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        ruleButtonsPanel.add(new JButton("Adicionar Regra"));
        ruleButtonsPanel.add(new JButton("Editar Regra"));
        ruleButtonsPanel.add(new JButton("Remover Regra"));
        view.setAlertRuleButtons(ruleButtonsPanel); // Passa o painel para a view
        alertRulesPanel.add(ruleButtonsPanel, BorderLayout.SOUTH);

        return alertRulesPanel;
    }

    private JPanel createInsightRulesPanel() {
        JPanel insightRulesPanel = new JPanel(new BorderLayout(5, 5));
        insightRulesPanel.setBorder(BorderFactory.createTitledBorder("Configurador de Alarmes Inteligentes"));

        JTable insightRulesTable = view.getInsightRulesTable();
        insightRulesTable.setFillsViewportHeight(true);
        insightRulesTable.getColumnModel().getColumn(0).setMaxWidth(50);
        insightRulesPanel.add(new JScrollPane(insightRulesTable), BorderLayout.CENTER);

        JPanel insightButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        insightButtonsPanel.add(new JButton("Adicionar Regra de Alarme"));
        insightButtonsPanel.add(new JButton("Editar Regra de Alarme"));
        insightButtonsPanel.add(new JButton("Remover Regra de Alarme"));
        view.setInsightRuleButtons(insightButtonsPanel); // Passa o painel para a view
        insightRulesPanel.add(insightButtonsPanel, BorderLayout.SOUTH);

        return insightRulesPanel;
    }
}