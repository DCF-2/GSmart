package main.java.com.gsmart.controller;

import main.java.com.gsmart.GSmartGui;
import main.java.com.gsmart.config.MetricConfig;
import main.java.com.gsmart.sources.DatabaseSource;
import main.java.com.gsmart.sources.Device;
import main.java.com.gsmart.sources.DeviceProfile;
import main.java.com.gsmart.resources.IDataSource;
import main.java.com.gsmart.sources.ThingsBoardSource;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Controlador responsável por toda a lógica de interação com as fontes de dados,
 * como conexões, busca de metadados (dispositivos, tabelas) e carregamento de métricas.
 */

public class DataSourceController {

    private final GSmartGui view;

    public DataSourceController(GSmartGui view) {
        this.view = view;
    }

    public void connectToThingsboard() {
        view.getTbStatusLabel().setText("Conectando...");
        view.getTbStatusLabel().setForeground(Color.ORANGE);
        view.getTbConnectButton().setEnabled(false);
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                try {
                    return new ThingsBoardSource(getThingsboardUrl(), null, null, null, view.getSharedOkHttpClient()).testConnection();
                } catch (Exception e) {
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        view.getTbStatusLabel().setText("Conectado");
                        view.getTbStatusLabel().setForeground(new Color(0, 128, 0));
                        view.getDeviceProfileSelector().setEnabled(true);
                        loadDeviceProfiles();
                    } else {
                        throw new Exception("Falha na autenticação ou URL incorreta.");
                    }
                } catch (Exception e) {
                    view.getTbStatusLabel().setText("Falha!");
                    view.getTbStatusLabel().setForeground(Color.RED);
                } finally {
                    view.getTbConnectButton().setEnabled(true);
                }
            }
        }.execute();
    }

    public void connectToDatabase() {
        view.getDbStatusLabel().setText("Conectando...");
        view.getDbStatusLabel().setForeground(Color.ORANGE);
        view.getDbConnectButton().setEnabled(false);
        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                DatabaseSource tempSource = new DatabaseSource(view.getDbUrlField().getText().trim(), view.getDbUserField().getText().trim(), new String(view.getDbPasswordField().getPassword()), null, null);
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
                    view.getDbStatusLabel().setText("Conectado");
                    view.getDbStatusLabel().setForeground(new Color(0, 128, 0));
                    view.getDbTableSelector().removeAllItems();
                    tables.forEach(view.getDbTableSelector()::addItem);
                    view.getDbTableSelector().setEnabled(true);
                    if (!tables.isEmpty()) {
                        loadAvailableKeys();
                    }
                } catch (Exception e) {
                    view.getDbStatusLabel().setText("Falha!");
                    view.getDbStatusLabel().setForeground(Color.RED);
                    JOptionPane.showMessageDialog(view, "Não foi possível conectar ao Banco de Dados:\n" + e.getCause().getMessage(), "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
                } finally {
                    view.getDbConnectButton().setEnabled(true);
                }
            }
        }.execute();
    }

    public void loadDeviceProfiles() {
        Object previouslySelected = view.getDeviceProfileSelector().getSelectedItem();
        view.getTbConnectButton().setEnabled(false);
        new SwingWorker<List<DeviceProfile>, Void>() {
            @Override
            protected List<DeviceProfile> doInBackground() throws Exception {
                return new ThingsBoardSource(getThingsboardUrl(), null, null, null, view.getSharedOkHttpClient()).getDeviceProfiles();
            }

            @Override
            protected void done() {
                try {
                    List<DeviceProfile> profiles = get();
                    view.getDeviceProfileSelector().removeAllItems();
                    profiles.forEach(view.getDeviceProfileSelector()::addItem);
                    if (previouslySelected != null) {
                        for (int i = 0; i < view.getDeviceProfileSelector().getItemCount(); i++) {
                            if (Objects.equals(view.getDeviceProfileSelector().getItemAt(i), previouslySelected)) {
                                view.getDeviceProfileSelector().setSelectedIndex(i);
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    view.getTbStatusLabel().setText("Falha!");
                    view.getTbStatusLabel().setForeground(Color.RED);
                } finally {
                    view.getTbConnectButton().setEnabled(true);
                }
            }
        }.execute();
    }

    public void loadDevicesByProfile() {
        DeviceProfile selectedProfile = (DeviceProfile) view.getDeviceProfileSelector().getSelectedItem();
        if (selectedProfile == null) {
            view.getDeviceSelector().removeAllItems();
            return;
        }
        view.getDeviceSelector().setEnabled(false);
        new SwingWorker<List<Device>, Void>() {
            @Override
            protected List<Device> doInBackground() throws Exception {
                return new ThingsBoardSource(getThingsboardUrl(), null, null, null, view.getSharedOkHttpClient()).getDevicesByProfileId(selectedProfile.id());
            }

            @Override
            protected void done() {
                try {
                    List<Device> devices = get();
                    view.getDeviceSelector().removeAllItems();
                    devices.forEach(view.getDeviceSelector()::addItem);
                    if (!devices.isEmpty()) {
                        loadAvailableKeys();
                    }
                    view.getDeviceSelector().setEnabled(true);
                } catch (Exception e) {
                    // Tratar erro
                }
            }
        }.execute();
    }

    public void loadAvailableKeys() {
        String selectedSource = (String) view.getSourceSelector().getSelectedItem();
        try {
            if ("Thingsboard API".equals(selectedSource)) {
                Device selectedDevice = (Device) view.getDeviceSelector().getSelectedItem();
                if (selectedDevice == null) {
                    JOptionPane.showMessageDialog(view, "Por favor, conecte e selecione um dispositivo primeiro.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                new SwingWorker<List<String>, Void>() {
                    @Override
                    protected List<String> doInBackground() throws Exception {
                        return new ThingsBoardSource(getThingsboardUrl(), selectedDevice.id(), null, null, view.getSharedOkHttpClient()).getAvailableKeys();
                    }
                    @Override
                    protected void done() { handleKeysLoaded(this); }
                }.execute();
            } else if ("Banco de Dados Espelho".equals(selectedSource)) {
                String selectedTable = (String) view.getDbTableSelector().getSelectedItem();
                if (selectedTable == null) {
                    JOptionPane.showMessageDialog(view, "Por favor, conecte e selecione uma tabela primeiro.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                new SwingWorker<List<String>, Void>() {
                    @Override
                    protected List<String> doInBackground() throws Exception {
                        return new DatabaseSource(view.getDbUrlField().getText().trim(), view.getDbUserField().getText().trim(), new String(view.getDbPasswordField().getPassword()), selectedTable, null).getAvailableColumns(selectedTable);
                    }
                    @Override
                    protected void done() { handleKeysLoaded(this); }
                }.execute();
            }
        } catch (IllegalStateException e) {
            // Tratar erro
        }
    }

    private void handleKeysLoaded(SwingWorker<List<String>, Void> worker) {
        try {
            List<String> keys = worker.get();
            if (keys.isEmpty()) {
                JOptionPane.showMessageDialog(view, "Nenhuma métrica ou coluna foi encontrada para esta fonte!", "Aviso", JOptionPane.WARNING_MESSAGE);
                view.getMetricTableModel().clearMetrics();
                return;
            }

            List<MetricConfig> savedConfigs = view.getConfigManager().loadMetricConfigs();
            Map<String, MetricConfig> savedConfigMap = savedConfigs.stream()
                    .collect(Collectors.toMap(MetricConfig::getOriginalName, config -> config));

            List<MetricConfig> newConfigs = keys.stream().map(key ->
                    savedConfigMap.getOrDefault(key, new MetricConfig(key))
            ).collect(Collectors.toList());

            newConfigs.add(0, new MetricConfig("AlertaCritico", true, true));
            newConfigs.add(0, new MetricConfig("timestamp", true, true));
            newConfigs.add(0, new MetricConfig("UltimoAlarme", true, true));
            newConfigs.add(0, new MetricConfig("UltimoAlerta", true, true));
            newConfigs.add(0, new MetricConfig("DataDev", true, true));
            newConfigs.add(0, new MetricConfig("HoraDev", true, true));
            newConfigs.add(0, new MetricConfig("OrigemDados", true, true));

            view.getMetricTableModel().setMetrics(newConfigs);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Falha ao carregar as métricas/colunas:\n" + e.getCause().getMessage(), "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
        }
    }

    public IDataSource createSelectedDataSource(List<String> originalKeys) throws Exception {
        String selectedSource = (String) view.getSourceSelector().getSelectedItem();
        if ("Thingsboard API".equals(selectedSource)) {
            Device selectedDevice = (Device) view.getDeviceSelector().getSelectedItem();
            if (selectedDevice == null) {
                throw new IllegalStateException("Nenhum dispositivo do ThingsBoard foi selecionado.");
            }
            ThingsBoardSource tbSource = new ThingsBoardSource(getThingsboardUrl(), selectedDevice.id(), selectedDevice.name(), originalKeys, view.getSharedOkHttpClient());
            tbSource.testConnectionAndThrow();
            return tbSource;
        } else if ("Banco de Dados Espelho".equals(selectedSource)) {
            DatabaseSource dbSource = new DatabaseSource(view.getDbUrlField().getText().trim(), view.getDbUserField().getText().trim(), new String(view.getDbPasswordField().getPassword()), (String) view.getDbTableSelector().getSelectedItem(), originalKeys);
            dbSource.testConnectionAndThrow();
            return dbSource;
        }
        throw new IllegalStateException("Nenhuma fonte de dados válida foi selecionada.");
    }

    // --- MÉTODOS AUXILIARES MOVIDOS DA GSmartGui ---

    /**
     * Obtém e valida a URL do servidor ThingsBoard a partir do campo de texto correspondente.
     * Lança uma IllegalStateException se o campo estiver vazio.
     *
     * @return A URL do ThingsBoard como uma String.
     */
    private String getThingsboardUrl() {
        String url = view.getThingsboardUrlField().getText().trim();
        if (url.isEmpty()) {
            JOptionPane.showMessageDialog(view, "A URL do Servidor ThingsBoard não pode estar vazia.", "Erro de Configuração", JOptionPane.ERROR_MESSAGE);
            throw new IllegalStateException("URL do ThingsBoard não fornecida.");
        }
        return url;
    }

    /**
     * Restaura o estado do botão "Carregar Métricas", reativando-o e
     * redefinindo o seu texto para o estado inicial.
     */
    private void setLoadButtonReady() {
        // Este método não é mais necessário, pois a lógica de ativar/desativar
        // o botão de conexão já lida com isto. Podemos apagá-lo ou mantê-lo
        // para futuras referências. Por agora, vamos mantê-lo vazio.
    }

    /**
     * Exibe um diálogo de seleção (JOptionPane) com uma lista de opções.
     *
     * @param options A lista de strings a serem exibidas no dropdown.
     * @param title   O título da janela de diálogo.
     * @param message A mensagem a ser exibida ao utilizador.
     * @return A string selecionada pelo utilizador ou null se o diálogo for cancelado.
     */
    private String showDropdownDialog(List<String> options, String title, String message) {
        if (options == null || options.isEmpty()) return null;
        Object[] possibilities = options.toArray();
        return (String) JOptionPane.showInputDialog(view, message, title, JOptionPane.PLAIN_MESSAGE, null, possibilities, options.get(0));
    }
}