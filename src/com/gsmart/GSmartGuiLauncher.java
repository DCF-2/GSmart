// Localização: src/main/java/com/gsmart/GSmartGuiLauncher.java
package com.gsmart;

import javax.swing.SwingUtilities;

public class GSmartGuiLauncher {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 1. Cria a janela de logs central UMA VEZ.
            LogViewerWindow globalLogViewer = new LogViewerWindow();

            // 2. Redireciona os logs da aplicação para esta janela.
            globalLogViewer.redirectSystemStreams();

            // 3. Passa a janela de logs para a GUI principal.
            GSmartGui gui = new GSmartGui(globalLogViewer);
            gui.setVisible(true);
        });
    }
}