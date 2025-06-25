// Localização: src/main/java/com/gsmart/MetricConfig.java
package com.gsmart.config;

public class MetricConfig {
    private boolean selected;
    private final String originalName;
    private String alias;
    private String expression; // --- NOVO CAMPO ---

    public MetricConfig(String originalName) {
        this.selected = true;
        this.originalName = originalName;
        this.alias = originalName;
        this.expression = ""; // Inicializa como vazio
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }
}