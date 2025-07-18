// Localização: src/com/gsmart/gui/AlertRuleDialog.java
package com.gsmart.Gui.windows;

import com.gsmart.config.AlertRule;
import com.gsmart.resources.ConditionType;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Uma janela de diálogo (JDialog) para criar ou editar uma {@code AlertRule}.
 *
 * Fornece um formulário para que o utilizador possa configurar todos os
 * parâmetros de uma regra de alerta crítico de forma intuitiva.
 *
 * @see com.gsmart.config.AlertRule
 */
public class AlertRuleDialog extends JDialog {
    private JTextField ruleNameField;
    private JComboBox<String> metricComboBox;
    private JComboBox<ConditionType> conditionComboBox;
    private JTextField thresholdValueField;
    private JTextArea messageToSendArea;
    private JCheckBox sendToMqttCheckBox;
    private JCheckBox sendToTelegramCheckBox;
    private JSpinner cooldownSpinner;
    private JButton saveButton;
    private JButton cancelButton;

    private AlertRule alertRule;

    public AlertRuleDialog(Frame owner, String title, List<String> availableMetrics) {
        super(owner, title, true); // true para ser uma janela modal
        initComponents(availableMetrics);
        setupLayout();

        cancelButton.addActionListener(e -> dispose());
        saveButton.addActionListener(e -> onSave());

        setSize(450, 350);
        setLocationRelativeTo(owner);
    }

    private void initComponents(List<String> availableMetrics) {
        ruleNameField = new JTextField(20);
        metricComboBox = new JComboBox<>(availableMetrics.toArray(new String[0]));
        conditionComboBox = new JComboBox<>(ConditionType.values());
        thresholdValueField = new JTextField(10);
        messageToSendArea = new JTextArea(5, 20);
        messageToSendArea.setLineWrap(true);
        messageToSendArea.setWrapStyleWord(true);
        sendToMqttCheckBox = new JCheckBox("Enviar via MQTT", true);
        sendToTelegramCheckBox = new JCheckBox("Enviar via Telegram", true);
        cooldownSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));

        saveButton = new JButton("Salvar");
        cancelButton = new JButton("Cancelar");
    }

    private void setupLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Linha 1: Nome da Regra
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Nome da Regra:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(ruleNameField, gbc);

        // Linha 2: Métrica
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        add(new JLabel("Se a métrica:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(metricComboBox, gbc);

        // Linha 3: Condição
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        add(new JLabel("For:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(conditionComboBox, gbc);

        // Linha 4: Valor Limiar
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        add(new JLabel("O valor de:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(thresholdValueField, gbc);

        // Linha 5: Mensagem
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        add(new JLabel("Enviar alerta:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        add(new JScrollPane(messageToSendArea), gbc);

        // --- CooldownSeconds ---
        JPanel cooldownPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        cooldownPanel.add(new JLabel("Reenviar alerta apenas após:"));
        cooldownPanel.add(cooldownSpinner);
        cooldownPanel.add(new JLabel(" segundos"));
        gbc.gridx = 1; gbc.gridy = 5; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weighty = 0;
        gbc.insets = new Insets(10, 5, 0, 5); // Adiciona um espaço extra em cima
        add(cooldownPanel, gbc);
        gbc.insets = new Insets(5, 5, 5, 5); // Restaura o espaçamento normal

        // --- Mqtt ou Telegram (DESTINOS) ---
        JPanel destinationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        destinationPanel.add(sendToMqttCheckBox);
        destinationPanel.add(sendToTelegramCheckBox);
        gbc.gridx = 1; gbc.gridy = 6; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weighty = 0;
        add(destinationPanel, gbc);

        // Linha 6: Botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        add(buttonPanel, gbc);
    }

    private void onSave() {
        // Validação dos campos
        if (ruleNameField.getText().trim().isEmpty() || thresholdValueField.getText().trim().isEmpty() || messageToSendArea.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, preencha todos os campos.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            return;
        }
        double threshold;
        try {
            threshold = Double.parseDouble(thresholdValueField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "O valor limiar deve ser um número válido.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Se estamos a editar, atualizamos o objeto existente. Se não, criamos um novo.
        if (this.alertRule == null) {
            this.alertRule = new AlertRule(
                    ruleNameField.getText().trim(),
                    (String) metricComboBox.getSelectedItem(),
                    (ConditionType) conditionComboBox.getSelectedItem(),
                    threshold,
                    messageToSendArea.getText().trim(),
                    sendToMqttCheckBox.isSelected(),
                    sendToTelegramCheckBox.isSelected()
            );
        } else {
            this.alertRule.setRuleName(ruleNameField.getText().trim());
            this.alertRule.setMetricToWatch((String) metricComboBox.getSelectedItem());
            this.alertRule.setCondition((ConditionType) conditionComboBox.getSelectedItem());
            this.alertRule.setThresholdValue(threshold);
            this.alertRule.setMessageToSend(messageToSendArea.getText().trim());
            this.alertRule.setSendToMqtt(sendToMqttCheckBox.isSelected());
            this.alertRule.setSendToTelegram(sendToTelegramCheckBox.isSelected());
        }

        // Define o valor do cooldown, tanto para regras novas como para editadas.
        this.alertRule.setCooldownSeconds((Integer) cooldownSpinner.getValue());
        dispose(); // Fecha a janela
    }

    // Método para preencher a janela com os dados de uma regra existente (para edição)
    public void setAlertRule(AlertRule rule) {
        this.alertRule = rule;
        ruleNameField.setText(rule.getRuleName());
        metricComboBox.setSelectedItem(rule.getMetricToWatch());
        conditionComboBox.setSelectedItem(rule.getCondition());
        thresholdValueField.setText(String.valueOf(rule.getThresholdValue()));
        messageToSendArea.setText(rule.getMessageToSend());
        sendToMqttCheckBox.setSelected(rule.isSendToMqtt());
        sendToTelegramCheckBox.setSelected(rule.isSendToTelegram());
        cooldownSpinner.setValue(rule.getCooldownSeconds());
    }

    // Metodo para obter a regra salva
    public AlertRule getAlertRule() {
        return alertRule;
    }
}