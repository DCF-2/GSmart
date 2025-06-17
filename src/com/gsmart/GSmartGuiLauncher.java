// Localização: src/main/java/com/gsmart/GSmartGuiLauncher.java
package com.gsmart;

import javax.swing.SwingUtilities;

public class GSmartGuiLauncher {

    public static void main(String[] args) {
        // Garante que a interface gráfica seja criada na thread correta
        SwingUtilities.invokeLater(() -> {
            GSmartGui gui = new GSmartGui();
            gui.setVisible(true);
        });
    }
}