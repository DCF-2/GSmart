package main.java.com.gsmart.Gui.windows;

import main.java.com.gsmart.config.AlertRule;
import main.java.com.gsmart.resources.ConditionType;
import main.java.com.gsmart.utils.RuleTester;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Uma janela de diálogo (JDialog) para criar ou editar uma {@link main.java.com.gsmart.config.AlertRule}.
 * <p>
 * Fornece um formulário para que o utilizador possa configurar todos os
 * parâmetros de uma regra de alerta crítico de forma intuitiva, incluindo o nome,
 * a métrica a ser monitorizada, a condição, o valor limiar e a mensagem de notificação.
 *
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
    private JTextField categoryField;

    // --- CAMPOS PARA A CONDIÇÃO "ENTRE" ---
    private JTextField thresholdMaxValueField;
    private JLabel andLabel; // O texto "e"

    private JButton saveButton;
    private JButton testButton;
    private JButton cancelButton;
    private AlertRule alertRule;

    /**
     * Constrói a janela de diálogo para criar ou editar uma regra de alerta.
     *
     * @param owner A janela pai (geralmente a janela principal da aplicação).
     * @param title O título a ser exibido na barra da janela.
     * @param availableMetrics A lista de métricas disponíveis para o utilizador selecionar.
     */
    public AlertRuleDialog(Frame owner, String title, List<String> availableMetrics) {
        super(owner, title, true);
        initComponents(availableMetrics);
        setupLayout();

        // --- ADICIONA O LISTENER E CHAMA-O UMA VEZ PARA CONFIGURAR O ESTADO INICIAL ---
        conditionComboBox.addActionListener(e -> toggleBetweenFields());
        toggleBetweenFields();

        cancelButton.addActionListener(e -> dispose());
        saveButton.addActionListener(e -> onSave());
        testButton.addActionListener(e -> onTest());

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

        // Linha 4: Valor Limiar
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; add(new JLabel("O valor de:"), gbc);
        JPanel valuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        valuePanel.add(thresholdValueField);
        valuePanel.add(andLabel);
        valuePanel.add(thresholdMaxValueField);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; add(valuePanel, gbc);

        //linha 4: Categoria
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; add(new JLabel("Categoria:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; add(categoryField, gbc);

        // Linha 5: Mensagem
        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; add(new JLabel("Enviar alerta:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0; add(new JScrollPane(messageToSendArea), gbc);


        // Linha 6: Destinos
        JPanel destinationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        destinationPanel.add(sendToMqttCheckBox);
        destinationPanel.add(sendToTelegramCheckBox);
        gbc.gridx = 1; gbc.gridy = 6;
        add(destinationPanel, gbc);

        // Linha 7: Botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(testButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        gbc.gridx = 1; gbc.gridy = 7;
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
     * Realiza a validação dos campos do formulário para garantir que todos os dados
     * necessários foram preenchidos corretamente. Se a validação for bem-sucedida,
     * cria um novo objeto {@link AlertRule} (no modo de adição) ou atualiza o
     * existente (no modo de edição) e, em seguida, fecha a janela de diálogo.
     */
    private void onSave() {
        // Validação dos campos principais
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

        // 1. Cria uma nova regra se estivermos no modo "Adicionar"
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
        }
        // 2. Atualiza a regra existente se estivermos no modo "Editar"
        else {
            this.alertRule.setRuleName(ruleNameField.getText().trim());
            this.alertRule.setMetricToWatch((String) metricComboBox.getSelectedItem());
            this.alertRule.setCondition((ConditionType) conditionComboBox.getSelectedItem());
            this.alertRule.setThresholdValue(threshold);
            this.alertRule.setMessageToSend(messageToSendArea.getText().trim());
            this.alertRule.setSendToMqtt(sendToMqttCheckBox.isSelected());
            this.alertRule.setSendToTelegram(sendToTelegramCheckBox.isSelected());
        }

        // 3. Define os valores comuns (categoria e valor máximo) em ambos os casos
        this.alertRule.setCategory(categoryField.getText().trim());
        this.alertRule.setThresholdValueMax(thresholdMax);

        // 4. Fecha a janela
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
        categoryField.setText(rule.getCategory());
        thresholdMaxValueField.setText(String.valueOf(rule.getThresholdValueMax()));

        toggleBetweenFields();
    }

    public AlertRule getAlertRule() {
        return alertRule;
    }

    /**
     * Lida com o evento de clique no botão "Testar Regra".
     * <p>
     * Pede ao utilizador um valor numérico de teste e utiliza a classe
     * {@link main.java.com.gsmart.utils.RuleTester} para simular se a regra,
     * com os parâmetros atualmente preenchidos no formulário, seria despoletada
     * com esse valor. O resultado é exibido numa mensagem informativa.
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
            AlertRule tempRule = new AlertRule("Teste", "", condition, threshold, "", false, false);
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