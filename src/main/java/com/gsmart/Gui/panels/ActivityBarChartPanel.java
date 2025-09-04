// Localização: src/main/java/com/gsmart/Gui/panels/ActivityBarChartPanel.java
package main.java.com.gsmart.Gui.panels;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Um painel customizado para exibir um gráfico de barras agrupado com 3 métricas de atividade.
 * <p>
 * Este componente é responsável por desenhar um gráfico de barras que compara a
 * atividade de pipelines, alertas e alarmes ao longo do tempo, fornecendo uma
 * representação visual imediata da carga e dos eventos do sistema.
 */
public class ActivityBarChartPanel extends JPanel {

    private Map<String, Integer> pipelineData;
    private Map<String, Integer> alertData;
    private Map<String, Integer> alarmData;

    private static final Color PIPELINE_COLOR = new Color(65, 105, 225); // Royal Blue
    private static final Color ALERT_COLOR = new Color(220, 20, 60);    // Crimson Red
    private static final Color ALARM_COLOR = new Color(255, 165, 0);   // Orange

    public ActivityBarChartPanel() {
        this.pipelineData = new LinkedHashMap<>();
        this.alertData = new LinkedHashMap<>();
        this.alarmData = new LinkedHashMap<>();
        setBorder(BorderFactory.createTitledBorder("Atividade Recente do Sistema"));
    }

    /**
     * Atualiza os dados das três métricas e força o redesenho do gráfico.
     *
     * @param pData Os dados de atividade das pipelines (rótulo de tempo -> contagem).
     * @param aData Os dados de atividade dos alertas (rótulo de tempo -> contagem).
     * @param alData Os dados de atividade dos alarmes (rótulo de tempo -> contagem).
     */
    public void updateData(Map<String, Integer> pData, Map<String, Integer> aData, Map<String, Integer> alData) {
        this.pipelineData = pData;
        this.alertData = aData;
        this.alarmData = alData;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Agrupa todos os rótulos de tempo (eixo X) e ordena
        List<String> timeLabels = Stream.of(pipelineData.keySet(), alertData.keySet(), alarmData.keySet())
                .flatMap(set -> set.stream())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        if (timeLabels.isEmpty()) {
            drawEmptyState(g2d);
            return;
        }

        // Encontra o valor máximo para a escala do eixo Y
        int maxVal = Math.max(5, // Garante altura mínima de 5
                Stream.of(pipelineData.values(), alertData.values(), alarmData.values())
                        .flatMap(col -> col.stream())
                        .max(Integer::compareTo).orElse(0)
        );

        int padding = 30;
        int labelPadding = 25;
        int chartWidth = getWidth() - (2 * padding) - labelPadding;
        int chartHeight = getHeight() - (2 * padding) - labelPadding;

        drawAxesAndGrid(g2d, padding, labelPadding, getWidth(), getHeight(), maxVal);

        int groupWidth = chartWidth / timeLabels.size();
        int barWidth = Math.max(5, groupWidth / 4); // Largura de cada barra individual
        int barSpacing = (groupWidth - (barWidth * 3)) / 2; // Espaço no início de cada grupo

        for (int i = 0; i < timeLabels.size(); i++) {
            String label = timeLabels.get(i);
            int groupX = padding + labelPadding + (i * groupWidth);

            // Rótulo do eixo X para o grupo
            g2d.setColor(Color.BLACK);
            g2d.drawString(label, groupX + groupWidth / 2 - 15, getHeight() - padding + 15);

            // Barra de Pipelines
            int pVal = pipelineData.getOrDefault(label, 0);
            int pBarH = (int) ((double) pVal / maxVal * chartHeight);
            g2d.setColor(PIPELINE_COLOR);
            g2d.fillRect(groupX + barSpacing, getHeight() - padding - labelPadding - pBarH, barWidth, pBarH);

            // Barra de Alertas
            int aVal = alertData.getOrDefault(label, 0);
            int aBarH = (int) ((double) aVal / maxVal * chartHeight);
            g2d.setColor(ALERT_COLOR);
            g2d.fillRect(groupX + barSpacing + barWidth, getHeight() - padding - labelPadding - aBarH, barWidth, aBarH);

            // Barra de Alarmes
            int alVal = alarmData.getOrDefault(label, 0);
            int alBarH = (int) ((double) alVal / maxVal * chartHeight);
            g2d.setColor(ALARM_COLOR);
            g2d.fillRect(groupX + barSpacing + (barWidth * 2), getHeight() - padding - labelPadding - alBarH, barWidth, alBarH);
        }

        drawLegend(g2d);
    }

    private void drawAxesAndGrid(Graphics2D g2d, int p, int lp, int w, int h, int maxVal) {
        g2d.setColor(Color.BLACK);
        // Eixo Y (linha vertical) e Eixo X (linha horizontal)
        g2d.drawLine(p + lp, h - p - lp, p + lp, p);
        g2d.drawLine(p + lp, h - p - lp, w - p, h - p - lp);

        // Rótulos do eixo Y e linhas da grade
        for (int i = 0; i <= 10; i++) {
            int y = h - ((i * (h - p * 2 - lp)) / 10 + p + lp);
            String label = String.valueOf((int)Math.ceil(maxVal * i / 10.0));
            g2d.setColor(Color.GRAY);
            g2d.drawString(label, p, y + 4);
            g2d.setColor(new Color(230, 230, 230)); // Cor da grade
            g2d.drawLine(p + lp + 1, y, w - p, y);
        }
    }

    private void drawLegend(Graphics2D g2d) {
        int x = getWidth() - 150;
        int y = 40;
        g2d.setColor(PIPELINE_COLOR);
        g2d.fillRect(x, y - 10, 10, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Pipelines Ativas", x + 15, y);

        y += 20;
        g2d.setColor(ALERT_COLOR);
        g2d.fillRect(x, y - 10, 10, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Alertas Ativos", x + 15, y);

        y += 20;
        g2d.setColor(ALARM_COLOR);
        g2d.fillRect(x, y - 10, 10, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Alarmes Gerados", x + 15, y);
    }

    private void drawEmptyState(Graphics2D g2d){
        String text = "Aguardando dados...";
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        g2d.setColor(Color.GRAY);
        g2d.drawString(text, (getWidth() - textWidth) / 2, getHeight() / 2);
        drawLegend(g2d);
    }
}