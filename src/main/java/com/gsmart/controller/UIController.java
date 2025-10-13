package main.java.com.gsmart.controller;

import main.java.com.gsmart.GSmartGui;
import main.java.com.gsmart.Gui.SystemMetricCellRenderer;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;

/**
 * Controlador responsável pela construção e montagem de todos os componentes
 * da interface gráfica (UI) da aplicação GSmart.
 * <p>
 * A sua principal função é inicializar a janela principal e organizar os vários
 * painéis (Dashboard, Configuração de Pipeline, Regras, etc.) dentro de um
 * {@link java.awt.CardLayout}, que permite a navegação entre as diferentes
 * secções da aplicação.
 */
public class UIController {

    private final GSmartGui view;

    public UIController(GSmartGui view) {
        this.view = view;
        initializeUI();
    }

    /**
     * Configura a janela principal e inicializa todos os painéis da UI.
     * <p>
     * Este método define as propriedades da janela principal (título, tamanho, ícone)
     * e monta os painéis de conteúdo, como o Dashboard e os ecrãs de configuração,
     * dentro de um painel com CardLayout para permitir a navegação.
     */
    private void initializeUI() {
        // --- Configuração da Janela Principal ---
        view.setTitle("GSmart - Configurador de Pipeline e Alertas v2.3.5");
        view.setExtendedState(JFrame.MAXIMIZED_BOTH);
        view.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        view.setLocationRelativeTo(null);
        view.setLayout(new BorderLayout());

        try {
            Image icon = ImageIO.read(view.getClass().getResource("/gsmart_icon.png"));
            view.setIconImage(icon);
        } catch (Exception e) {
            System.err.println("Erro ao carregar o ícone da aplicação: " + e.getMessage());
        }

        JPanel contentPanel = new JPanel(new CardLayout());
        JPanel pipelineConfigPanel = createPipelineConfigPanel();
        JPanel alertRulesPanel = createAlertRulesPanel();
        JPanel insightRulesPanel = createInsightRulesPanel();

        contentPanel.add(view.getDashboardPanel(), "Dashboard");
        contentPanel.add(pipelineConfigPanel, "Configurar Pipeline");
        contentPanel.add(alertRulesPanel, "Regras de Alerta");
        contentPanel.add(insightRulesPanel, "Regras de Alarme");

        view.add(view.getSideMenuPanel(), BorderLayout.WEST);
        view.add(contentPanel, BorderLayout.CENTER);
        view.setContentPanel(contentPanel);
    }

