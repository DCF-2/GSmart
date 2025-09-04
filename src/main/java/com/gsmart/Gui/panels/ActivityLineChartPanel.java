

// Localização: src/main/java/com/gsmart/Gui/panels/ActivityLineChartPanel.java
package main.java.com.gsmart.Gui.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Um painel customizado para exibir um gráfico de linhas com 3 métricas de atividade.
 * <p>
 * Oferece uma visualização alternativa ao gráfico de barras, mostrando a tendência da
 * atividade de pipelines, alertas e alarmes ao longo do tempo. Este painel pode
 * ser alternado com o {@link ActivityBarChartPanel} no dashboard.
 */

public class ActivityLineChartPanel extends JPanel {

    private Map<String, Integer> pipelineData;
    private Map<String, Integer> alertData;
    private Map<String, Integer> alarmData;

    // --- Paleta de Cores para as Linhas ---
    private static final Color PIPELINE_COLOR = new Color(65, 105, 225); // Royal Blue
    private static final Color ALERT_COLOR = new Color(220, 20, 60);    // Crimson Red
    private static final Color ALARM_COLOR = new Color(255, 165, 0);   // Orange

    public ActivityLineChartPanel() {
        this.pipelineData = new LinkedHashMap<>();
        this.alertData = new LinkedHashMap<>();
        this.alarmData = new LinkedHashMap<>();
        setBorder(BorderFactory.createTitledBorder("Atividade Recente do Sistema"));
    }


    /**
     * Atualiza os dados das três métricas e força o redesenho do componente.
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

        int padding = 30;
        int labelPadding = 25;
        int width = getWidth();
        int height = getHeight();

        // Desenha o fundo branco e a grade
        drawGrid(g2d, padding, labelPadding, width, height);

        // Agrupa todos os rótulos de tempo (eixo X) e ordena
        List<String> timeLabels = Stream.of(pipelineData.keySet(), alertData.keySet(), alarmData.keySet())
                .flatMap(set -> set.stream())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        if (timeLabels.isEmpty()) return;

        // Encontra o valor máximo para o eixo Y
        int maxVal = Math.max(5, // Garante que o gráfico tenha uma altura mínima de 5
                Stream.of(pipelineData.values(), alertData.values(), alarmData.values())
                        .flatMap(col -> col.stream())
                        .max(Integer::compareTo).orElse(0)
        );

        // Desenha os eixos X e Y com seus rótulos
        drawAxes(g2d, padding, labelPadding, width, height, timeLabels, maxVal);

        // Desenha as linhas para cada métrica
        drawMetricPath(g2d, pipelineData, timeLabels, PIPELINE_COLOR, maxVal);
        drawMetricPath(g2d, alertData, timeLabels, ALERT_COLOR, maxVal);
        drawMetricPath(g2d, alarmData, timeLabels, ALARM_COLOR, maxVal);

        // Desenha a legenda
        drawLegend(g2d);
    }

    private void drawGrid(Graphics2D g2d, int p, int lp, int w, int h) {
        g2d.setColor(Color.WHITE);
        g2d.fillRect(p + lp, p, w - (2 * p) - lp, h - (2 * p) - lp);
        g2d.setColor(new Color(230, 230, 230)); // Cor da grade
        for (int i = 0; i < 10; i++) {
            int y = h - ((i * (h - p * 2 - lp)) / 10 + p + lp);
            g2d.drawLine(p + lp, y, w - p, y);
        }
    }

    private void drawAxes(Graphics2D g2d, int p, int lp, int w, int h, List<String> timeLabels, int maxVal) {
        g2d.setColor(Color.BLACK);
        // Rótulos do eixo Y
        for (int i = 0; i <= 10; i++) {
            int y = h - ((i * (h - p * 2 - lp)) / 10 + p + lp);
            String label = String.valueOf((int)Math.ceil(maxVal * i / 10.0));
            g2d.drawString(label, p, y);
        }
        // Rótulos do eixo X
        for(int i = 0; i < timeLabels.size(); i++) {
            int x = p + lp + (i * (w - p * 2 - lp) / (timeLabels.size() > 1 ? timeLabels.size() - 1 : 1));
            g2d.drawString(timeLabels.get(i), x - 15, h - p + 15);
        }
    }

    private void drawMetricPath(Graphics2D g2d, Map<String, Integer> data, List<String> allLabels, Color color, int maxVal) {
        if (data.isEmpty()) return;

        Path2D.Double path = new Path2D.Double();
        int p = 30;
        int lp = 25;
        int w = getWidth();
        int h = getHeight();

        // Mapeia os dados existentes para pontos na tela
        Map<Integer, Point> points = new LinkedHashMap<>();
        for(int i = 0; i < allLabels.size(); i++) {
            String label = allLabels.get(i);
            if(data.containsKey(label)){
                int x = p + lp + (i * (w - p * 2 - lp) / (allLabels.size() > 1 ? allLabels.size() - 1 : 1));
                int y = h - p - lp - (int) (data.get(label) * (double)(h - 2*p - lp) / maxVal);
                points.put(i, new Point(x,y));
            }
        }

        // Desenha a linha e os pontos
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(2f));
        boolean firstPoint = true;
        for(Point point : points.values()){
            if(firstPoint) {
                path.moveTo(point.x, point.y);
                firstPoint = false;
            } else {
                path.lineTo(point.x, point.y);
            }
            g2d.fillOval(point.x-3, point.y-3, 6, 6);
        }
        g2d.draw(path);
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
}
