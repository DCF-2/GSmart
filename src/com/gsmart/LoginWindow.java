// Localização: src/main/java/com/gsmart/LoginWindow.java
package com.gsmart;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginWindow extends JFrame {

    // --- CREDENCIAIS FIXAS ---
    // Em uma aplicação real, isso viria de um banco de dados ou serviço de autenticação.
    private static final String USUARIO_VALIDO = "gsmart_admin";
    private static final String SENHA_VALIDA = "GSmart@2024#";

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

    private void performLogin(ActionEvent e) {
        String user = userField.getText();
        String password = new String(passField.getPassword());

        if (USUARIO_VALIDO.equals(user) && SENHA_VALIDA.equals(password)) {
            // Sucesso! Fecha a janela de login e abre a principal.
            dispose(); // Libera os recursos desta janela
            showMainApplication();
        } else {
            // Falha
            JOptionPane.showMessageDialog(this,
                    "Usuário ou senha inválidos.",
                    "Erro de Autenticação",
                    JOptionPane.ERROR_MESSAGE);
            passField.setText(""); // Limpa o campo de senha
        }
    }

    private void showMainApplication() {
        // Usa o mesmo método do Launcher para garantir consistência
        SwingUtilities.invokeLater(() -> {
            LogViewerWindow globalLogViewer = new LogViewerWindow();
            globalLogViewer.redirectSystemStreams();

            PipelineManager pipelineManager = new PipelineManager();

            GSmartGui gui = new GSmartGui(globalLogViewer, pipelineManager);
            gui.setVisible(true);
        });
    }
}