package main.java.com.gsmart.Gui.windows;

import main.java.com.gsmart.config.AlertRule;
import main.java.com.gsmart.resources.ConditionType;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Uma janela de diálogo (JDialog) para criar ou editar uma {@code AlertRule}.
 * Fornece um formulário para que o utilizador possa configurar todos os
 * parâmetros de uma regra de alerta crítico de forma intuitiva.
 * @see main.java.com.gsmart.config.AlertRule
 */
public class AlertRuleDialog extends JDialog {
    private JTextField ruleNameField;
    private JComboBox<String> metricComboBox;
    private JComboBox<ConditionType> conditionComboBox;
    private JTextField thresholdValueField;
    private JTextArea messageToSendArea;
    private JCheckBox sendToMqttCheckBox;
    private JCheckBox sendToTelegramCheckBox;

    // --- NOVOS CAMPOS PARA A CONDIÇÃO "ENTRE" ---
    private JTextField thresholdMaxValueField;
    private JLabel andLabel; // O texto "e"

    private JButton saveButton;
    private JButton cancelButton;
    private AlertRule alertRule;

    public AlertRuleDialog(Frame owner, String title, List<String> availableMetrics) {
        super(owner, title, true);
        initComponents(availableMetrics);
        setupLayout();

        // --- ADICIONA O LISTENER E CHAMA-O UMA VEZ PARA CONFIGURAR O ESTADO INICIAL ---
        conditionComboBox.addActionListener(e -> toggleBetweenFields());
        toggleBetweenFields();

        cancelButton.addActionListener(e -> dispose());
        saveButton.addActionListener(e -> onSave());

        setSize(500, 400); // Aumentar um pouco a largura
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


        // --- INICIALIZA OS NOVOS COMPONENTES ---
        thresholdMaxValueField = new JTextField(10);
        andLabel = new JLabel(" e ");

        saveButton = new JButton("Salvar");
        cancelButton = new JButton("Cancelar");
    }

    private void setupLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Linha 1: Nome da Regra
        gbc.gridx = 0; gbc.gridy = 0; add(new JLabel("Nome da Regra:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; add(ruleNameField, gbc);

        // Linha 2: Métrica
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; add(new JLabel("Se a métrica:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; add(metricComboBox, gbc);

        // Linha 3: Condição
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; add(new JLabel("Estiver:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; add(conditionComboBox, gbc);

        // Linha 4: Valor Limiar (CORRIGIDO)
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; add(new JLabel("O valor de:"), gbc);
        JPanel valuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        valuePanel.add(thresholdValueField);
        valuePanel.add(andLabel);
        valuePanel.add(thresholdMaxValueField);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; add(valuePanel, gbc);

        // Linha 5: Mensagem
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; add(new JLabel("Enviar alerta:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0; add(new JScrollPane(messageToSendArea), gbc);


        // Destinos
        JPanel destinationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        destinationPanel.add(sendToMqttCheckBox);
        destinationPanel.add(sendToTelegramCheckBox);
        gbc.gridx = 1; gbc.gridy = 5;
        add(destinationPanel, gbc);

        // Botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        gbc.gridx = 1; gbc.gridy = 6;
        add(buttonPanel, gbc);
    }

    /**
     * Mostra ou esconde o segundo campo de valor com base na condição selecionada.
     */
    private void toggleBetweenFields() {
        boolean isBetween = conditionComboBox.getSelectedItem() == ConditionType.BETWEEN;
        andLabel.setVisible(isBetween);
        thresholdMaxValueField.setVisible(isBetween);
    }

    private void onSave() {
        if (ruleNameField.getText().trim().isEmpty() || thresholdValueField.getText().trim().isEmpty() || messageToSendArea.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, preencha todos os campos.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double threshold, thresholdMax = 0;
        try {
            threshold = Double.parseDouble(thresholdValueField.getText().trim());
            if (conditionComboBox.getSelectedItem() == ConditionType.BETWEEN) {
                if (thresholdMaxValueField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Por favor, preencha o segundo valor para a condição 'Entre'.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                thresholdMax = Double.parseDouble(thresholdMaxValueField.getText().trim());
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Os valores de limiar devem ser números válidos.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            return;
        }

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

        this.alertRule.setThresholdValueMax(thresholdMax);
        dispose();
    }

    public void setAlertRule(AlertRule rule) {
        this.alertRule = rule;
        ruleNameField.setText(rule.getRuleName());
        metricComboBox.setSelectedItem(rule.getMetricToWatch());
        conditionComboBox.setSelectedItem(rule.getCondition());
        thresholdValueField.setText(String.valueOf(rule.getThresholdValue()));
        messageToSendArea.setText(rule.getMessageToSend());
        sendToMqttCheckBox.setSelected(rule.isSendToMqtt());
        sendToTelegramCheckBox.setSelected(rule.isSendToTelegram());

        // CORRIGIDO: Define o valor do segundo campo
        thresholdMaxValueField.setText(String.valueOf(rule.getThresholdValueMax()));

        // Garante que o estado da UI está correto ao abrir
        toggleBetweenFields();
    }

    public AlertRule getAlertRule() {
        return alertRule;
    }
}