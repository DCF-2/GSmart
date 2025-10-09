// Localização: src/main/java/com/gsmart/controller/DataSourceController.java
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
                    ThingsBoardSource tempSource = new ThingsBoardSource(getThingsboardUrl(), getThingsboardUser(), getThingsboardPassword(), null, null, null, view.getSharedOkHttpClient());
                    return tempSource.testConnection();
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
                ThingsBoardSource tempSource = new ThingsBoardSource(getThingsboardUrl(), getThingsboardUser(), getThingsboardPassword(), null, null, null, view.getSharedOkHttpClient());
                return tempSource.getDeviceProfiles();
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
                ThingsBoardSource tempSource = new ThingsBoardSource(getThingsboardUrl(), getThingsboardUser(), getThingsboardPassword(), null, null, null, view.getSharedOkHttpClient());
                return tempSource.getDevicesByProfileId(selectedProfile.id());
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
                // --- ✨ CORREÇÃO AQUI ✨ ---
                // O SwingWorker agora espera uma List<String> e chama o método correto (getAvailableKeys).
                new SwingWorker<List<String>, Void>() {
                    @Override
                    protected List<String> doInBackground() throws Exception {
                        ThingsBoardSource tempSource = new ThingsBoardSource(getThingsboardUrl(), getThingsboardUser(), getThingsboardPassword(), selectedDevice.id(), null, null, view.getSharedOkHttpClient());
                        return tempSource.getAvailableKeys();
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

            newConfigs.add(0, new MetricConfig("OrigemDados", true, true));
            newConfigs.add(0, new MetricConfig("HoraDev", true, true));
            newConfigs.add(0, new MetricConfig("DataDev", true, true));
            newConfigs.add(0, new MetricConfig("UltimoAlerta", true, true));
            newConfigs.add(0, new MetricConfig("UltimoAlarme", true, true));
            newConfigs.add(0, new MetricConfig("timestamp", true, true));
            newConfigs.add(0, new MetricConfig("AlertaCritico", true, true));

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
            ThingsBoardSource tbSource = new ThingsBoardSource(getThingsboardUrl(), getThingsboardUser(), getThingsboardPassword(), selectedDevice.id(), selectedDevice.name(), originalKeys, view.getSharedOkHttpClient());
            tbSource.testConnectionAndThrow();
            return tbSource;
        } else if ("Banco de Dados Espelho".equals(selectedSource)) {
            DatabaseSource dbSource = new DatabaseSource(view.getDbUrlField().getText().trim(), view.getDbUserField().getText().trim(), new String(view.getDbPasswordField().getPassword()), (String) view.getDbTableSelector().getSelectedItem(), originalKeys);
            dbSource.testConnectionAndThrow();
            return dbSource;
        }
        throw new IllegalStateException("Nenhuma fonte de dados válida foi selecionada.");
    }

    // --- MÉTODOS AUXILIARES ---

    private String getThingsboardUrl() {
        String url = view.getThingsboardUrlField().getText().trim();
        if (url.isEmpty()) {
            JOptionPane.showMessageDialog(view, "A URL do Servidor ThingsBoard não pode estar vazia.", "Erro de Configuração", JOptionPane.ERROR_MESSAGE);
            throw new IllegalStateException("URL do ThingsBoard não fornecida.");
        }
        return url;
    }

    // Apenas a versão com validação foi mantida.
    private String getThingsboardUser() {
        String user = view.getTbUserField().getText().trim();
        if (user.isEmpty()) {
            throw new IllegalStateException("Utilizador do ThingsBoard não fornecido.");
        }
        return user;
    }

    private String getThingsboardPassword() {
        String pass = new String(view.getTbPassField().getPassword());
        if (pass.isEmpty()) {
            throw new IllegalStateException("Senha do ThingsBoard não fornecida.");
        }
        return pass;
    }
}