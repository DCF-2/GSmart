// Localização: src/main/java/com/gsmart/MetricConfig.java
package com.gsmart;

public class MetricConfig {
    private boolean selected;
    private final String originalName;
    private String alias;

    public MetricConfig(String originalName) {
        this.selected = true; // Por padrão, todas as métricas vêm selecionadas
        this.originalName = originalName;
        this.alias = originalName; // O alias inicial é o próprio nome original
    }

    // Getters e Setters que a tabela usará para ler e escrever os valores
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
}