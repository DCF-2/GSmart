// Localização: src/main/java/com/gsmart/controller/ActionController.java
package main.java.com.gsmart.controller;

import main.java.com.gsmart.GSmartGui;
import main.java.com.gsmart.Gui.AlertRuleTableModel;
import main.java.com.gsmart.Gui.InsightRuleTableModel;
import main.java.com.gsmart.Gui.MetricTableModel;
import main.java.com.gsmart.Gui.panels.SideMenuPanel;
import main.java.com.gsmart.Gui.windows.AlertRuleDialog;
import main.java.com.gsmart.Gui.windows.InsightRuleDialog;
import main.java.com.gsmart.config.AlertRule;
import main.java.com.gsmart.config.InsightRule;
import main.java.com.gsmart.config.MetricConfig;
import main.java.com.gsmart.config.PipelineConfiguration;
import main.java.com.gsmart.pipeline.PipelineTask;
import main.java.com.gsmart.resources.DestinationType;
import main.java.com.gsmart.resources.IDataSource;
import javax.swing.table.TableRowSorter;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import main.java.com.gsmart.ui.ThemeManager;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.swing.filechooser.FileNameExtensionFilter;
import main.java.com.gsmart.config.RulesContainer;
import java.io.*;

/**
 * Controlador responsável por configurar todos os "ouvintes" (listeners) e gerir as
 * ações do utilizador na interface gráfica do GSmart.
 * <p>
 * Esta classe centraliza a lógica de eventos, ligando os componentes da UI
 * (botões, menus, tabelas) às suas respetivas funcionalidades na lógica de negócio,
 * como lançar pipelines, abrir diálogos, importar/exportar regras e filtrar dados.
 * <p>
 * Ela colabora estreitamente com o {@link DataSourceController} para ações
 * relacionadas com a fonte de dados e com o {@link main.java.com.gsmart.pipeline.PipelineManager}
 * para a gestão do ciclo de vida das pipelines.
 */
public class ActionController {

    private final GSmartGui view;
    private final DataSourceController dataSourceController;
    private final boolean isAdmin;

    public ActionController(GSmartGui view, DataSourceController dataSourceController, String currentUserRole) {
        this.view = view;
        this.dataSourceController = dataSourceController;
        this.isAdmin = "ADMINISTRATOR".equals(currentUserRole);
        registerListeners();
    }

