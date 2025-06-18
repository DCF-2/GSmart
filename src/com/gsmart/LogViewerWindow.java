// Localização: src/main/java/com/gsmart/LogViewerWindow.java
package com.gsmart;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class LogViewerWindow extends JFrame {

    private final JTextPane textPane;

    // Definindo as cores para os logs
    private static final Color COLOR_INFO = new Color(0, 128, 0); // Verde
    private static final Color COLOR_WARN = new Color(255, 165, 0); // Laranja
    private static final Color COLOR_ERROR = Color.RED;
    private static final Color COLOR_DEBUG = Color.BLUE;
    private static final Color COLOR_DEFAULT = Color.BLACK;

    public LogViewerWindow() {
        setTitle("GSmart - Visualizador de Logs");
        setSize(750, 450);
        setLocationByPlatform(true); // Deixa o sistema operacional decidir a posição

        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(textPane);
        add(scrollPane, BorderLayout.CENTER);

        // Não fecha a aplicação, apenas esconde a janela
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }

    private void append(String message, Color color) {
        StyledDocument doc = textPane.getStyledDocument();
        Style style = textPane.addStyle("Color Style", null);
        StyleConstants.setForeground(style, color);
        try {
            doc.insertString(doc.getLength(), message, style);
            // Auto-scroll
            textPane.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void redirectSystemStreams() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                updateTextPane(String.valueOf((char) b));
            }
        };
        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }

    private void updateTextPane(final String text) {
        SwingUtilities.invokeLater(() -> {
            // Lógica para colorir o texto com base em palavras-chave
            if (text.contains("ERROR")) {
                append(text, COLOR_ERROR);
            } else if (text.contains("WARN")) {
                append(text, COLOR_WARN);
            } else if (text.contains("INFO") || text.contains("sucesso")) {
                append(text, COLOR_INFO);
            } else if (text.contains("DEBUG")) {
                append(text, COLOR_DEBUG);
            } else {
                append(text, COLOR_DEFAULT);
            }
        });
    }
}