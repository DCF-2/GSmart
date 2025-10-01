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

    public HelpWindow(Frame owner) {
        super(owner, "Ajuda e Suporte GSmart", true);
        setSize(700, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Dúvidas Frequentes (FAQ)", createFaqPanel());
        tabbedPane.addTab("Vídeos Tutoriais", createVideosPanel());
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createFaqPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        JEditorPane editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setEditable(false);
        editorPane.setBackground(panel.getBackground());
        String faqText = "<html>" +
                "<body style='font-family: Segoe UI; font-size: 12px;'>" +
                "<h2>Dúvidas Frequentes</h2>" +
                "<p>Aqui encontrará respostas para as perguntas mais comuns sobre o GSmart.</p>" +
                "<h3><b>P: Como configuro uma nova pipeline?</b></h3>" +
                "<p><b>R:</b> Vá ao separador 'Configurar Pipeline', selecione a sua fonte de dados (ThingsBoard ou Banco de Dados), preencha as credenciais e clique em 'Conectar'. Depois, selecione o dispositivo/tabela para carregar as métricas.</p>" +
                "<h3><b>P: O que são 'Regras de Alerta' e 'Regras de Alarmes'?</b></h3>" +
                "<p><b>R:</b> <b>Alertas</b> são para condições críticas que exigem atenção imediata (ex: temperatura acima de 100°C). <b>Alarmes</b> (anteriormente 'Insights') são para observações inteligentes e proativas (ex: 'O consumo de energia está 20% acima da média').</p>" +
                "<h3><b>P: A minha pipeline parou com um erro. O que faço?</b></h3>" +
                "<p><b>R:</b> Abra a 'Central de Monitoramento'. A pipeline com erro terá um botão 'Reiniciar'. Se o erro persistir, verifique a janela 'Log Geral' para mais detalhes sobre a causa da falha.</p>" +
                "</body></html>";
        editorPane.setText(faqText);
        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Cria o painel que conterá os links para os vídeos tutoriais online.
     */
    private JPanel createVideosPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        panel.add(createSectionLabel("Introdução"));
        panel.add(createVideoLinkButton("1. O Que é o GSmart? (Introdução e Instalação)", "https://drive.google.com/file/d/1U3MJznb8NLfccVTDxbqMBr8Y2u-BvcO3/view?usp=drive_link"));
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        panel.add(createSectionLabel("Configuração Inicial"));
        panel.add(createVideoLinkButton("2. Conexão às Fontes de Dados (Primeiros Passos)", "https://drive.google.com/file/d/1xEzoE771FC5JtT9aYh-biKDo-HMAABEG/view?usp=drive_link"));
        panel.add(createVideoLinkButton("3. Criando o Seu Primeiro Pipeline de Dados", "https://drive.google.com/file/d/1b3UJHjhZAVOLl3twVoKSVskPCdl3nCXt/view?usp=drive_link"));
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        panel.add(createSectionLabel("Funcionalidades Essenciais"));
        panel.add(createVideoLinkButton("4. Desvendando as Regras (Alertas e Insights)", "https://drive.google.com/file/d/12a-H2f0pEPOdg_KZznLSWuGkBT179CSM/view?usp=drive_link"));
        panel.add(createVideoLinkButton("5. Dashboard: O Seu Centro de Comando", "https://drive.google.com/file/d/1zL3FrUZj2SFMmuS8mstbUXKZH-yYkgEW/view?usp=drive_link"));
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        panel.add(createSectionLabel("Gestão e Dicas"));
        panel.add(createVideoLinkButton("6. Gestão de Utilizadores (Para Administradores)", "https://drive.google.com/file/d/18IVvthrE4HvauYX5_EfR6i_iDOeMs-dZ/view?usp=drive_link"));
        panel.add(createVideoLinkButton("7. Dicas e Truques Avançados", "https://drive.google.com/file/d/1HAYASx1azGc4gwWeH0Qf5bO4kxYPzlW9/view?usp=drive_link"));

        return panel;
    }

    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(new EmptyBorder(5, 0, 5, 0));
        return label;
    }

    /**
     * Método auxiliar para criar um botão que abre um URL no navegador.
     */
    private JButton createVideoLinkButton(String text, String url) {
        JButton button = new JButton("<html><body style='text-align:left;'>▶ " + text + "</body></html>");
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setForeground(new Color(0, 102, 204));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);

        button.addActionListener(e -> openUrlInBrowser(url));

        return button;
    }

    /**
     * Abre um URL no navegador padrão do sistema.
     * @param url O endereço da web a ser aberto.
     */
    private void openUrlInBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                JOptionPane.showMessageDialog(this, "Abertura de links não suportada neste sistema.", "Erro de Compatibilidade", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException | URISyntaxException ex) {
            JOptionPane.showMessageDialog(this, "Não foi possível abrir o link: " + url, "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}