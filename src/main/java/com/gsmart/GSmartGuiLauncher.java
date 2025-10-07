// Localização: src/main/java/com/gsmart/GSmartGuiLauncher.java
package main.java.com.gsmart;

import main.java.com.gsmart.Gui.windows.LoginWindow;
import main.java.com.gsmart.db.DatabaseManager;
import main.java.com.gsmart.ui.ThemeManager;

import javax.swing.*;

/**
 * Ponto de entrada (entry point) principal para a aplicação GSmart.
 *
 * A única responsabilidade desta classe é conter o metodo {@code main}, que
 * inicializa e exibe a primeira janela da interface gráfica, a {@code LoginWindow},
 * dando início à execução do programa na thread de eventos do Swing (EDT)
 * para garantir a segurança da thread.
 */
public class GSmartGuiLauncher {

    public static void main(String[] args) {
        // O ponto de entrada da aplicação agora simplesmente cria e exibe a janela de login.
        try {
            // Força o carregamento do driver do PostgreSQL na memória.
            // Isto garante que ele se registe no DriverManager.
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            // Este erro só acontece se o .jar do driver não estiver incluído no build final.
            JOptionPane.showMessageDialog(
                    null,
                    "Erro Crítico: O driver do PostgreSQL não foi encontrado.\n" +
                            "Verifique se a dependência está corretamente configurada no seu pom.xml.",
                    "Falha de Driver",
                    JOptionPane.ERROR_MESSAGE
            );
            System.exit(1); // Encerra a aplicação se o driver não existir.
        }

        DatabaseManager.getInstance();
        ThemeManager.applySavedTheme();
        SwingUtilities.invokeLater(() -> {
            LoginWindow loginWindow = new LoginWindow();
            loginWindow.setVisible(true);
        });
    }
}