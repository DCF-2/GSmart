package main.java.com.gsmart.Gui.windows;

import main.java.com.gsmart.config.InsightRule;
import main.java.com.gsmart.resources.ConditionType;
import main.java.com.gsmart.utils.RuleTester;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Uma janela de diálogo (JDialog) para criar ou editar uma {@link main.java.com.gsmart.config.InsightRule}.
 * <p>
 * Fornece um formulário para que o utilizador possa configurar todos os
 * parâmetros de uma regra de alarme (insight) de forma intuitiva, permitindo
 * a criação de observações proativas sobre o estado do sistema.
 *
 * @see main.java.com.gsmart.config.InsightRule
 */
public class InsightRuleDialog extends JDialog {
    private JTextField ruleNameField;
    private JComboBox<String> metricComboBox;
    private JComboBox<ConditionType> conditionComboBox;
    private JTextField thresholdValueField;
    private JTextArea messageToSendArea;
    private JTextField insightTypeField;
    private JCheckBox sendToTelegramCheckBox;
    private JCheckBox sendToMqttCheckBox;
    private JTextField categoryField;



    // --- CAMPOS PARA A CONDIÇÃO "ENTRE" ---
    private JTextField thresholdMaxValueField;
    private JLabel andLabel;

    private JButton saveButton;
    private JButton cancelButton;
    private JButton testButton;
    private InsightRule insightRule;

    /**
     * Constrói a janela de diálogo para criar ou editar uma regra de alarme.
     *
     * @param owner A janela pai à qual este diálogo está associado.
     * @param title O título a ser exibido na barra da janela.
     * @param availableMetrics A lista de métricas disponíveis que o utilizador pode selecionar.
     */
    public InsightRuleDialog(Frame owner, String title, List<String> availableMetrics) {
        super(owner, title, true);
        initComponents(availableMetrics);
        setupLayout();

        // --- ADICIONA O LISTENER E CHAMA-O UMA VEZ PARA CONFIGURAR O ESTADO INICIAL ---
        conditionComboBox.addActionListener(e -> toggleBetweenFields());
        toggleBetweenFields();


        cancelButton.addActionListener(e -> dispose());
        saveButton.addActionListener(e -> onSave());
        testButton.addActionListener(e -> onTest());

        setSize(500, 450); // Aumentar a largura e altura
        setLocationRelativeTo(owner);
    }