    /**
     * Cria e monta o painel de configuração de pipelines.
     * <p>
     * Este painel agrega todos os componentes necessários para configurar uma nova
     * pipeline, incluindo a seleção da fonte de dados, o destino, as configurações
     * de serviços auxiliares (MQTT, Telegram) e a tabela de métricas.
     *
     * @return O {@link JPanel} completo para a configuração de pipelines.
     */
    private JPanel createPipelineConfigPanel() {
        // --- Painel Superior (Fonte de Dados e Destino) ---
        JPanel topConfigurationPanel = createTopConfigurationPanel();

        // --- Tabela de Métricas ---
        JScrollPane keysScrollPane = createMetricsTablePanel();

        // --- ALTERAÇÃO: Adiciona o botão de ajuda abaixo da tabela ---
        JPanel helpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        helpPanel.add(view.getExpressionHelpButton());

        // Cria um painel central para agrupar a tabela e o painel de ajuda
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(keysScrollPane, BorderLayout.CENTER);
        centerPanel.add(helpPanel, BorderLayout.SOUTH);

        // --- Montagem Final ---
        JPanel pipelineConfigPanel = new JPanel(new BorderLayout(5, 5));
        pipelineConfigPanel.add(topConfigurationPanel, BorderLayout.NORTH);
        pipelineConfigPanel.add(centerPanel, BorderLayout.CENTER); // Adiciona o novo painel central

        // Adiciona APENAS o botão de iniciar num novo painel inferior simples
        JPanel startPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15));
        startPanel.add(view.getStartButton());
        pipelineConfigPanel.add(startPanel, BorderLayout.SOUTH);

        return pipelineConfigPanel;
    }

    /**
     * Cria e agrega o painel superior da secção de configuração de pipelines.
     * <p>
     * Este painel contém todos os sub-painéis relacionados com a configuração da
     * origem dos dados (ThingsBoard/Base de Dados), do destino (Power BI/Fabric) e dos
     * serviços auxiliares (MQTT/Telegram).
     *
     * @return Um {@link JPanel} que contém todos os painéis de configuração superior.
     */
    private JPanel createTopConfigurationPanel() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        JPanel sourceSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sourceSelectionPanel.setBorder(BorderFactory.createTitledBorder("Selecione a Fonte de Dados"));
        sourceSelectionPanel.add(new JLabel("Tipo de Fonte:"));
        sourceSelectionPanel.add(view.getSourceSelector());
        topPanel.add(sourceSelectionPanel);

        createDataSourceConfigPanels();
        topPanel.add(view.getSourceConfigCardPanel());

        JPanel destinationPanel = createDestinationPanel();
        topPanel.add(destinationPanel);

        JPanel auxiliaryServicesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        auxiliaryServicesPanel.setBorder(BorderFactory.createTitledBorder("Configurar Serviços Auxiliares"));
        auxiliaryServicesPanel.add(new JLabel("URL do Broker MQTT:"));
        auxiliaryServicesPanel.add(view.getMqttBrokerUrlField());
        topPanel.add(auxiliaryServicesPanel);

        JPanel telegramPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        telegramPanel.setBorder(BorderFactory.createTitledBorder("Configurar Notificações do Telegram"));
        telegramPanel.add(new JLabel("Token do Bot:"));
        telegramPanel.add(view.getTelegramTokenField());
        telegramPanel.add(view.getTelegramHelpButton());
        telegramPanel.add(new JLabel("Chat ID:"));
        telegramPanel.add(view.getTelegramChatIdField());
        topPanel.add(telegramPanel);

        return topPanel;
    }

    /**
     * Constrói os painéis de configuração específicos para cada fonte de dados.
     * <p>
     * Este método cria os painéis para o ThingsBoard e para a Base de Dados,
     * organizando os seus respetivos campos de texto, seletores e botões de conexão.
     * Os painéis são depois adicionados a um {@link java.awt.CardLayout} para
     * permitir a alternância entre eles.
     */
    private void createDataSourceConfigPanels() {
        // --- PAINEL DO THINGSBOARD ---
        JPanel tbPanel = view.getThingsboardConfigPanel();
        tbPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbcTb = new GridBagConstraints();
        gbcTb.insets = new Insets(4, 5, 4, 5);
        gbcTb.anchor = GridBagConstraints.WEST;

        // Linha 0: URL
        gbcTb.gridx = 0; gbcTb.gridy = 0; tbPanel.add(new JLabel("URL do Servidor:"), gbcTb);
        gbcTb.gridx = 1; gbcTb.gridwidth = 3; gbcTb.fill = GridBagConstraints.HORIZONTAL; tbPanel.add(view.getThingsboardUrlField(), gbcTb);

        // Linha 1: Utilizador
        gbcTb.gridx = 0; gbcTb.gridy = 1; gbcTb.gridwidth = 1; gbcTb.fill = GridBagConstraints.NONE; tbPanel.add(new JLabel("Utilizador:"), gbcTb);
        gbcTb.gridx = 1; gbcTb.gridwidth = 3; gbcTb.fill = GridBagConstraints.HORIZONTAL; tbPanel.add(view.getTbUserField(), gbcTb);

        // Linha 2: Senha e Botão de Conectar
        gbcTb.gridx = 0; gbcTb.gridy = 2; gbcTb.gridwidth = 1; tbPanel.add(new JLabel("Senha:"), gbcTb);
        gbcTb.gridx = 1; gbcTb.gridwidth = 1; gbcTb.fill = GridBagConstraints.HORIZONTAL; tbPanel.add(view.getTbPassField(), gbcTb);
        gbcTb.gridx = 2; gbcTb.gridwidth = 1; gbcTb.fill = GridBagConstraints.NONE; tbPanel.add(view.getTbConnectButton(), gbcTb);
        gbcTb.gridx = 3; gbcTb.gridwidth = 1; tbPanel.add(view.getTbStatusLabel(), gbcTb);

        // Linha 3: Perfil de Dispositivo
        gbcTb.gridx = 0; gbcTb.gridy = 3; tbPanel.add(new JLabel("Perfil de Dispositivo:"), gbcTb);
        gbcTb.gridx = 1; gbcTb.gridwidth = 3; gbcTb.fill = GridBagConstraints.HORIZONTAL; tbPanel.add(view.getDeviceProfileSelector(), gbcTb);

        // Linha 4: Dispositivo
        gbcTb.gridx = 0; gbcTb.gridy = 4; gbcTb.gridwidth = 1; tbPanel.add(new JLabel("Dispositivo:"), gbcTb);
        gbcTb.gridx = 1; gbcTb.gridwidth = 3; gbcTb.fill = GridBagConstraints.HORIZONTAL; tbPanel.add(view.getDeviceSelector(), gbcTb);

        // Filler: Componente invisível que ocupa todo o espaço extra à direita
        gbcTb.gridx = 4; gbcTb.gridy = 0; gbcTb.weightx = 1.0; gbcTb.fill = GridBagConstraints.HORIZONTAL;
        tbPanel.add(new JPanel(), gbcTb);


        // --- PAINEL DO BANCO DE DADOS ---
        JPanel dbPanel = view.getDatabaseConfigPanel();
        dbPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbcDb = new GridBagConstraints();
        gbcDb.insets = new Insets(2, 5, 2, 5); gbcDb.anchor = GridBagConstraints.WEST;

        // Linha 0: URL
        gbcDb.gridx = 0; gbcDb.gridy = 0; gbcDb.gridwidth = 1; dbPanel.add(new JLabel("URL do Banco (JDBC):"), gbcDb);
        gbcDb.gridx = 1; gbcDb.gridwidth = 3; gbcDb.fill = GridBagConstraints.HORIZONTAL; dbPanel.add(view.getDbUrlField(), gbcDb);

        // Linha 1: Utilizador
        gbcDb.gridx = 0; gbcDb.gridy = 1; gbcDb.gridwidth = 1; gbcDb.fill = GridBagConstraints.NONE; dbPanel.add(new JLabel("Usuário:"), gbcDb);
        gbcDb.gridx = 1; gbcDb.gridwidth = 3; gbcDb.fill = GridBagConstraints.HORIZONTAL; dbPanel.add(view.getDbUserField(), gbcDb);

        // Linha 2: Senha e Botão
        gbcDb.gridx = 0; gbcDb.gridy = 2; gbcDb.gridwidth = 1; dbPanel.add(new JLabel("Senha:"), gbcDb);
        gbcDb.gridx = 1; gbcDb.gridwidth = 1; gbcDb.fill = GridBagConstraints.HORIZONTAL; dbPanel.add(view.getDbPasswordField(), gbcDb);
        gbcDb.gridx = 2; gbcDb.gridwidth = 1; gbcDb.fill = GridBagConstraints.NONE; dbPanel.add(view.getDbConnectButton(), gbcDb);
        gbcDb.gridx = 3; gbcDb.gridwidth = 1; dbPanel.add(view.getDbStatusLabel(), gbcDb);

        // Linha 3: Tabela
        gbcDb.gridx = 0; gbcDb.gridy = 3; gbcDb.gridwidth = 1; dbPanel.add(new JLabel("Tabela:"), gbcDb);
        gbcDb.gridx = 1; gbcDb.gridwidth = 3; gbcDb.fill = GridBagConstraints.HORIZONTAL; dbPanel.add(view.getDbTableSelector(), gbcDb);

        // Filler: Componente invisível para empurrar tudo para a esquerda
        gbcDb.gridx = 4; gbcDb.gridy = 0; gbcDb.weightx = 1.0; gbcDb.fill = GridBagConstraints.HORIZONTAL;
        dbPanel.add(new JPanel(), gbcDb);


        view.getSourceConfigCardPanel().add(tbPanel, "Thingsboard API");
        view.getSourceConfigCardPanel().add(dbPanel, "Banco de Dados Espelho");
    }

    /**
     * Cria e monta o painel para a configuração do destino dos dados.
     * <p>
     * Contém um JComboBox para selecionar o tipo de destino (Power BI ou Fabric) e um
     * painel com {@link java.awt.CardLayout} que exibe os campos de configuração
     * apropriados (URL de Push ou Connection String) com base na seleção.
     *
     * @return O {@link JPanel} completo para a configuração do destino.
     */
    private JPanel createDestinationPanel() {
        JPanel destinationPanel = new JPanel(new BorderLayout(5, 5));
        destinationPanel.setBorder(BorderFactory.createTitledBorder("Configurar Destino dos Dados"));
        JPanel destinationSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        destinationSelectionPanel.add(new JLabel("Tipo de Destino:"));
        destinationSelectionPanel.add(view.getDestinationSelector());

        // O painel de seleção é adicionado ao painel principal (destinationPanel).
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

    /**
     * Cria e configura o painel que contém a tabela de métricas.
     * <p>
     * Este método configura a JTable para exibir as métricas, ajustando a largura das
     * colunas e aplicando um renderizador customizado ({@link main.java.com.gsmart.Gui.SystemMetricCellRenderer})
     * para diferenciar visualmente as métricas de sistema.
     *
     * @return Um {@link JScrollPane} contendo a tabela de métricas configurada.
     */
    private JScrollPane createMetricsTablePanel() {
        JTable metricsTable = view.getMetricsTable();
        metricsTable.setShowGrid(true);
        metricsTable.setGridColor(Color.LIGHT_GRAY);
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

    /**
     * Cria e monta o painel completo para a gestão de Regras de Alerta.
     * <p>
     * O painel inclui um filtro por categoria, a tabela principal que exibe as regras
     * e um painel inferior com os botões de ação (adicionar, editar, remover,
     * importar/exportar).
     *
     * @return O {@link JPanel} completo para a gestão de regras de alerta.
     */
    private JPanel createAlertRulesPanel() {
        JPanel alertRulesPanel = new JPanel(new BorderLayout(5, 5));
        alertRulesPanel.setBorder(BorderFactory.createTitledBorder("Configurador de Alertas Customizados"));

        // --- PAINEL DE FILTRO ---
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filtrar por Categoria:"));
        filterPanel.add(view.getAlertCategoryFilter());
        alertRulesPanel.add(filterPanel, BorderLayout.NORTH);

        JTable alertRulesTable = view.getAlertRulesTable();
        alertRulesTable.setShowGrid(true);
        alertRulesTable.setGridColor(Color.LIGHT_GRAY);
        alertRulesTable.setFillsViewportHeight(true);
        alertRulesTable.getColumnModel().getColumn(0).setMaxWidth(50);
        alertRulesPanel.add(new JScrollPane(alertRulesTable), BorderLayout.CENTER);

        JPanel ruleButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        ruleButtonsPanel.add(view.getAddAlertRuleButton());
        ruleButtonsPanel.add(view.getEditAlertRuleButton());
        ruleButtonsPanel.add(view.getRemoveAlertRuleButton());
        ruleButtonsPanel.add(view.getDuplicateAlertRuleButton());
        ruleButtonsPanel.add(Box.createHorizontalStrut(20));

        // Adiciona ToolTips aqui
        view.getImportAlertRulesButton().setToolTipText("Importa TODAS as regras (Alertas e Alarmes) de um ficheiro, substituindo as atuais.");
        view.getExportAlertRulesButton().setToolTipText("Exporta TODAS as regras (Alertas e Alarmes) para um único ficheiro de backup.");

        ruleButtonsPanel.add(view.getImportAlertRulesButton());
        ruleButtonsPanel.add(view.getExportAlertRulesButton());

        view.setAlertRuleButtons(ruleButtonsPanel);
        alertRulesPanel.add(ruleButtonsPanel, BorderLayout.SOUTH);

        return alertRulesPanel;
    }

    /**
     * Cria e monta o painel completo para a gestão de Regras de Alarme (Insights).
     * <p>
     * De forma semelhante ao painel de alertas, este painel inclui um filtro, a tabela
     * de regras de alarme e os botões de ação correspondentes.
     *
     * @return O {@link JPanel} completo para a gestão de regras de alarme.
     */
    private JPanel createInsightRulesPanel() {
        JPanel insightRulesPanel = new JPanel(new BorderLayout(5, 5));
        insightRulesPanel.setBorder(BorderFactory.createTitledBorder("Configurador de Alarmes Inteligentes"));

        // --- PAINEL DE FILTRO ---
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filtrar por Categoria:"));
        filterPanel.add(view.getInsightCategoryFilter());
        insightRulesPanel.add(filterPanel, BorderLayout.NORTH);

        JTable insightRulesTable = view.getInsightRulesTable();
        insightRulesTable.setShowGrid(true);
        insightRulesTable.setGridColor(Color.LIGHT_GRAY);
        insightRulesTable.setFillsViewportHeight(true);
        insightRulesTable.getColumnModel().getColumn(0).setMaxWidth(50);
        insightRulesPanel.add(new JScrollPane(insightRulesTable), BorderLayout.CENTER);

        JPanel insightButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        insightButtonsPanel.add(view.getAddInsightRuleButton());
        insightButtonsPanel.add(view.getEditInsightRuleButton());
        insightButtonsPanel.add(view.getRemoveInsightRuleButton());
        insightButtonsPanel.add(view.getDuplicateInsightRuleButton());
        insightButtonsPanel.add(Box.createHorizontalStrut(20));

        // Adiciona ToolTips aqui
        view.getImportInsightRulesButton().setToolTipText("Importa TODAS as regras (Alertas e Alarmes) de um ficheiro, substituindo as atuais.");
        view.getExportInsightRulesButton().setToolTipText("Exporta TODAS as regras (Alertas e Alarmes) para um único ficheiro de backup.");

        insightButtonsPanel.add(view.getImportInsightRulesButton());
        insightButtonsPanel.add(view.getExportInsightRulesButton());

        view.setInsightRuleButtons(insightButtonsPanel);
        insightRulesPanel.add(insightButtonsPanel, BorderLayout.SOUTH);

        return insightRulesPanel;
    }
}