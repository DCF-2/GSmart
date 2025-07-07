// Localização: src/main/java/com/gsmart/GSmartGuiLauncher.java
package com.gsmart;

import com.gsmart.windows.LoginWindow;

import javax.swing.SwingUtilities;

/**
 * Ponto de entrada (entry point) principal para a aplicação GSmart.
 *
 * A única responsabilidade desta classe é conter o método {@code main}, que
 * inicializa e exibe a primeira janela da interface gráfica, a {@code LoginWindow},
 * dando início à execução do programa na thread de eventos do Swing (EDT)
 * para garantir a segurança da thread.
 */
public class GSmartGuiLauncher {

    public static void main(String[] args) {
        // O ponto de entrada da aplicação agora simplesmente cria e exibe a janela de login.
        SwingUtilities.invokeLater(() -> {
            LoginWindow loginWindow = new LoginWindow();
            loginWindow.setVisible(true);
        });
    }
}