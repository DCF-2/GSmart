// Localização: src/main/java/com/gsmart/LoginWindow.java
package main.java.com.gsmart.Gui.windows;

import main.java.com.gsmart.GSmartGui;
import main.java.com.gsmart.db.DatabaseManager;
import main.java.com.gsmart.pipeline.PipelineManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Apresenta a janela de login inicial da aplicação GSmart.
 *
 * Esta classe é responsável por obter as credenciais do utilizador e validá-las.
 * Em caso de sucesso na autenticação, ela fecha-se e abre a janela principal
 * da aplicação, a {@code GSmartGui}.
 *
 * Atualmente, a validação é feita com credenciais fixas no código, mas a
 * estrutura permite a sua substituição por um mecanismo de autenticação mais robusto.
 */
public class LoginWindow extends JFrame {

    // --- CREDENCIAIS FIXAS ---
    //private static final String USUARIO_VALIDO = "admin";
    //private static final String SENHA_VALIDA = "admin";

    private final JTextField userField;
    private final JPasswordField passField;
    private final JButton loginButton;

    public LoginWindow() {
        setTitle("GSmart - Autenticação");
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centraliza a janela
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // --- Componentes da UI ---
        JLabel userLabel = new JLabel("Usuário:");
        userField = new JTextField(20);
        JLabel passLabel = new JLabel("Senha:");
        passField = new JPasswordField(20);
        loginButton = new JButton("Entrar");

        // --- Adicionando componentes ao painel ---
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.LINE_END;
        panel.add(userLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.anchor = GridBagConstraints.LINE_START;
        panel.add(userField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.LINE_END;
        panel.add(passLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.anchor = GridBagConstraints.LINE_START;
        panel.add(passField, gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.CENTER;
        panel.add(loginButton, gbc);

        add(panel);

        // --- Ações dos Botões ---
        loginButton.addActionListener(this::performLogin);
        // Permite que o usuário pressione Enter para tentar o login
        getRootPane().setDefaultButton(loginButton);
    }

    /**
     * Valida o login usando o DatabaseManager e abre a aplicação principal se for bem-sucedido.
     */
    private void performLogin(ActionEvent e) {
        String user = userField.getText().trim();
        String password = new String(passField.getPassword());

        if (user.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Utilizador e senha não podem estar vazios.",
                    "Erro de Validação",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // --- 2. USAR O NOSSO NOVO MTODO DE VALIDAÇÃO ---
        String userRole = DatabaseManager.validateLogin(user, password);

        if (userRole != null) {
            // Sucesso! Fecha a janela de login e abre a principal, passando o perfil do utilizador.
            dispose(); // Libera os recursos desta janela
            showMainApplication(userRole);
        } else {
            // Falha
            JOptionPane.showMessageDialog(this,
                    "Utilizador ou senha inválidos.",
                    "Erro de Autenticação",
                    JOptionPane.ERROR_MESSAGE);
            passField.setText(""); // Limpa o campo de senha
        }
    }

    /**
     * Inicializa e exibe a janela principal da aplicação, passando o perfil do utilizador.
     * @param userRole O perfil do utilizador que fez o login (ex: "ADMINISTRATOR").
     */
    private void showMainApplication(String userRole) {
        // Usa o mesmo metodo do Launcher para garantir consistência
        SwingUtilities.invokeLater(() -> {
            LogViewerWindow globalLogViewer = new LogViewerWindow();
            globalLogViewer.redirectSystemStreams();

            PipelineManager pipelineManager = new PipelineManager();

            // --- 3. PASSAR O PERFIL PARA A GSmartGui ---
            GSmartGui gui = new GSmartGui(globalLogViewer, pipelineManager, userRole);
            gui.setVisible(true);
        });
    }
}