// Localização: src/main/java/com/gsmart/Gui/windows/SettingsDialog.java
package main.java.com.gsmart.Gui.windows;

import main.java.com.gsmart.GSmartGui;
import main.java.com.gsmart.config.ConfigManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Properties;

/**
 * Janela de diálogo para as configurações gerais da aplicação GSmart.
 * <p>
 * Permite ao utilizador configurar o comportamento da aplicação, como a opção de
 * executar em segundo plano, e fornece um atalho para a gestão das pipelines
 * de início automático. As configurações são carregadas no arranque da janela
 * e guardadas quando esta é fechada.
 */
public class SettingsDialog extends JDialog {

    private final ConfigManager configManager;
    private final GSmartGui parent;
    private JCheckBox runInBackgroundCheckBox;

    /**
     * Constrói a janela de diálogo de configurações gerais.
     *
     * @param owner A janela principal da aplicação ({@link GSmartGui}), que é a "dona" deste diálogo.
     */
    public SettingsDialog(GSmartGui owner) {
        super(owner, "Configurações Gerais", true); // Modal
        this.parent = owner;
        this.configManager = new ConfigManager();

        // --- Layout Principal ---
        setLayout(new BorderLayout(10, 10));
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // --- Secção de Comportamento ---
        JPanel behaviorPanel = new JPanel(new BorderLayout());
        behaviorPanel.setBorder(BorderFactory.createTitledBorder("Comportamento da Aplicação"));

        runInBackgroundCheckBox = new JCheckBox("Executar em 2º plano ao fechar a janela");
        runInBackgroundCheckBox.setToolTipText("Se marcado, o GSmart continuará a correr na bandeja do sistema.");
        behaviorPanel.add(runInBackgroundCheckBox, BorderLayout.NORTH);
        mainPanel.add(behaviorPanel);

        // --- Secção de Início Automático ---
        JPanel autoStartPanel = new JPanel(new BorderLayout());
        autoStartPanel.setBorder(BorderFactory.createTitledBorder("Início Automático de Pipelines"));

        JButton manageAutoStartButton = new JButton("Gerir Pipelines de Início Automático");
        manageAutoStartButton.addActionListener(e -> parent.showAutoStartManager());
        autoStartPanel.add(manageAutoStartButton);
        mainPanel.add(autoStartPanel);

        // --- Botão de Fechar ---
        JPanel closeButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Fechar");
        closeButton.addActionListener(e -> dispose());
        closeButtonPanel.add(closeButton);

        // --- Montagem Final ---
        add(mainPanel, BorderLayout.CENTER);
        add(closeButtonPanel, BorderLayout.SOUTH);

        // Carrega as configurações atuais ao abrir
        loadSettings();

        // Salva as configurações ao fechar
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveSettings();
            }
        });

        pack();
        setLocationRelativeTo(owner);
    }

    private void loadSettings() {
        Properties props = configManager.loadProperties();
        runInBackgroundCheckBox.setSelected(Boolean.parseBoolean(props.getProperty("system.runInBackground", "false")));
    }

    private void saveSettings() {
        Properties props = configManager.loadProperties();
        props.setProperty("system.runInBackground", String.valueOf(runInBackgroundCheckBox.isSelected()));
        configManager.saveProperties(props);
    }
}