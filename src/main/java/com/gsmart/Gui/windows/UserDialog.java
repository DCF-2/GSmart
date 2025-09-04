// Localização: src/com/gsmart/Gui/windows/UserDialog.java
package main.java.com.gsmart.Gui.windows;

import main.java.com.gsmart.db.DatabaseManager;
import main.java.com.gsmart.model.User; // <-- 1. Importar o nosso modelo User

import javax.swing.*;
import java.awt.*;

/**
 * Janela de diálogo para criar um novo utilizador ou editar um existente.
 * <p>
 * Apresenta um formulário com campos para o nome de utilizador, senha e perfil de
 * acesso. Esta janela é utilizada pela {@link UserManagementWindow} para gerir
 * o ciclo de vida dos utilizadores do sistema.
 *
 * @see main.java.com.gsmart.Gui.windows.UserManagementWindow
 * @see main.java.com.gsmart.model.User
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
     * Constrói a janela de diálogo no modo de "Adicionar Novo Utilizador".
     *
     * @param owner A janela pai ({@link UserManagementWindow}) à qual este diálogo está associado.
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
     * Constrói a janela de diálogo no modo de "Editar Utilizador Existente".
     *
     * @param owner A janela pai ({@link UserManagementWindow}) à qual este diálogo está associado.
     * @param userToEdit O objeto {@link User} a ser editado. Os campos do formulário
     * serão pré-preenchidos com os dados deste utilizador.
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
     * Lida com o evento de clique no botão "Salvar", tratando tanto da adição como da edição.
     * <p>
     * Valida os campos de utilizador e senha. Se estiver no modo de adição, chama
     * o método {@code addUser} do {@link DatabaseManager}. Se estiver no modo de edição,
     * chama o método {@code updateUser}. A senha só é atualizada se o campo
     * correspondente for preenchido durante uma edição.
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
            boolean success = DatabaseManager.getInstance().addUser(username, password, role);
            if (success) {
                saved = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Não foi possível adicionar o utilizador.\nO nome de utilizador pode já existir.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } else { // Estamos a EDITAR
            boolean success = DatabaseManager.getInstance().updateUser(userToEdit.id(), username, role);

            // Apenas atualiza a senha se o campo não estiver vazio
            if (!password.isEmpty()) {
                success = success && DatabaseManager.getInstance().updateUserPassword(userToEdit.id(), password);
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