    private void initComponents(List<String> availableMetrics) {
        ruleNameField = new JTextField(20);
        metricComboBox = new JComboBox<>(availableMetrics.toArray(new String[0]));
        conditionComboBox = new JComboBox<>(ConditionType.values());
        thresholdValueField = new JTextField(10);
        insightTypeField = new JTextField("INFO", 15);
        messageToSendArea = new JTextArea(5, 20);
        messageToSendArea.setLineWrap(true);
        messageToSendArea.setWrapStyleWord(true);
        sendToMqttCheckBox = new JCheckBox("Enviar via MQTT", true);
        sendToTelegramCheckBox = new JCheckBox("Enviar via Telegram", true);
        categoryField = new JTextField("Geral");

        thresholdMaxValueField = new JTextField(10);
        andLabel = new JLabel(" e ");

        saveButton = new JButton("Salvar");
        cancelButton = new JButton("Cancelar");
        testButton = new JButton("Testar Regra");
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

        //linha 4: Categoria
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; add(new JLabel("Categoria:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; add(categoryField, gbc);

        // Linha 5: Tipo de Insight
        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; add(new JLabel("Tipo de Alarme:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; add(insightTypeField, gbc);

        //Linha 6: Destinos
        JPanel destinationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        destinationPanel.add(sendToMqttCheckBox);
        destinationPanel.add(sendToTelegramCheckBox);
        gbc.gridx = 1; gbc.gridy = 6;
        add(destinationPanel, gbc);

        // Linha 7: Mensagem
        gbc.gridx = 0; gbc.gridy = 7; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; add(new JLabel("Gerar alarme:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0; add(new JScrollPane(messageToSendArea), gbc);

        // Linha 8: Botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(testButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        gbc.gridx = 1; gbc.gridy = 8;
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

    /**
     * Lida com o evento de clique no botão "Salvar".
     * <p>
     * Valida os campos do formulário e, se os dados forem válidos, cria um novo
     * objeto {@link InsightRule} (no modo de adição) ou atualiza o existente
     * (no modo de edição). Em seguida, fecha a janela de diálogo.
     */
    private void onSave() {
        // Validação dos campos principais (continua igual)
        if (ruleNameField.getText().trim().isEmpty() || thresholdValueField.getText().trim().isEmpty() || messageToSendArea.getText().trim().isEmpty() || insightTypeField.getText().trim().isEmpty()) {
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

        // Cria ou atualiza a regra
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

        // Define os valores comuns em ambos os casos
        this.insightRule.setCategory(categoryField.getText().trim());
        this.insightRule.setThresholdValueMax(thresholdMax);
        this.insightRule.setSendToMqtt(sendToMqttCheckBox.isSelected());
        this.insightRule.setSendToTelegram(sendToTelegramCheckBox.isSelected());

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
        sendToMqttCheckBox.setSelected(rule.isSendToMqtt());
        sendToTelegramCheckBox.setSelected(rule.isSendToTelegram());
        categoryField.setText(rule.getCategory());

        thresholdMaxValueField.setText(String.valueOf(rule.getThresholdValueMax()));

        // Garante que o estado da UI está correto ao abrir
        toggleBetweenFields();
    }

    public InsightRule getInsightRule() {
        return insightRule;
    }

    /**
     * Lida com o evento de clique no botão "Testar Regra".
     * <p>
     * Pede ao utilizador um valor numérico de teste e utiliza a classe
     * {@link main.java.com.gsmart.utils.RuleTester} para simular se a regra de alarme,
     * com os parâmetros atualmente preenchidos, seria despoletada. O resultado da
     * simulação é exibido numa mensagem ao utilizador.
     */
    private void onTest() {
        // 1. Tenta obter os valores dos campos da regra
        double threshold;
        double thresholdMax = 0;
        ConditionType condition = (ConditionType) conditionComboBox.getSelectedItem();

        try {
            threshold = Double.parseDouble(thresholdValueField.getText().trim());
            if (condition == ConditionType.BETWEEN) {
                if (thresholdMaxValueField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Para testar a condição 'Entre', preencha ambos os valores de limiar.", "Erro de Validação", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                thresholdMax = Double.parseDouble(thresholdMaxValueField.getText().trim());
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Os valores de limiar devem ser números válidos para o teste.", "Erro de Validação", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. Pede ao utilizador um valor de teste
        String testValueStr = JOptionPane.showInputDialog(this, "Insira um valor numérico para testar a regra:", "Testar Regra", JOptionPane.QUESTION_MESSAGE);
        if (testValueStr == null || testValueStr.trim().isEmpty()) {
            return; // O utilizador cancelou
        }

        // 3. Avalia a regra
        try {
            double testValue = Double.parseDouble(testValueStr.trim());

            // Cria uma regra temporária apenas para o teste
            InsightRule tempRule = new InsightRule("Teste", "", condition, threshold, "", "");
            tempRule.setThresholdValueMax(thresholdMax);

            boolean isTriggered = RuleTester.evaluate(tempRule, testValue);

            // 4. Mostra o resultado
            String resultMessage = isTriggered
                    ? "A regra SERIA despoletada com o valor " + testValue + "."
                    : "A regra NÃO seria despoletada com o valor " + testValue + ".";

            JOptionPane.showMessageDialog(this, resultMessage, "Resultado do Teste", JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "O valor inserido para o teste não é um número válido.", "Erro de Teste", JOptionPane.ERROR_MESSAGE);
        }
    }
}