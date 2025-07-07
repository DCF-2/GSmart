// Localização: src/main/java/com/gsmart/LogViewerWindow.java
package com.gsmart.windows;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets; // --- IMPORT ADICIONADO ---

/**
 * Uma janela de UI que exibe os logs gerais da aplicação em tempo real.
 *
 * Esta classe é responsável por ler o ficheiro de log principal (ex: gsmart_general.log)
 * e apresentar o seu conteúdo numa área de texto. Ela pode ser configurada para
 * atualizar automaticamente, fornecendo um feedback contínuo sobre o que a
 * aplicação está a fazer em segundo plano.
 */
public class LogViewerWindow extends JFrame {

    private final JTextPane textPane;

    private static final Color COLOR_INFO = new Color(0, 128, 0); // Verde
    private static final Color COLOR_WARN = new Color(255, 165, 0); // Laranja
    private static final Color COLOR_ERROR = Color.RED;
    private static final Color COLOR_DEBUG = Color.BLUE;
    private static final Color COLOR_DEFAULT = Color.BLACK;

    public LogViewerWindow() {
        setTitle("GSmart - Visualizador de Logs");
        setSize(750, 450);
        setLocationByPlatform(true);

        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(textPane);
        add(scrollPane, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }

    private void append(String message, Color color) {
        StyledDocument doc = textPane.getStyledDocument();
        Style style = textPane.addStyle("Color Style", null);
        StyleConstants.setForeground(style, color);
        try {
            doc.insertString(doc.getLength(), message, style);
            textPane.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            // Em vez de imprimir no stack trace (que seria redirecionado de volta para cá),
            // podemos lidar com isso de forma mais segura, talvez logando para um arquivo futuramente.
            // Por enquanto, vamos evitar a recursão infinita.
        }
    }

    public void redirectSystemStreams() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                // A decodificação de bytes para char é complexa.
                // A maneira como o PrintStream lida com isso é mais robusta.
                // Apenas encaminhamos o byte bruto. O PrintStream fará o resto.
                // Esta abordagem simples pode não lidar com todos os caracteres multibyte
                // perfeitamente, mas para o Logback que envia strings completas,
                // a especificação do charset no PrintStream é o mais importante.
                updateTextPane(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                // O Logback geralmente chama este metodo, enviando uma string completa.
                // Aqui garantimos a decodificação correta.
                updateTextPane(new String(b, off, len, StandardCharsets.UTF_8));
            }
        };

        // --- MUDANÇA PRINCIPAL AQUI ---
        // Especificamos o encoding UTF-8 ao criar o PrintStream.
        System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(out, true, StandardCharsets.UTF_8));
    }

    private void updateTextPane(final String text) {
        SwingUtilities.invokeLater(() -> {
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