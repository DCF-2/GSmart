// Localização: src/com/gsmart/Gui/windows/UserDialog.java
package com.gsmart.Gui.windows;

import com.gsmart.db.DatabaseManager;
import com.gsmart.model.User; // <-- 1. Importar o nosso modelo User

import javax.swing.*;
import java.awt.*;

/**
 * Janela de diálogo para criar ou editar um utilizador.
 */
public class UserDialog extends JDialog {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private JButton saveButton;
    private JButton cancelButton;

    private boolean saved = false;
    private User userToEdit; // <-- 2. Variável para guardar o utilizador que estamos a editar

    /**
     * Construtor para ADICIONAR um novo utilizador.
     */
    public UserDialog(Dialog owner) {
        super(owner, "Adicionar Novo Utilizador", true);
        this.userToEdit = null; // Garante que estamos no modo de adição
        initComponents();
        setupLayout();
        setupListeners();
        setSize(400, 220); // Ajustar tamanho
        setLocationRelativeTo(owner);
    }

    /**
     * Construtor para EDITAR um utilizador existente.
     */
    public UserDialog(Dialog owner, User userToEdit) {
        super(owner, "Editar Utilizador", true);
        this.userToEdit = userToEdit; // Guarda o utilizador para edição
        initComponents();
        setupLayout();
        setupListeners();
        populateFields(); // <-- 3. Novo método para preencher os campos com os dados do utilizador
        setSize(400, 220); // Ajustar tamanho
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        String[] roles = {"ADMINISTRATOR", "OPERATOR"};
        roleComboBox = new JComboBox<>(roles);
        saveButton = new JButton("Salvar");
        cancelButton = new JButton("Cancelar");
    }

    private void setupLayout() {
        // ... (o método setupLayout permanece exatamente o mesmo)
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0; gbc.gridy = 0; add(new JLabel("Nome de Utilizador:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; add(usernameField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; add(new JLabel("Senha:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL; add(passwordField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; add(new JLabel("Perfil:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.fill = GridBagConstraints.HORIZONTAL; add(roleComboBox, gbc);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        gbc.gridx = 1; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST; add(buttonPanel, gbc);
    }

    /**
     * Configura os listeners dos botões.
     */
    private void setupListeners() {
        saveButton.addActionListener(e -> onSave());
        cancelButton.addActionListener(e -> dispose());
    }

    /**
     * Preenche os campos do formulário com os dados de um utilizador existente.
     */
    private void populateFields() {
        if (userToEdit != null) {
            usernameField.setText(userToEdit.username());
            roleComboBox.setSelectedItem(userToEdit.role());
            passwordField.setToolTipText("Deixe em branco para não alterar a senha");

            // Medida de segurança: não se pode editar o nome do utilizador 'admin'
            if ("admin".equalsIgnoreCase(userToEdit.username())) {
                usernameField.setEnabled(false);
            }
        }
    }

    /**
     * Lida com o evento de clique no botão Salvar.
     */
    private void onSave() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role = (String) roleComboBox.getSelectedItem();

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nome de utilizador não pode estar vazio.", "Erro de Validação", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // --- 4. Lógica adaptada para Adicionar OU Editar ---
        if (userToEdit == null) { // Estamos a ADICIONAR
            if (password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "A senha não pode estar vazia ao criar um novo utilizador.", "Erro de Validação", JOptionPane.WARNING_MESSAGE);
                return;
            }
            boolean success = DatabaseManager.addUser(username, password, role);
            if (success) {
                saved = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Não foi possível adicionar o utilizador.\nO nome de utilizador pode já existir.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } else { // Estamos a EDITAR
            boolean success = DatabaseManager.updateUser(userToEdit.id(), username, role);

            // Apenas atualiza a senha se o campo não estiver vazio
            if (!password.isEmpty()) {
                success = success && DatabaseManager.updateUserPassword(userToEdit.id(), password); // <-- Corrigido para userToEdit
            }

            if (success) {
                saved = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Não foi possível atualizar o utilizador.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public boolean isSaved() {
        return saved;
    }
}