// Localização: src/main/java/com/gsmart/Gui/windows/HelpWindow.java
package main.java.com.gsmart.Gui.windows;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Janela de Ajuda que fornece informações, FAQs e links para tutoriais.
 * <p>
 * Esta janela utiliza um {@link JTabbedPane} para organizar o conteúdo de ajuda
 * em diferentes secções, como "Dúvidas Frequentes" e "Vídeos Tutoriais",
 * melhorando a experiência de suporte ao utilizador.
 */
public class HelpWindow extends JDialog {

    /**
     * Constrói a janela de ajuda e suporte.
     *
     * @param owner A janela pai à qual este diálogo está associado.
     */
    public HelpWindow(Frame owner) {
        super(owner, "Ajuda e Suporte GSmart", true);
        setSize(700, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        // --- Criação dos Separadores (Abas) ---
        JTabbedPane tabbedPane = new JTabbedPane();

        // --- Separador 1: Dúvidas Frequentes (FAQ) ---
        JPanel faqPanel = createFaqPanel();
        tabbedPane.addTab("Dúvidas Frequentes (FAQ)", faqPanel);

        // --- Separador 2: Vídeos Tutoriais ---
        JPanel videosPanel = createVideosPanel();
        tabbedPane.addTab("Vídeos Tutoriais", videosPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    /**
     * Cria o painel que conterá a secção de Perguntas Frequentes.
     */
    private JPanel createFaqPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Usamos JEditorPane para poder formatar o texto com HTML simples
        JEditorPane editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setEditable(false);
        editorPane.setBackground(panel.getBackground()); // Cor de fundo igual ao painel

        // --- CONTEÚDO DO FAQ (Pode ser expandido) ---
        String faqText = "<html>" +
                "<body style='font-family: Segoe UI; font-size: 12px;'>" +
                "<h2>Dúvidas Frequentes</h2>" +
                "<p>Aqui encontrará respostas para as perguntas mais comuns sobre o GSmart.</p>" +

                "<h3><b>P: Como configuro uma nova pipeline?</b></h3>" +
                "<p><b>R:</b> Vá ao separador 'Configuração da Pipeline', selecione a sua fonte de dados (ThingsBoard ou Banco de Dados), preencha as credenciais e clique em 'Conectar'. Depois, selecione o dispositivo/tabela para carregar as métricas.</p>" +

                "<h3><b>P: O que são 'Regras de Alerta' e 'Regras de Alarmes'?</b></h3>" +
                "<p><b>R:</b> <b>Alertas</b> são para condições críticas que exigem atenção imediata (ex: temperatura acima de 100°C). <b>Alarmes</b> (anteriormente 'Insights') são para observações inteligentes e proativas (ex: 'O consumo de energia está 20% acima da média').</p>" +

                "<h3><b>P: A minha pipeline parou com um erro. O que faço?</b></h3>" +
                "<p><b>R:</b> Abra a 'Central de Monitoramento'. A pipeline com erro terá um botão 'Reiniciar'. Se o erro persistir, verifique a janela 'Log Geral' para mais detalhes sobre a causa da falha.</p>" +
                "</body></html>";

        editorPane.setText(faqText);

        // Colocamos o editor dentro de um JScrollPane para o caso de o texto ser muito longo
        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setBorder(null); // Remove a borda do scrollpane
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Cria o painel que conterá os links para os vídeos tutoriais.
     */
    private JPanel createVideosPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        panel.add(createVideoLinkButton("1. Como Configurar a Conexão com o ThingsBoard", "https://www.youtube.com/watch?v=dQw4w9WgXcQ"));
        panel.add(Box.createRigidArea(new Dimension(0, 10))); // Espaçamento
        panel.add(createVideoLinkButton("2. Criando a sua Primeira Regra de Alerta", "https://www.youtube.com/watch?v=dQw4w9WgXcQ"));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(createVideoLinkButton("3. Entendendo o Dashboard e a Central de Monitoramento", "https://www.youtube.com/watch?v=dQw4w9WgXcQ"));

        return panel;
    }

    /**
     * Método auxiliar para criar um botão que abre um link no navegador.
     */
    private JButton createVideoLinkButton(String text, String url) {
        JButton button = new JButton(text);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(e -> {
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(new URI(url));
                }
            } catch (IOException | URISyntaxException ex) {
                JOptionPane.showMessageDialog(this, "Não foi possível abrir o link.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        return button;
    }
}