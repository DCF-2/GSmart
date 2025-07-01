// Localização: src/main/java/com/gsmart/config/MetricConfig.java
package com.gsmart.config;

public class MetricConfig {
    private boolean selected;
    private final String originalName;
    private String alias;
    private String expression;
    private final boolean isSystemMetric;

    /**
     * Construtor principal que permite definir todos os campos.
     * @param originalName O nome da métrica.
     * @param isSelected Se ela deve ser enviada por padrão.
     * @param isSystem Se é uma métrica interna do sistema (para travar o checkbox).
     */
    public MetricConfig(String originalName, boolean isSelected, boolean isSystem) {
        this.selected = isSelected;
        this.originalName = originalName;
        this.alias = originalName;
        this.expression = "";
        this.isSystemMetric = isSystem;
    }

    /**
     * Construtor simplificado para as métricas que vêm da fonte de dados.
     * Elas são, por padrão, selecionadas e não são do sistema.
     * @param originalName O nome da métrica vinda da fonte.
     */
    public MetricConfig(String originalName) {
        this(originalName, true, false);
    }

    public boolean isSelected() {return selected;}

    public void setSelected(boolean selected) {this.selected = selected;}

    public String getOriginalName() {return originalName;}

    public String getAlias() {return alias;}

    public void setAlias(String alias) {this.alias = alias;}

    public String getExpression() {return expression;}

    public void setExpression(String expression) {this.expression = expression;}

    public boolean isSystemMetric() {return isSystemMetric;}
}