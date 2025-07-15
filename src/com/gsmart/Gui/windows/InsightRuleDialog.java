package com.gsmart.Gui.windows;

import com.gsmart.config.InsightRule;
import com.gsmart.resources.ConditionType;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Uma janela de diálogo (JDialog) para criar ou editar uma {@code InsightRule}.
 *
 * Fornece um formulário para que o utilizador possa configurar todos os
 * parâmetros de uma regra de alarme de forma intuitiva.
 *
 * @see com.gsmart.config.InsightRule
 */
public class InsightRuleDialog extends JDialog {
    private JTextField ruleNameField;
    private JComboBox<String> metricComboBox;
    private JComboBox<ConditionType> conditionComboBox;
    private JTextField thresholdValueField;
    private JTextArea messageToSendArea;
    private JTextField insightTypeField; // Novo campo para o tipo de insight
    private JButton saveButton;
    private JButton cancelButton;

    private InsightRule insightRule;

    public InsightRuleDialog(Frame owner, String title, List<String> availableMetrics) {
        super(owner, title, true);
        initComponents(availableMetrics);
        setupLayout();

        cancelButton.addActionListener(e -> dispose());
        saveButton.addActionListener(e -> onSave());

        setSize(450, 400); // Aumentar um pouco a altura
        setLocationRelativeTo(owner);
    }

    private void initComponents(List<String> availableMetrics) {
        ruleNameField = new JTextField(20);
        metricComboBox = new JComboBox<>(availableMetrics.toArray(new String[0]));
        conditionComboBox = new JComboBox<>(ConditionType.values());
        thresholdValueField = new JTextField(10);
        insightTypeField = new JTextField("INFO", 15); // Valor padrão "INFO"
        messageToSendArea = new JTextArea(5, 20);
        messageToSendArea.setLineWrap(true);
        messageToSendArea.setWrapStyleWord(true);
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
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; add(new JLabel("For:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; add(conditionComboBox, gbc);

        // Linha 4: Valor Limiar
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; add(new JLabel("O valor de:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; add(thresholdValueField, gbc);

        // Linha 5: Tipo de Insight
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; add(new JLabel("Tipo de Insight:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; add(insightTypeField, gbc);

        // Linha 6: Mensagem
        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; add(new JLabel("Gerar insight:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0; add(new JScrollPane(messageToSendArea), gbc);

        // Linha 7: Botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        gbc.gridx = 1; gbc.gridy = 6; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weighty = 0; add(buttonPanel, gbc);
    }

    private void onSave() {
        if (ruleNameField.getText().trim().isEmpty() || thresholdValueField.getText().trim().isEmpty() || messageToSendArea.getText().trim().isEmpty() || insightTypeField.getText().trim().isEmpty()) {
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

        if (this.insightRule == null) {
            this.insightRule = new InsightRule(
                    ruleNameField.getText().trim(),
                    (String) metricComboBox.getSelectedItem(),
                    (ConditionType) conditionComboBox.getSelectedItem(),
                    threshold,
                    messageToSendArea.getText().trim(),
                    insightTypeField.getText().trim().toUpperCase()
            );
        } else {
            this.insightRule.setRuleName(ruleNameField.getText().trim());
            this.insightRule.setMetricToWatch((String) metricComboBox.getSelectedItem());
            this.insightRule.setCondition((ConditionType) conditionComboBox.getSelectedItem());
            this.insightRule.setThresholdValue(threshold);
            this.insightRule.setMessageToSend(messageToSendArea.getText().trim());
            this.insightRule.setInsightType(insightTypeField.getText().trim().toUpperCase());
        }
        dispose();
    }

    public void setInsightRule(InsightRule rule) {
        this.insightRule = rule;
        ruleNameField.setText(rule.getRuleName());
        metricComboBox.setSelectedItem(rule.getMetricToWatch());
        conditionComboBox.setSelectedItem(rule.getCondition());
        thresholdValueField.setText(String.valueOf(rule.getThresholdValue()));
        messageToSendArea.setText(rule.getMessageToSend());
        insightTypeField.setText(rule.getInsightType());
    }

    public InsightRule getInsightRule() {
        return insightRule;
    }
}