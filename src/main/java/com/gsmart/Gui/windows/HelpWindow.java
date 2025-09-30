// Localização: src/main/java/com/gsmart/Gui/windows/HelpWindow.java
package main.java.com.gsmart.Gui.windows;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Janela de Ajuda que fornece informações, FAQs e links para tutoriais.
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
        // (Este método permanece o mesmo)
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

    private JPanel createVideosPanel() {
        // (Este método permanece o mesmo)
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        panel.add(createSectionLabel("Introdução"));
        panel.add(createVideoLinkButton("1. O Que é o GSmart? (Introdução e Instalação)", "GSmart Tutorial - Video 1.mp4"));
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        panel.add(createSectionLabel("Configuração Inicial"));
        panel.add(createVideoLinkButton("2. Conexão às Fontes de Dados (Primeiros Passos)", "GSmart Tutorial - Video 2.mp4"));
        panel.add(createVideoLinkButton("3. Criando o Seu Primeiro Pipeline de Dados", "GSmart Tutorial - Video 3.mp4"));
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        panel.add(createSectionLabel("Funcionalidades Essenciais"));
        panel.add(createVideoLinkButton("4. Desvendando as Regras (Alertas e Insights)", "GSmart Tutorial - Video 4.mp4"));
        panel.add(createVideoLinkButton("5. Dashboard: O Seu Centro de Comando", "GSmart Tutorial - Video 5.mp4"));
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        panel.add(createSectionLabel("Gestão e Dicas"));
        panel.add(createVideoLinkButton("6. Gestão de Utilizadores (Para Administradores)", "GSmart Tutorial - Video 6.mp4"));
        panel.add(createVideoLinkButton("7. Dicas e Truques Avançados", "GSmart Tutorial - Video 7.mp4"));

        return panel;
    }

    private JLabel createSectionLabel(String text) {
        // (Este método permanece o mesmo)
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(new EmptyBorder(5, 0, 5, 0));
        return label;
    }

    private JButton createVideoLinkButton(String text, String videoFileName) {
        // (Este método permanece o mesmo)
        JButton button = new JButton("<html><body style='text-align:left;'>▶ " + text + "</body></html>");
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setForeground(new Color(0, 102, 204));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.addActionListener(e -> openLocalVideo(videoFileName));
        return button;
    }

    private void openLocalVideo(String videoFileName) {
        try {
            // --- ✨ CORREÇÃO APLICADA AQUI ✨ ---
            // O caminho agora aponta para a sua pasta "/GSmart-Tutoriais/"
            String resourcePath = "/GSmart-Tutoriais/" + videoFileName;
            InputStream videoStream = getClass().getResourceAsStream(resourcePath);

            if (videoStream == null) {
                JOptionPane.showMessageDialog(this, "Ficheiro de vídeo não encontrado no caminho: " + resourcePath, "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            File tempFile = File.createTempFile("gsmart_tutorial_", ".mp4");
            tempFile.deleteOnExit();

            try (OutputStream outputStream = new FileOutputStream(tempFile)) {
                videoStream.transferTo(outputStream);
            }

            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(tempFile);
            } else {
                JOptionPane.showMessageDialog(this, "Abertura de ficheiros de vídeo não suportada neste sistema.", "Erro de Compatibilidade", JOptionPane.ERROR_MESSAGE);
            }

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Ocorreu um erro ao tentar abrir o vídeo:\n" + ex.getMessage(), "Erro de Ficheiro", JOptionPane.ERROR_MESSAGE);
        }
    }
}