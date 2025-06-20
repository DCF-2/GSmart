// Localização: src/main/java/com/gsmart/GSmartGuiLauncher.java
package com.gsmart;

import javax.swing.SwingUtilities;

public class GSmartGuiLauncher {

    public static void main(String[] args) {
        // O ponto de entrada da aplicação agora simplesmente cria e exibe a janela de login.
        SwingUtilities.invokeLater(() -> {
            LoginWindow loginWindow = new LoginWindow();
            loginWindow.setVisible(true);
        });
    }
}