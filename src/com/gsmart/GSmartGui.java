// Localização: src/main/java/com/gsmart/GSmartGui.java
package com.gsmart;

import com.gsmart.sources.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GSmartGui extends JFrame {

    private static final Logger logger = LoggerFactory.getLogger(GSmartGui.class);

    // --- Componentes da Interface ---
    private final JComboBox<String> sourceSelector;
    private final JButton startButton;
    private final JButton loadKeysButton;
    private final JTextField pbiUrlField;

    // --- Componentes de Configuração de Fonte ---
    private final JPanel thingsboardConfigPanel;
    private final JComboBox<DeviceProfile> deviceProfileSelector;
    private final JButton refreshProfilesButton;
    private final JComboBox<Device> deviceSelector;

    private final JPanel databaseConfigPanel;
    private final JTextField dbUrlField;
    private final JTextField dbUserField;
    private final JPasswordField dbPasswordField;
    private final JTextField dbTableNameField;

    private final JTable metricsTable;
    private final MetricTableModel tableModel;
    private final JPanel sourceConfigCardPanel;

    private final LogViewerWindow globalLogViewer;

    // --- Variáveis de Configuração ---
    private String chaveDeAcumuloSelecionada;
    private LogicConfig logicConfig;

    public GSmartGui(LogViewerWindow logViewer) {
        this.globalLogViewer = logViewer;

        setTitle("GSmart - Configurador de Pipeline v3.1.1");
        setSize(800, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // --- PAINEL DE CONFIGURAÇÃO GERAL (TOPO) ---
        JPanel topConfigurationPanel = new JPanel();
        topConfigurationPanel.setLayout(new BoxLayout(topConfigurationPanel, BoxLayout.Y_AXIS));

        JPanel sourceSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sourceSelectionPanel.setBorder(BorderFactory.createTitledBorder("1. Selecione a Fonte de Dados"));
        sourceSelectionPanel.add(new JLabel("Tipo de Fonte:"));
        String[] sources = {"Thingsboard API", "Banco de Dados Espelho"};
        sourceSelector = new JComboBox<>(sources);
        sourceSelectionPanel.add(sourceSelector);

        sourceConfigCardPanel = new JPanel(new CardLayout());

        thingsboardConfigPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbcTb = new GridBagConstraints();
        gbcTb.insets = new Insets(4, 5, 4, 5);
        gbcTb.anchor = GridBagConstraints.WEST;
        gbcTb.gridx = 0; gbcTb.gridy = 0; gbcTb.gridwidth = 1; gbcTb.fill = GridBagConstraints.NONE;
        thingsboardConfigPanel.add(new JLabel("Perfil de Dispositivo (Tipo):"), gbcTb);
        gbcTb.gridx = 1; gbcTb.gridy = 0; gbcTb.weightx = 1.0; gbcTb.fill = GridBagConstraints.HORIZONTAL;
        deviceProfileSelector = new JComboBox<>();
        thingsboardConfigPanel.add(deviceProfileSelector, gbcTb);
        gbcTb.gridx = 2; gbcTb.gridy = 0; gbcTb.weightx = 0; gbcTb.fill = GridBagConstraints.NONE;
        refreshProfilesButton = new JButton("Atualizar Perfis");
        thingsboardConfigPanel.add(refreshProfilesButton, gbcTb);
        gbcTb.gridx = 0; gbcTb.gridy = 1;
        thingsboardConfigPanel.add(new JLabel("Dispositivo:"), gbcTb);
        gbcTb.gridx = 1; gbcTb.gridy = 1; gbcTb.gridwidth = 2; gbcTb.fill = GridBagConstraints.HORIZONTAL;
        deviceSelector = new JComboBox<>();
        thingsboardConfigPanel.add(deviceSelector, gbcTb);

        databaseConfigPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbcDb = new GridBagConstraints();
        gbcDb.insets = new Insets(2, 5, 2, 5);
        gbcDb.anchor = GridBagConstraints.WEST;
        gbcDb.gridx = 0; gbcDb.gridy = 0; databaseConfigPanel.add(new JLabel("URL do Banco (JDBC):"), gbcDb);
        gbcDb.gridx = 1; gbcDb.gridy = 0; gbcDb.weightx = 1.0; gbcDb.fill = GridBagConstraints.HORIZONTAL; dbUrlField = new JTextField("jdbc:postgresql://localhost:5432/seu_banco", 30); databaseConfigPanel.add(dbUrlField, gbcDb);
        gbcDb.gridx = 0; gbcDb.gridy = 1; gbcDb.weightx = 0; gbcDb.fill = GridBagConstraints.NONE; databaseConfigPanel.add(new JLabel("Usuário:"), gbcDb);
        gbcDb.gridx = 1; gbcDb.gridy = 1; gbcDb.fill = GridBagConstraints.HORIZONTAL; dbUserField = new JTextField("postgres"); databaseConfigPanel.add(dbUserField, gbcDb);
        gbcDb.gridx = 0; gbcDb.gridy = 2; databaseConfigPanel.add(new JLabel("Senha:"), gbcDb);
        gbcDb.gridx = 1; gbcDb.gridy = 2; gbcDb.fill = GridBagConstraints.HORIZONTAL; dbPasswordField = new JPasswordField(""); databaseConfigPanel.add(dbPasswordField, gbcDb);
        gbcDb.gridx = 0; gbcDb.gridy = 3; databaseConfigPanel.add(new JLabel("Nome da Tabela:"), gbcDb);
        gbcDb.gridx = 1; gbcDb.gridy = 3; gbcDb.fill = GridBagConstraints.HORIZONTAL; dbTableNameField = new JTextField(); databaseConfigPanel.add(dbTableNameField, gbcDb);

        sourceConfigCardPanel.add(thingsboardConfigPanel, "Thingsboard API");
        sourceConfigCardPanel.add(databaseConfigPanel, "Banco de Dados Espelho");

        JPanel loadKeysPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        loadKeysButton = new JButton("2. Carregar Métricas da Fonte Selecionada");
        loadKeysPanel.add(loadKeysButton);

        JPanel destinationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        destinationPanel.setBorder(BorderFactory.createTitledBorder("3. Configurar Destino dos Dados"));
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
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        metricsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        JScrollPane keysScrollPane = new JScrollPane(metricsTable);
        keysScrollPane.setBorder(BorderFactory.createTitledBorder("4. Selecionar e Mapear Métricas para Envio"));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        startButton = new JButton("▶ Iniciar Monitoramento em Nova Janela");
        startButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        actionPanel.add(startButton);

        JButton viewLogsButton = new JButton("Ver Logs da Aplicação");
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(actionPanel, BorderLayout.CENTER);
        bottomPanel.add(viewLogsButton, BorderLayout.EAST);

        setLayout(new BorderLayout(5, 5));
        add(topConfigurationPanel, BorderLayout.NORTH);
        add(keysScrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        sourceSelector.addItemListener(e -> toggleSourceFields());
        refreshProfilesButton.addActionListener(e -> loadDeviceProfiles());
        deviceProfileSelector.addItemListener(e -> { if (e.getStateChange() == ItemEvent.SELECTED) loadDevicesByProfile(); });
        loadKeysButton.addActionListener(e -> loadAvailableKeys());
        startButton.addActionListener(e -> startMonitoring());
        viewLogsButton.addActionListener(e -> this.globalLogViewer.setVisible(true));

        toggleSourceFields();
        loadDeviceProfiles();
    }

    private void startMonitoring() {
        try {
            String pbiUrl = pbiUrlField.getText().trim();
            if (pbiUrl.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor, insira a URL de Push do Power BI.", "Erro de Configuração", JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<MetricConfig> selectedConfigs = tableModel.getSelectedMetrics();
            if (selectedConfigs.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nenhuma métrica foi selecionada para envio!", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (this.logicConfig == null || this.chaveDeAcumuloSelecionada == null) {
                JOptionPane.showMessageDialog(this, "A configuração de Métrica de Acúmulo e da Lógica de Negócio deve ser definida.\nPor favor, carregue as métricas novamente.", "Erro de Configuração", JOptionPane.ERROR_MESSAGE);
                return;
            }

            IDataSource selectedDataSource = createSelectedDataSource(selectedConfigs.stream().map(MetricConfig::getOriginalName).collect(Collectors.toList()));

            MonitoringWindow monitor = new MonitoringWindow(selectedDataSource, pbiUrl, this.chaveDeAcumuloSelecionada, selectedConfigs, this.logicConfig, this.globalLogViewer);
            monitor.setVisible(true);
            monitor.start();

        } catch (Exception e) {
            logger.error("Falha ao preparar a pipeline: {}", e.getMessage(), e);
            JOptionPane.showMessageDialog(this, "Falha ao preparar a pipeline:\n" + e.getMessage(), "Erro Crítico", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void toggleSourceFields() {
        CardLayout cl = (CardLayout) (sourceConfigCardPanel.getLayout());
        String selectedSource = (String) sourceSelector.getSelectedItem();
        if (selectedSource != null) {
            cl.show(sourceConfigCardPanel, selectedSource);
        }
    }

    private void loadAvailableKeys() {
        loadKeysButton.setEnabled(false);
        loadKeysButton.setText("Carregando...");
        String selectedSource = (String) sourceSelector.getSelectedItem();

        if ("Thingsboard API".equals(selectedSource)) {
            Device selectedDevice = (Device) deviceSelector.getSelectedItem();
            if (selectedDevice == null) {
                JOptionPane.showMessageDialog(this, "Por favor, selecione um dispositivo primeiro.", "Aviso", JOptionPane.WARNING_MESSAGE);
                setLoadButtonReady();
                return;
            }
            new SwingWorker<List<String>, Void>() {
                @Override protected List<String> doInBackground() throws Exception {
                    return new ThingsBoardSource("http://10.8.0.5:8080", selectedDevice.id(), null).getAvailableKeys();
                }
                @Override protected void done() { handleKeysLoaded(this); }
            }.execute();

        } else if ("Banco de Dados Espelho".equals(selectedSource)) {
            String dbUrl = dbUrlField.getText().trim();
            String dbUser = dbUserField.getText().trim();
            String dbPassword = new String(dbPasswordField.getPassword());
            String dbTable = dbTableNameField.getText().trim();
            if (dbUrl.isEmpty() || dbUser.isEmpty() || dbTable.isEmpty()) {
                JOptionPane.showMessageDialog(this, "URL do Banco, Usuário e Nome da Tabela são obrigatórios.", "Erro", JOptionPane.ERROR_MESSAGE);
                setLoadButtonReady();
                return;
            }
            new SwingWorker<List<String>, Void>() {
                @Override protected List<String> doInBackground() throws Exception {
                    return new DatabaseSource(dbUrl, dbUser, dbPassword, dbTable, null).getAvailableColumns(dbTable);
                }
                @Override protected void done() { handleKeysLoaded(this); }
            }.execute();
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

            String acumuloKey = showDropdownDialog(keys, "Configuração da Métrica de Acúmulo", "1/4: Selecione a métrica para o 'Acúmulo por Hora':");
            if (acumuloKey == null) { logger.warn("Usuário cancelou a configuração. Nenhuma métrica foi carregada."); tableModel.clearMetrics(); return; }
            this.chaveDeAcumuloSelecionada = acumuloKey;

            String tempKey = showDropdownDialog(keys, "Mapeamento da Lógica", "2/4: Selecione a métrica de 'Temperatura':\n(Pode cancelar se não aplicável)");
            String fpKey = showDropdownDialog(keys, "Mapeamento da Lógica", "3/4: Selecione a métrica de 'Fator de Potência':\n(Pode cancelar se não aplicável)");
            String ptotKey = showDropdownDialog(keys, "Mapeamento da Lógica", "4/4: Selecione a métrica de 'Potência Ativa':\n(Pode cancelar se não aplicável)");

            this.logicConfig = new LogicConfig(tempKey, fpKey, ptotKey);

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
        loadKeysButton.setText("2. Carregar Métricas da Fonte Selecionada");
    }

    private IDataSource createSelectedDataSource(List<String> originalKeys) {
        String selectedSource = (String) sourceSelector.getSelectedItem();
        if ("Thingsboard API".equals(selectedSource)) {
            Device selectedDevice = (Device) deviceSelector.getSelectedItem();
            if (selectedDevice == null) { throw new IllegalStateException("Nenhum dispositivo do ThingsBoard foi selecionado."); }
            return new ThingsBoardSource("http://10.8.0.5:8080", selectedDevice.id(), originalKeys);
        } else if ("Banco de Dados Espelho".equals(selectedSource)) {
            String dbUrl = dbUrlField.getText().trim();
            String dbUser = dbUserField.getText().trim();
            String dbPassword = new String(dbPasswordField.getPassword());
            String dbTable = dbTableNameField.getText().trim();
            return new DatabaseSource(dbUrl, dbUser, dbPassword, dbTable, originalKeys);
        }
        throw new IllegalStateException("Nenhuma fonte de dados válida foi selecionada.");
    }

    private String showDropdownDialog(List<String> options, String title, String message) {
        if (options == null || options.isEmpty()) return null;
        Object[] possibilities = options.toArray();
        return (String) JOptionPane.showInputDialog(this, message, title, JOptionPane.PLAIN_MESSAGE, null, possibilities, options.get(0));
    }

    private void loadDeviceProfiles() {
        refreshProfilesButton.setEnabled(false);
        refreshProfilesButton.setText("Buscando...");
        new SwingWorker<List<DeviceProfile>, Void>() {
            @Override
            protected List<DeviceProfile> doInBackground() throws Exception {
                return new ThingsBoardSource("http://10.8.0.5:8080", null, null).getDeviceProfiles();
            }
            @Override
            protected void done() {
                try {
                    List<DeviceProfile> profiles = get();
                    deviceProfileSelector.removeAllItems();
                    profiles.forEach(deviceProfileSelector::addItem);
                } catch (Exception e) { logger.error("Falha ao buscar perfis de dispositivo: {}", e.getMessage(), e);
                } finally { refreshProfilesButton.setEnabled(true); refreshProfilesButton.setText("Atualizar Perfis"); }
            }
        }.execute();
    }

    private void loadDevicesByProfile() {
        DeviceProfile selectedProfile = (DeviceProfile) deviceProfileSelector.getSelectedItem();
        if (selectedProfile == null) { deviceSelector.removeAllItems(); return; }
        new SwingWorker<List<Device>, Void>() {
            @Override
            protected List<Device> doInBackground() throws Exception {
                return new ThingsBoardSource("http://10.8.0.5:8080", null, null).getDevicesByProfileId(selectedProfile.id());
            }
            @Override
            protected void done() {
                try {
                    List<Device> devices = get();
                    deviceSelector.removeAllItems();
                    devices.forEach(deviceSelector::addItem);
                } catch (Exception e) { logger.error("Falha ao buscar dispositivos para o perfil '{}': {}", selectedProfile.name(), e.getMessage(), e); }
            }
        }.execute();
    }
}

class MetricTableModel extends AbstractTableModel {
    private final String[] columnNames = {"Enviar", "Nome Original", "Enviar Como (Alias)"};
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
    @Override public boolean isCellEditable(int rowIndex, int columnIndex) { return columnIndex == 0 || columnIndex == 2; }

    @Override public Object getValueAt(int rowIndex, int columnIndex) {
        MetricConfig metric = metrics.get(rowIndex);
        switch (columnIndex) {
            case 0: return metric.isSelected();
            case 1: return metric.getOriginalName();
            case 2: return metric.getAlias();
            default: return null;
        }
    }

    @Override public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        MetricConfig metric = metrics.get(rowIndex);
        switch (columnIndex) {
            case 0: metric.setSelected((Boolean) aValue); break;
            case 2: metric.setAlias((String) aValue); break;
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }
}