package main.java.com.gsmart.controller;

import main.java.com.gsmart.GSmartGui;
import main.java.com.gsmart.Gui.AlertRuleTableModel;
import main.java.com.gsmart.Gui.InsightRuleTableModel;
import main.java.com.gsmart.Gui.MetricTableModel;
import main.java.com.gsmart.Gui.windows.AlertRuleDialog;
import main.java.com.gsmart.Gui.windows.InsightRuleDialog;
import main.java.com.gsmart.config.AlertRule;
import main.java.com.gsmart.config.InsightRule;
import main.java.com.gsmart.config.MetricConfig;
import main.java.com.gsmart.config.PipelineConfiguration;
import main.java.com.gsmart.resources.DestinationType;
import main.java.com.gsmart.resources.IDataSource;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador responsável por configurar todos os listeners e gerir as ações
 * do utilizador na interface GSmart.
 */
public class ActionController {

    private final GSmartGui view;
    private final DataSourceController dataSourceController;

    public ActionController(GSmartGui view, DataSourceController dataSourceController) {
        this.view = view;
        this.dataSourceController = dataSourceController;
        registerListeners();
    }

    private void registerListeners() {
        // Listeners da UI Principal
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
        view.getStopAllButton().addActionListener(e -> view.getPipelineManager().stopAllPipelines());
        view.getMonitoringButton().addActionListener(e -> view.showTaskManager());
        view.getManageUsersButton().addActionListener(e -> view.showUserManagementWindow());

        // Listeners das Regras de Alerta
        registerAlertRuleListeners();

        // Listeners das Regras de Alarme
        registerInsightRuleListeners();

        // Listeners do Dashboard e da Janela
        view.getDashboardPanel().getOpenTaskManagerButton().addActionListener(e -> view.showTaskManager());
        view.getPipelineManager().addTaskListUpdatedListener(() -> {
            int taskCount = view.getPipelineManager().getRunningTasks().size();
            view.getDashboardPanel().updateActivePipelinesCount(taskCount);
        });

        // Listeners de Validação
        registerValidationListeners();

        // Listener de Fecho da Janela
        view.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (view.getRunInBackgroundCheckBox().isSelected()) {
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
        JButton addRuleButton = (JButton) view.getAlertRuleButtons().getComponent(0);
        JButton editRuleButton = (JButton) view.getAlertRuleButtons().getComponent(1);
        JButton removeRuleButton = (JButton) view.getAlertRuleButtons().getComponent(2);
        MetricTableModel tableModel = view.getMetricTableModel();
        AlertRuleTableModel alertRuleTableModel = view.getAlertRuleTableModel();

        addRuleButton.addActionListener(e -> {
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
            }
        });

        editRuleButton.addActionListener(e -> {
            int selectedRow = view.getAlertRulesTable().getSelectedRow();
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
                alertRuleTableModel.updateRule(selectedRow, dialog.getAlertRule());
            }
        });

        removeRuleButton.addActionListener(e -> {
            int selectedRow = view.getAlertRulesTable().getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(view, "Por favor, selecione uma regra para remover.", "Nenhuma Regra Selecionada", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(view, "Tem a certeza que deseja remover a regra selecionada?", "Confirmar Remoção", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                alertRuleTableModel.removeRule(selectedRow);
            }
        });
    }

    private void registerInsightRuleListeners() {
        JButton addInsightButton = (JButton) view.getInsightRuleButtons().getComponent(0);
        JButton editInsightButton = (JButton) view.getInsightRuleButtons().getComponent(1);
        JButton removeInsightButton = (JButton) view.getInsightRuleButtons().getComponent(2);
        MetricTableModel tableModel = view.getMetricTableModel();
        InsightRuleTableModel insightRuleTableModel = view.getInsightRuleTableModel();

        addInsightButton.addActionListener(e -> {
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
            }
        });

        editInsightButton.addActionListener(e -> {
            int selectedRow = view.getInsightRulesTable().getSelectedRow();
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
                insightRuleTableModel.updateRule(selectedRow, dialog.getInsightRule());
            }
        });

        removeInsightButton.addActionListener(e -> {
            int selectedRow = view.getInsightRulesTable().getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(view, "Por favor, selecione uma regra para remover.", "Nenhuma Regra Selecionada", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(view, "Tem a certeza que deseja remover a regra selecionada?", "Confirmar Remoção", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                insightRuleTableModel.removeRule(selectedRow);
            }
        });
    }

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
}