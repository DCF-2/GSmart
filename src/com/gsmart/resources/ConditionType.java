package com.gsmart.resources;

/**
 * Define os tipos de condição que podem ser usados nas regras de alerta.
 * Cada tipo tem um símbolo para exibição na interface.
 */
public enum ConditionType {
    GREATER_THAN("Maior que (>)"),
    LESS_THAN("Menor que (<)"),
    EQUALS("Igual a (==)");

    private final String displayName;

    ConditionType(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}