// Localização: src/main/java/com/gsmart/Gui/windows/LoginWindow.java
package main.java.com.gsmart.Gui.windows;

import main.java.com.gsmart.GSmartGui;
import main.java.com.gsmart.db.DatabaseManager;
import main.java.com.gsmart.pipeline.PipelineManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URL;

/**
 * Apresenta a janela de login inicial da aplicação GSmart.
 * <p>
 * Esta é a primeira janela que o utilizador vê e é responsável por autenticá-lo
 * contra a base de dados local. Em caso de sucesso, esta janela é fechada e a
 * janela principal da aplicação ({@link GSmartGui}) é aberta, passando o perfil
 * de acesso do utilizador.
 */
public class LoginWindow extends JFrame {

    private final JTextField userField;
    private final JPasswordField passField;
    private final JButton loginButton;

    /**
     * Constrói e inicializa a janela de login, montando todos os seus componentes visuais.
     * <p>
     * O design é dividido em dois painéis: um painel esquerdo para a marca (logo) e um
     * painel direito que contém o formulário de autenticação.
     */
    public LoginWindow() {
        setTitle("GSmart - Autenticação");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setMinimumSize(new Dimension(800, 500)); // Define um tamanho fixo para a janela

        // --- PAINEL PRINCIPAL COM DIVISÃO ---
        JPanel mainPanel = new JPanel(new GridLayout(1, 2));

        // --- PAINEL ESQUERDO (AZUL COM LOGO) ---
        // ALTERAÇÕES AQUI: GridLayout para centralizar e cor azul mais saturada
        JPanel leftPanel = new JPanel(new GridBagLayout()); // Usa GridBagLayout para centralizar
        leftPanel.setBackground(new Color(40, 70, 160)); // Tom de azul mais saturado

        try {
            URL iconUrl = getClass().getResource("/gsmart_icon.png");
            if (iconUrl != null) {
                ImageIcon originalIcon = new ImageIcon(iconUrl);
                // Redimensiona o ícone para ser muito maior no painel esquerdo
                // Ajusta o tamanho para ocupar boa parte do painel
                Image scaledImage = originalIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                JLabel logoLabel = new JLabel(new ImageIcon(scaledImage));

                // Centraliza o logo no GridBagLayout
                GridBagConstraints gbcLeft = new GridBagConstraints();
                gbcLeft.gridx = 0;
                gbcLeft.gridy = 0;
                gbcLeft.weightx = 1.0;
                gbcLeft.weighty = 1.0;
                gbcLeft.anchor = GridBagConstraints.CENTER;
                leftPanel.add(logoLabel, gbcLeft);
            }
            // Adiciona o ícone à barra de título da janela também (como antes)
            Image windowIcon = ImageIO.read(iconUrl);
            setIconImage(windowIcon);
        } catch (Exception e) {
            System.err.println("Erro ao carregar o ícone da aplicação: " + e.getMessage());
        }
        mainPanel.add(leftPanel);

        // --- PAINEL DIREITO (FORMULÁRIO DE LOGIN) ---
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new EmptyBorder(40, 40, 40, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Título
        JLabel titleLabel = new JLabel("Bem-vindo ao GSmart");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        gbc.gridy = 0;
        rightPanel.add(titleLabel, gbc);

        // Subtítulo
        JLabel subtitleLabel = new JLabel("Insira as suas credenciais para continuar.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.GRAY);
        gbc.gridy = 1;
        rightPanel.add(subtitleLabel, gbc);

        // Espaçador
        gbc.gridy = 2;
        rightPanel.add(Box.createVerticalStrut(20), gbc);

        // Campo Utilizador
        JLabel userLabel = new JLabel("Utilizador");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridy = 3;
        rightPanel.add(userLabel, gbc);

        userField = new JTextField(25);
        userField.putClientProperty("JComponent.roundRect", true); // Borda arredondada (FlatLaf)
        userField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridy = 4;
        rightPanel.add(userField, gbc);

        // Campo Senha
        JLabel passLabel = new JLabel("Senha");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridy = 5;
        rightPanel.add(passLabel, gbc);

        passField = new JPasswordField(25);
        passField.putClientProperty("JComponent.roundRect", true); // Borda arredondada (FlatLaf)
        passField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridy = 6;
        rightPanel.add(passField, gbc);

        // Botão de Login
        loginButton = new JButton("Entrar");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.putClientProperty("JButton.buttonType", "roundRect"); // Borda arredondada (FlatLaf)
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridy = 7;
        gbc.insets = new Insets(20, 0, 10, 0);
        rightPanel.add(loginButton, gbc);

        mainPanel.add(rightPanel);

        // Adiciona o painel principal à janela e finaliza
        add(mainPanel);
        setLocationRelativeTo(null); // Centraliza a janela
        pack(); // Ajusta o tamanho da janela aos componentes

        // Ações
        loginButton.addActionListener(this::performLogin);
        getRootPane().setDefaultButton(loginButton);
    }

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

        String userRole = DatabaseManager.getInstance().validateLogin(user, password);

        if (userRole != null) {
            dispose();
            showMainApplication(userRole);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Utilizador ou senha inválidos.",
                    "Erro de Autenticação",
                    JOptionPane.ERROR_MESSAGE);
            passField.setText("");
        }
    }

    private void showMainApplication(String userRole) {
        SwingUtilities.invokeLater(() -> {
            try {
                LogViewerWindow globalLogViewer = new LogViewerWindow();
                globalLogViewer.redirectSystemStreams();
                PipelineManager pipelineManager = new PipelineManager();
                GSmartGui gui = new GSmartGui(globalLogViewer, pipelineManager, userRole);
                gui.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(
                        null,
                        "Ocorreu um erro crítico ao iniciar a aplicação.\nErro: " + e.getMessage(),
                        "Falha na Inicialização",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}