// Localização: src/main/java/com/gsmart/LogViewerWindow.java
package main.java.com.gsmart.Gui.windows;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import main.java.com.gsmart.Gui.windows.LogViewerAppender;

/**
 * Uma janela de UI que exibe os logs gerais da aplicação em tempo real.
 * <p>
 * Esta classe redireciona as saídas padrão (System.out) e de erro (System.err)
 * para um {@link JTextPane}, permitindo que todos os logs gerados pela aplicação
 * sejam visualizados numa única interface. As mensagens são coloridas com base
 * no nível e no conteúdo do log para facilitar a leitura.
 */
public class LogViewerWindow extends JFrame {

    private final JTextPane textPane;

    // --- PALETA DE CORES PARA LOGS ---
    private static final Color COLOR_SUCCESS = new Color(0, 150, 0);       // Verde Escuro
    private static final Color COLOR_INFO = new Color(60, 150, 220);       // Azul
    private static final Color COLOR_WARN = new Color(255, 165, 0);        // Laranja
    private static final Color COLOR_ERROR = new Color(220, 20, 60);         // Vermelho
    private static final Color COLOR_DEBUG = new Color(150, 150, 150);     // Cinzento
    private static final Color COLOR_LIFECYCLE = new Color(138, 43, 226);  // Roxo
    private static final Color COLOR_DEFAULT = Color.BLACK;

    public LogViewerWindow() {
        setTitle("GSmart - Visualizador de Logs");
        setSize(850, 500);
        setLocationByPlatform(true);

        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setFont(new Font("Monospaced", Font.PLAIN, 12));

        // Adiciona estilos ao JTextPane
        addStylesToDocument(textPane);

        JScrollPane scrollPane = new JScrollPane(textPane);
        add(scrollPane, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        LogViewerAppender.setLogViewer(this);
    }

    /**
     * Adiciona os estilos de cor e formatação ao documento do JTextPane.
     */
    private void addStylesToDocument(JTextPane textPane) {
        StyledDocument doc = textPane.getStyledDocument();
        // Estilo Padrão
        Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setFontFamily(def, "Monospaced");
        StyleConstants.setFontSize(def, 12);
        // Estilos de Log
        createStyle("SUCCESS", doc, COLOR_SUCCESS, true);
        createStyle("INFO", doc, COLOR_INFO, false);
        createStyle("WARN", doc, COLOR_WARN, false);
        createStyle("ERROR", doc, COLOR_ERROR, true);
        createStyle("DEBUG", doc, COLOR_DEBUG, false);
        createStyle("LIFECYCLE", doc, COLOR_LIFECYCLE, true);
        createStyle("DEFAULT", doc, COLOR_DEFAULT, false);
    }

    /**
     * Método auxiliar para criar um novo estilo.
     */
    private void createStyle(String name, StyledDocument doc, Color color, boolean isBold) {
        Style style = doc.addStyle(name, doc.getStyle(StyleContext.DEFAULT_STYLE));
        StyleConstants.setForeground(style, color);
        StyleConstants.setBold(style, isBold);
    }

    /**
     * Adiciona texto à janela, aplicando estilos com base no conteúdo da mensagem.
     * @param text O texto a ser adicionado.
     */
    public void appendText(final String text) {
        SwingUtilities.invokeLater(() -> {
            StyledDocument doc = textPane.getStyledDocument();
            String styleName = getStyleNameFor(text); // Determina o estilo a ser usado

            try {
                doc.insertString(doc.getLength(), text, doc.getStyle(styleName));
                textPane.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                // Erro interno, não fazer nada para evitar loops infinitos
            }
        });
    }

    /**
     * Determina qual o nome do estilo a ser aplicado com base no conteúdo da linha de log.
     * @param text A linha de log.
     * @return O nome do estilo ("SUCCESS", "ERROR", etc.).
     */
    private String getStyleNameFor(String text) {
        String upperCaseText = text.toUpperCase();

        if (upperCaseText.contains("ERROR") || upperCaseText.contains("FALHA AO")) {
            return "ERROR";
        }
        if (upperCaseText.contains("PARADA PROCESSADO") || upperCaseText.contains("ENCERRANDO A PIPELINE")) {
            return "ERROR";
        }
        if (upperCaseText.contains("WARN") || upperCaseText.contains("AVISO")) {
            return "WARN";
        }
        if (upperCaseText.contains("CONCLUÍDA COM SUCESSO") || upperCaseText.contains("BEM-SUCEDIDO")) {
            return "SUCCESS";
        }
        if (upperCaseText.startsWith("--- INICIANDO") || upperCaseText.contains("FIM DO LOOP")) {
            return "LIFECYCLE";
        }
        if (upperCaseText.contains("INFO") || text.startsWith("[")) {
            return "INFO";
        }
        if (upperCaseText.contains("DEBUG") || upperCaseText.contains("AZ.SDK.MESSAGE")) {
            return "DEBUG";
        }

        return "DEFAULT";
    }
}