    /**
     * Regista todos os listeners de eventos para os componentes da interface gráfica.
     * <p>
     * Este método é o ponto central de configuração para todas as interações do utilizador,
     * desde a navegação no menu lateral até aos cliques nos botões de gestão de regras
     * e configuração de pipelines.
     */
    private void registerListeners() {
        // --- Listeners do Menu Lateral ---
        SideMenuPanel sideMenu = view.getSideMenuPanel();
        CardLayout cardLayout = (CardLayout) view.getContentPanel().getLayout();

        // Navegação
        sideMenu.getDashboardButton().addActionListener(e -> cardLayout.show(view.getContentPanel(), "Dashboard"));
        sideMenu.getPipelineConfigButton().addActionListener(e -> cardLayout.show(view.getContentPanel(), "Configurar Pipeline"));
        sideMenu.getAlertRulesButton().addActionListener(e -> cardLayout.show(view.getContentPanel(), "Regras de Alerta"));
        sideMenu.getInsightRulesButton().addActionListener(e -> cardLayout.show(view.getContentPanel(), "Regras de Alarme"));

        //Ações Rápidas ---
        sideMenu.getQuickStartPipelineButton().addActionListener(e -> cardLayout.show(view.getContentPanel(), "Configurar Pipeline"));
        sideMenu.getQuickOpenTaskManagerButton().addActionListener(e -> view.showTaskManager());

        // Gestão e Sistema
        sideMenu.getUserManagementButton().addActionListener(e -> view.showUserManagementWindow());
        sideMenu.getSettingsButton().addActionListener(e -> view.showSettingsDialog());
        sideMenu.getTaskManagerButton().addActionListener(e -> view.showTaskManager());
        sideMenu.getStopAllButton().addActionListener(e -> view.getPipelineManager().stopAllPipelines());
        sideMenu.getHelpButton().addActionListener(e -> view.showHelpWindow());
        view.getTelegramHelpButton().addActionListener(e -> showTelegramHelp());
        sideMenu.getLogoffButton().addActionListener(e -> view.handleLogoff());
        sideMenu.getToggleThemeButton().addActionListener(e -> {
            JPopupMenu themeMenu = new JPopupMenu();

            JMenuItem systemThemeItem = new JMenuItem("Padrão do Sistema");
            systemThemeItem.addActionListener(event -> ThemeManager.changeAndSaveTheme(ThemeManager.THEME_SYSTEM));

            JMenuItem lightThemeItem = new JMenuItem("Tema Claro");
            lightThemeItem.addActionListener(event -> ThemeManager.changeAndSaveTheme(ThemeManager.THEME_LIGHT));

            JMenuItem darkThemeItem = new JMenuItem("Tema Escuro");
            darkThemeItem.addActionListener(event -> ThemeManager.changeAndSaveTheme(ThemeManager.THEME_DARK));

            themeMenu.add(systemThemeItem);
            themeMenu.add(lightThemeItem);
            themeMenu.add(darkThemeItem);

            // Mostra o menu abaixo do botão que foi clicado
            JButton button = (JButton) e.getSource();
            themeMenu.show(button, 0, button.getHeight());
        });

        // Lógica para o menu de Logs
        JPopupMenu logsPopupMenu = new JPopupMenu();
        JMenuItem generalLogItem = new JMenuItem("Log Geral");
        generalLogItem.addActionListener(e -> view.getGlobalLogViewer().setVisible(true));
        JMenuItem reconexLogItem = new JMenuItem("Log de Reconexão");
        reconexLogItem.addActionListener(e -> view.showReconnectionLog());
        logsPopupMenu.add(generalLogItem);
        logsPopupMenu.add(reconexLogItem);
        sideMenu.getLogsButton().addActionListener(e -> logsPopupMenu.show(sideMenu.getLogsButton(), 0, sideMenu.getLogsButton().getHeight()));

        // Listeners dos Componentes de Configuração
        view.getSourceSelector().addItemListener(e -> view.toggleSourceFields());
        view.getDestinationSelector().addItemListener(e -> view.toggleDestinationFields());
        view.getTbConnectButton().addActionListener(e -> dataSourceController.connectToThingsboard());
        view.getDbConnectButton().addActionListener(e -> dataSourceController.connectToDatabase());
        view.getDeviceProfileSelector().addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) dataSourceController.loadDevicesByProfile();
        });
        view.getDeviceSelector().addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) dataSourceController.loadAvailableKeys();
        });
        view.getDbTableSelector().addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) dataSourceController.loadAvailableKeys();
        });
        view.getStartButton().addActionListener(e -> launchPipeline());

        // Listener para o botão de ajuda de expressões ---
        view.getExpressionHelpButton().addActionListener(e -> showExpressionHelp());

        //Listeners e inicialização dos filtros de categoria ---
        view.getAlertCategoryFilter().addActionListener(e -> applyTableFilter());
        view.getInsightCategoryFilter().addActionListener(e -> applyTableFilter());
        updateCategoryFilters(); // Popula os filtros na primeira vez

        // Listeners das Regras de Alerta
        registerAlertRuleListeners();

        // Listeners das Regras de Alarme
        registerInsightRuleListeners();

        // Listener para incrementar o contador de alarmes
        view.getPipelineManager().addAlarmListener(() -> view.incrementRecentAlarmsCount());

        // Temporizador para reiniciar o contador de alarmes a cada hora
        Timer alarmResetTimer = new Timer(3600 * 1000, e -> {
            view.resetRecentAlarmsCount();
        });
        alarmResetTimer.setRepeats(true);
        alarmResetTimer.start();

        // Listeners do Dashboard e da Janela
        view.getPipelineManager().addTaskListUpdatedListener(() -> {
            List<PipelineTask> runningTasks = view.getPipelineManager().getRunningTasks();

            int taskCount = runningTasks.size();
            view.getDashboardPanel().updateActivePipelinesCount(taskCount);

            long activeAlerts = runningTasks.stream()
                    .filter(PipelineTask::hasAlert)
                    .count();
            view.getDashboardPanel().updateActiveAlertsCount((int) activeAlerts);
            view.updatePipelineAndAlertChartData(); // Atualiza os dados do gráfico
        });
        // Listeners de Validação
        registerValidationListeners();

        // Listener de Fecho da Janela
        view.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Properties props = view.getConfigManager().loadProperties();
                boolean runInBackground = Boolean.parseBoolean(props.getProperty("system.runInBackground", "false"));

                if (runInBackground) {
                    view.setVisible(false);
                } else {
                    view.handleWindowExit();
                }
            }
        });
    }

    private void registerValidationListeners() {
        java.util.function.Predicate<String> urlValidator = s -> s.toLowerCase().startsWith("http://") || s.toLowerCase().startsWith("https://");
        view.getThingsboardUrlField().getDocument().addDocumentListener(view.createRealtimeValidator(view.getThingsboardUrlField(), urlValidator));
        view.getPbiUrlField().getDocument().addDocumentListener(view.createRealtimeValidator(view.getPbiUrlField(), urlValidator));

        java.util.function.Predicate<String> tcpValidator = s -> s.toLowerCase().startsWith("tcp://");
        view.getMqttBrokerUrlField().getDocument().addDocumentListener(view.createRealtimeValidator(view.getMqttBrokerUrlField(), tcpValidator));

        java.util.function.Predicate<String> fabricValidator = s -> s.contains("Endpoint=");
        view.getFabricConnectionStringField().getDocument().addDocumentListener(view.createRealtimeValidator(view.getFabricConnectionStringField(), fabricValidator));
    }

    private void registerAlertRuleListeners() {
        MetricTableModel tableModel = view.getMetricTableModel();
        AlertRuleTableModel alertRuleTableModel = view.getAlertRuleTableModel();

        view.getAddAlertRuleButton().addActionListener(e -> {
            if (!isAdmin) return;
            List<String> availableMetrics = tableModel.getSelectedMetrics().stream()
                    .map(MetricConfig::getOriginalName)
                    .collect(Collectors.toList());
            if (availableMetrics.isEmpty()) {
                JOptionPane.showMessageDialog(view, "Por favor, carregue e selecione as métricas primeiro.", "Métricas não encontradas", JOptionPane.WARNING_MESSAGE);
                return;
            }
            AlertRuleDialog dialog = new AlertRuleDialog(view, "Adicionar Nova Regra de Alerta", availableMetrics);
            dialog.setVisible(true);
            if (dialog.getAlertRule() != null) {
                alertRuleTableModel.addRule(dialog.getAlertRule());
                updateCategoryFilters();
                applyTableFilter();
            }
        });

        view.getEditAlertRuleButton().addActionListener(e -> {
            if (!isAdmin) return;
            int selectedRow = view.getAlertRulesTable().getSelectedRow(); // A variável estava em falta aqui
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(view, "Por favor, selecione uma regra para editar.", "Nenhuma Regra Selecionada", JOptionPane.WARNING_MESSAGE);
                return;
            }
            AlertRule ruleToEdit = alertRuleTableModel.getRuleAt(selectedRow);
            List<String> availableMetrics = tableModel.getSelectedMetrics().stream()
                    .map(MetricConfig::getOriginalName)
                    .collect(Collectors.toList());
            AlertRuleDialog dialog = new AlertRuleDialog(view, "Editar Regra de Alerta", availableMetrics);
            dialog.setAlertRule(ruleToEdit);
            dialog.setVisible(true);
            if (dialog.getAlertRule() != null) {
                alertRuleTableModel.updateRule(selectedRow, dialog.getAlertRule()); // Corrigido para updateRule
                updateCategoryFilters();
                applyTableFilter();
            }
        });

        view.getRemoveAlertRuleButton().addActionListener(e -> {
            if (!isAdmin) return;
            int selectedRow = view.getAlertRulesTable().getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(view, "Por favor, selecione uma regra para remover.", "Nenhuma Regra Selecionada", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(view, "Tem a certeza que deseja remover a regra selecionada?", "Confirmar Remoção", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                alertRuleTableModel.removeRule(selectedRow);
                updateCategoryFilters();
                applyTableFilter();
            }
        });

        view.getExportAlertRulesButton().addActionListener(e -> exportRules());

        view.getDuplicateAlertRuleButton().addActionListener(e -> {
            if (!isAdmin) return;
            int selectedRow = view.getAlertRulesTable().getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(view, "Por favor, selecione uma regra para duplicar.", "Nenhuma Regra Selecionada", JOptionPane.WARNING_MESSAGE);
                return;
            }
            AlertRule originalRule = alertRuleTableModel.getRuleAt(selectedRow);
            AlertRule newRule = new AlertRule(originalRule); // Usa o construtor de cópia
            newRule.setRuleName(originalRule.getRuleName() + " (Cópia)");
            alertRuleTableModel.addRule(newRule);
        });

        view.getImportAlertRulesButton().addActionListener(e -> importRules());
        view.getExportAlertRulesButton().addActionListener(e -> exportRules());
    }

    private void registerInsightRuleListeners() {
        MetricTableModel tableModel = view.getMetricTableModel();
        InsightRuleTableModel insightRuleTableModel = view.getInsightRuleTableModel();

        view.getAddInsightRuleButton().addActionListener(e -> {
            if (!isAdmin) return;
            List<String> availableMetrics = tableModel.getSelectedMetrics().stream()
                    .map(MetricConfig::getOriginalName)
                    .collect(Collectors.toList());
            if (availableMetrics.isEmpty()) {
                JOptionPane.showMessageDialog(view, "Por favor, carregue e selecione as métricas primeiro.", "Métricas não encontradas", JOptionPane.WARNING_MESSAGE);
                return;
            }
            InsightRuleDialog dialog = new InsightRuleDialog(view, "Adicionar Nova Regra de Alarme", availableMetrics);
            dialog.setVisible(true);
            if (dialog.getInsightRule() != null) {
                insightRuleTableModel.addRule(dialog.getInsightRule());
                updateCategoryFilters();
                applyTableFilter();
            }
        });

        view.getEditInsightRuleButton().addActionListener(e -> {
            if (!isAdmin) return;
            int selectedRow = view.getInsightRulesTable().getSelectedRow(); // A variável estava em falta aqui
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(view, "Por favor, selecione uma regra para editar.", "Nenhuma Regra Selecionada", JOptionPane.WARNING_MESSAGE);
                return;
            }
            InsightRule ruleToEdit = insightRuleTableModel.getRuleAt(selectedRow);
            List<String> availableMetrics = tableModel.getSelectedMetrics().stream()
                    .map(MetricConfig::getOriginalName)
                    .collect(Collectors.toList());
            InsightRuleDialog dialog = new InsightRuleDialog(view, "Editar Regra de Alarme", availableMetrics);
            dialog.setInsightRule(ruleToEdit);
            dialog.setVisible(true);
            if (dialog.getInsightRule() != null) {
                insightRuleTableModel.updateRule(selectedRow, dialog.getInsightRule()); // Corrigido para updateRule
                updateCategoryFilters();
                applyTableFilter();
            }
        });

        view.getRemoveInsightRuleButton().addActionListener(e -> {
            if (!isAdmin) return;
            int selectedRow = view.getInsightRulesTable().getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(view, "Por favor, selecione uma regra para remover.", "Nenhuma Regra Selecionada", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(view, "Tem a certeza que deseja remover a regra selecionada?", "Confirmar Remoção", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                insightRuleTableModel.removeRule(selectedRow);
                updateCategoryFilters();
                applyTableFilter();
            }
        });

        view.getExportInsightRulesButton().addActionListener(e -> exportRules());

        view.getDuplicateInsightRuleButton().addActionListener(e -> {
            if (!isAdmin) return;
            int selectedRow = view.getInsightRulesTable().getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(view, "Por favor, selecione uma regra para duplicar.", "Nenhuma Regra Selecionada", JOptionPane.WARNING_MESSAGE);
                return;
            }
            InsightRule originalRule = insightRuleTableModel.getRuleAt(selectedRow);
            InsightRule newRule = new InsightRule(originalRule); // Usa o construtor de cópia
            newRule.setRuleName(originalRule.getRuleName() + " (Cópia)");
            insightRuleTableModel.addRule(newRule);
        });

        view.getImportInsightRulesButton().addActionListener(e -> importRules());
        view.getExportInsightRulesButton().addActionListener(e -> exportRules());
    }

    /**
     * Recolhe todas as configurações da UI, constrói um objeto {@link PipelineConfiguration}
     * e solicita ao {@link main.java.com.gsmart.pipeline.PipelineManager} que inicie uma nova pipeline.
     * <p>
     * Realiza validações para garantir que uma fonte de dados válida, um destino e pelo menos
     * uma métrica foram selecionados antes de prosseguir. Em caso de sucesso, exibe uma
     * mensagem de confirmação ao utilizador.
     */
    private void launchPipeline() {
        if (view.getMetricsTable().isEditing()) {
            view.getMetricsTable().getCellEditor().stopCellEditing();
        }
        try {
            DestinationType destinationType;
            String destinationEndpoint;
            String selectedDestination = (String) view.getDestinationSelector().getSelectedItem();

            if ("Power BI Push URL".equals(selectedDestination)) {
                destinationType = DestinationType.POWER_BI;
                destinationEndpoint = view.getPbiUrlField().getText().trim();
                if (destinationEndpoint.isEmpty() || !destinationEndpoint.toLowerCase().startsWith("http")) {
                    JOptionPane.showMessageDialog(view, "Por favor, insira uma URL de Push do Power BI válida.", "Erro de Configuração", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                destinationType = DestinationType.FABRIC;
                destinationEndpoint = view.getFabricConnectionStringField().getText().trim();
                if (destinationEndpoint.isEmpty()) {
                    JOptionPane.showMessageDialog(view, "Por favor, insira uma 'Connection String' do Fabric válida.", "Erro de Configuração", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            List<MetricConfig> selectedConfigs = view.getMetricTableModel().getSelectedMetrics();
            if (selectedConfigs.isEmpty()) {
                JOptionPane.showMessageDialog(view, "Nenhuma métrica foi selecionada para envio!", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            IDataSource selectedDataSource = dataSourceController.createSelectedDataSource(selectedConfigs.stream().map(MetricConfig::getOriginalName).collect(Collectors.toList()));

            PipelineConfiguration config = new PipelineConfiguration(
                    selectedDataSource,
                    destinationType,
                    destinationEndpoint,
                    selectedConfigs,
                    view.getGlobalLogViewer(),
                    view.getAlertRuleTableModel().getRules(),
                    view.getInsightRuleTableModel().getRules(),
                    view.getTelegramTokenField().getText().trim(),
                    view.getTelegramChatIdField().getText().trim(),
                    view.getMqttBrokerUrlField().getText().trim()
            );

            view.getPipelineManager().launchPipeline(config);
            JOptionPane.showMessageDialog(view, "Pipeline para '" + selectedDataSource.getSourceName() + "' iniciada em segundo plano.", "Pipeline Iniciada", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Falha ao preparar a pipeline:\n" + e.getMessage(), "Erro Crítico", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Atualiza as opções nos JComboBox de filtro com base nas categorias existentes.
     */
    private void updateCategoryFilters() {
        // Para as regras de Alerta
        List<String> alertCategories = view.getAlertRuleTableModel().getRules().stream()
                .map(AlertRule::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        view.getAlertCategoryFilter().removeAllItems();
        view.getAlertCategoryFilter().addItem("Exibir Todas");
        alertCategories.forEach(view.getAlertCategoryFilter()::addItem);

        // Para as regras de Alarme
        List<String> insightCategories = view.getInsightRuleTableModel().getRules().stream()
                .map(InsightRule::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        view.getInsightCategoryFilter().removeAllItems();
        view.getInsightCategoryFilter().addItem("Exibir Todas");
        insightCategories.forEach(view.getInsightCategoryFilter()::addItem);
    }

    /**
     * Aplica um filtro à tabela de regras (Alertas ou Alarmes) com base na categoria
     * selecionada no respetivo JComboBox.
     * <p>
     * Utiliza um {@link javax.swing.RowFilter} para ocultar as linhas que não correspondem
     * à categoria selecionada, permitindo ao utilizador focar-se num subconjunto
     * específico de regras. Se "Exibir Todas" for selecionado, o filtro é removido.
     */
    private void applyTableFilter() {
        // Filtro para a tabela de Alertas
        TableRowSorter<AlertRuleTableModel> alertSorter = new TableRowSorter<>(view.getAlertRuleTableModel());
        view.getAlertRulesTable().setRowSorter(alertSorter);
        String alertCategory = (String) view.getAlertCategoryFilter().getSelectedItem();
        if (alertCategory != null && !alertCategory.equals("Exibir Todas")) {
            alertSorter.setRowFilter(RowFilter.regexFilter("^" + alertCategory + "$", 2)); // Filtra pela coluna 2 (Categoria)
        } else {
            alertSorter.setRowFilter(null);
        }

        // Filtro para a tabela de Alarmes
        TableRowSorter<InsightRuleTableModel> insightSorter = new TableRowSorter<>(view.getInsightRuleTableModel());
        view.getInsightRulesTable().setRowSorter(insightSorter);
        String insightCategory = (String) view.getInsightCategoryFilter().getSelectedItem();
        if (insightCategory != null && !insightCategory.equals("Exibir Todas")) {
            insightSorter.setRowFilter(RowFilter.regexFilter("^" + insightCategory + "$", 2)); // Filtra pela coluna 2 (Categoria)
        } else {
            insightSorter.setRowFilter(null);
        }
    }

    /**
     * Lida com a lógica de exportar todas as regras (Alertas e Alarmes) para um único ficheiro.
     * <p>
     * Abre um seletor de ficheiros para que o utilizador escolha o local de gravação.
     * As regras de alerta e de alarme são encapsuladas num objeto {@link main.java.com.gsmart.config.RulesContainer}
     * e serializadas para um ficheiro .dat, criando um backup completo.
     */
    private void exportRules() {
        if (!isAdmin) return; // Segurança extra

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Exportar Regras");
        fileChooser.setSelectedFile(new File("gsmart_rules.dat"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("GSmart Rules File (*.dat)", "dat"));

        int userSelection = fileChooser.showSaveDialog(view);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileToSave))) {

                // Pega nas regras atuais dos modelos de tabela
                List<AlertRule> alertRules = view.getAlertRuleTableModel().getRules();
                List<InsightRule> insightRules = view.getInsightRuleTableModel().getRules();

                // Coloca as listas no nosso "contentor"
                RulesContainer container = new RulesContainer(alertRules, insightRules);

                // Escreve o contentor no ficheiro
                oos.writeObject(container);

                JOptionPane.showMessageDialog(view, "Regras exportadas com sucesso para:\n" + fileToSave.getAbsolutePath(), "Exportação Concluída", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(view, "Ocorreu um erro ao salvar o ficheiro:\n" + ex.getMessage(), "Erro de Exportação", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Lida com a lógica de importar todas as regras de um ficheiro, substituindo as atuais.
     * <p>
     * Pede confirmação ao utilizador antes de prosseguir, pois a operação é destrutiva.
     * Lê um objeto {@link main.java.com.gsmart.config.RulesContainer} de um ficheiro .dat
     * e utiliza os dados para substituir completamente as regras existentes nos modelos
     * de tabela de alertas e alarmes.
     */
    private void importRules() {
        if (!isAdmin) return; // Segurança extra

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Importar Regras");
        fileChooser.setFileFilter(new FileNameExtensionFilter("GSmart Rules File (*.dat)", "dat"));

        int userSelection = fileChooser.showOpenDialog(view);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToLoad = fileChooser.getSelectedFile();

            int confirm = JOptionPane.showConfirmDialog(view,
                    "Tem a certeza que deseja importar as regras deste ficheiro?\nEsta ação irá substituir TODAS as suas regras atuais.",
                    "Confirmar Importação",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileToLoad))) {

                // Lê o "contentor" do ficheiro
                RulesContainer container = (RulesContainer) ois.readObject();

                // Extrai as listas de regras do contentor
                List<AlertRule> alertRules = container.getAlertRules();
                List<InsightRule> insightRules = container.getInsightRules();

                // Atualiza os modelos das tabelas com as novas regras
                view.getAlertRuleTableModel().setRules(alertRules);
                view.getInsightRuleTableModel().setRules(insightRules);

                // Atualiza a UI
                updateCategoryFilters();
                applyTableFilter();

                JOptionPane.showMessageDialog(view, "Regras importadas com sucesso!", "Importação Concluída", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException | ClassNotFoundException | ClassCastException ex) {
                JOptionPane.showMessageDialog(view, "Ocorreu um erro ao ler o ficheiro:\nO ficheiro pode estar corrompido ou não é um ficheiro de regras válido.\n\n" + ex.getMessage(), "Erro de Importação", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Exibe uma janela de diálogo com ajuda sobre como criar expressões.
     */
    private void showExpressionHelp() {
        String helpText = "<html>"
                + "<body style='width: 350px; font-family: Segoe UI; font-size: 12px;'>"
                + "<h2>Ajuda com Funções/Expressões</h2>"
                + "<p>Nesta coluna, pode transformar o valor original de uma métrica usando expressões matemáticas simples.</p>"
                + "<br>"
                + "<h3><b>Variáveis Disponíveis:</b></h3>"
                + "<ul>"
                + "<li><b>valor</b>: Representa o valor numérico original da métrica.</li>"
                + "</ul>"
                + "<br>"
                + "<h3><b>Exemplos Práticos:</b></h3>"
                + "<p><b>Converter de Watts para Quilowatts:</b></p>"
                + "<p><code>valor / 1000</code></p>"
                + "<p><b>Adicionar uma margem de 10%:</b></p>"
                + "<p><code>valor * 1.1</code></p>"
                + "<p><b>Converter temperatura de Celsius para Fahrenheit:</b></p>"
                + "<p><code>(valor * 9/5) + 32</code></p>"
                + "<p><b>Arredondar para duas casas decimais:</b></p>"
                + "<p><code>round(valor * 100) / 100</code></p>"
                + "<br>"
                + "<p><b>Nota:</b> A biblioteca usada é a <b>exp4j</b>. Funções como <code>sin</code>, <code>cos</code>, <code>log</code>, <code>abs</code> e <code>sqrt</code> também são suportadas.</p>"
                + "</body></html>";

        JOptionPane.showMessageDialog(view, helpText, "Ajuda com Expressões", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Exibe uma janela de diálogo com ajuda sobre como configurar o boot para o telegram.
     */
    private void showTelegramHelp() {
        String helpMessage = "<html><body style='width: 400px; font-family: Segoe UI; font-size: 12px;'>"
                + "<h3>Como obter o Token do Bot e o Chat ID?</h3>"
                + "<b>1. Crie um Bot com o BotFather:</b>"
                + "<ul><li>No Telegram, procure por <b>@BotFather</b> e inicie uma conversa.</li>"
                + "<li>Envie o comando <code>/newbot</code>.</li>"
                + "<li>Siga as instruções para dar um nome e um username ao seu bot.</li>"
                + "<li>O BotFather irá fornecer um <b>Token de API</b>. Copie e cole esse token no campo ao lado.</li></ul>"
                + "<b>2. Obtenha o seu Chat ID:</b>"
                + "<ul><li>Depois de criar o bot, envie-lhe uma mensagem qualquer.</li>"
                + "<li>Abra o seu navegador de internet e aceda ao seguinte URL, substituindo <b>SEU_TOKEN_AQUI</b> pelo token que acabou de receber:</li>"
                + "<code>https://api.telegram.org/bot<b>SEU_TOKEN_AQUI</b>/getUpdates</code>"
                + "<li>Procure no texto por <code>\"chat\":{\"id\":<b>123456789</b>,...}</code>. O número que aparece é o seu <b>Chat ID</b>.</li></ul>"
                + "</body></html>";
        JOptionPane.showMessageDialog(view, helpMessage, "Ajuda - Telegram", JOptionPane.INFORMATION_MESSAGE);
    }
}