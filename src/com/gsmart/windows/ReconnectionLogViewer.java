package com.gsmart.windows;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Uma janela de UI dedicada a exibir os logs de reconexão da aplicação.
 *
 * Esta classe foca-se em ler o ficheiro de log específico de reconexões
 * (ex: reconnection.log) e apresentar o seu conteúdo. Isto permite isolar
 * e analisar facilmente os eventos de perda e restabelecimento de conexão
 * das pipelines, facilitando a depuração de problemas de rede.
 */
public class ReconnectionLogViewer extends JFrame {

    private final JTextPane textPane; // MUDANÇA: De JTextArea para JTextPane
    private static final String LOG_FILE_PATH = "reconnection.log";

    // Definindo as cores para os logs
    private static final Color COR_FALHA = new Color(200, 0, 0);
    private static final Color COR_SUCESSO = new Color(0, 128, 0);
    private static final Color COR_TENTATIVA = new Color(255, 140, 0);

    public ReconnectionLogViewer() {
        setTitle("GSmart - Log de Reconexões");
        setSize(750, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // MUDANÇA: Inicializa JTextPane em vez de JTextArea
        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textPane.setMargin(new Insets(5, 5, 5, 5)); // Adiciona um pouco de margem interna

        JScrollPane scrollPane = new JScrollPane(textPane);

        JButton reloadButton = new JButton("Recarregar Log");
        reloadButton.addActionListener(e -> loadLogFile());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(reloadButton);

        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Carrega e colore o conteúdo do arquivo de log.
     */
    public void loadLogFile() {
        // Limpa o conteúdo anterior
        textPane.setText("");

        try (Stream<String> lines = Files.lines(Paths.get(LOG_FILE_PATH), StandardCharsets.UTF_8)) {
            lines.forEach(line -> {
                Color color = getColorForLine(line);
                appendColoredText(line + "\n", color);
            });
            // Move o scroll para o final para ver as últimas entradas
            SwingUtilities.invokeLater(() -> textPane.setCaretPosition(textPane.getDocument().getLength()));
        } catch (IOException e) {
            appendColoredText("Não foi possível carregar o arquivo de log: " + LOG_FILE_PATH + "\n\n" + e.getMessage(), COR_FALHA);
        }
    }

    /**
     * Adiciona uma string com uma cor específica ao JTextPane.
     */
    private void appendColoredText(String text, Color color) {
        StyledDocument doc = textPane.getStyledDocument();
        Style style = textPane.addStyle("Color Style", null);
        StyleConstants.setForeground(style, color);
        try {
            doc.insertString(doc.getLength(), text, style);
        } catch (BadLocationException e) {
            // Este erro é raro, mas é bom ter o log
            System.err.println("Falha ao adicionar texto estilizado: " + e.getMessage());
        }
    }

    /**
     * Determina a cor com base no conteúdo da linha do log.
     */
    private Color getColorForLine(String line) {
        if (line.contains("CONEXÃO PERDIDA")) {
            return COR_FALHA;
        } else if (line.contains("FALHOU")) {
            return COR_TENTATIVA;
        } else if (line.contains("CONEXÃO RESTABELECIDA")) {
            return COR_SUCESSO;
        }
        return Color.BLACK; // Cor padrão
    }
}