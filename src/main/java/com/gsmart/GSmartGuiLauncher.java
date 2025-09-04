// Localização: src/main/java/com/gsmart/GSmartGuiLauncher.java
package main.java.com.gsmart;

import main.java.com.gsmart.Gui.windows.LoginWindow;
import main.java.com.gsmart.db.DatabaseManager;
import main.java.com.gsmart.ui.ThemeManager;
import javax.swing.SwingUtilities;

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
        DatabaseManager.getInstance();
        ThemeManager.applySavedTheme();
        SwingUtilities.invokeLater(() -> {
            LoginWindow loginWindow = new LoginWindow();
            loginWindow.setVisible(true);
        });
    }
}