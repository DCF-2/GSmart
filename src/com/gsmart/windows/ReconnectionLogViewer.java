// Em GSmart/src/com/gsmart/windows/ReconnectionLogViewer.java
package com.gsmart.windows;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class ReconnectionLogViewer extends JFrame {

    private final JTextArea textArea;

    public ReconnectionLogViewer() {
        setTitle("GSmart - Log de Reconexões");
        setSize(750, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Carrega e exibe o conteúdo do arquivo de log de reconexão.
     */
    public void loadLogFile() {
        try {
            // Lê todas as linhas do arquivo de log e as junta em uma única string.
            String content = Files.lines(Paths.get("reconnection.log"))
                    .collect(Collectors.joining("\n"));
            textArea.setText(content);
            // Move o scroll para o final do texto.
            textArea.setCaretPosition(textArea.getDocument().getLength());
        } catch (IOException e) {
            textArea.setText("Não foi possível carregar o arquivo de log: reconnection.log\n\n" + e.getMessage());
        }
    }
}