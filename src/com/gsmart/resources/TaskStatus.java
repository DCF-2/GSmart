// Localização: src/main/java/com/gsmart/TaskStatus.java
package com.gsmart.resources;

public enum TaskStatus {
    RUNNING("Executando"),
    STOPPING("Parando..."),
    FINISHED("Finalizado"),
    ERROR("Erro");

    private final String displayName;

    TaskStatus(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}