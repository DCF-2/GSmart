// Localização: src/main/java/com/gsmart/TaskStatus.java
package com.gsmart.resources;

/**
 * Representa os possíveis estados de uma tarefa de pipeline ({@code PipelineTask}).
 *
 * Esta enumeração define o ciclo de vida de uma tarefa, desde o seu início
 * até à sua finalização, incluindo estados de erro e de paragem.
 */
public enum TaskStatus {
    /**
     * A tarefa está atualmente em execução e a processar dados.
     */
    RUNNING("Executando"),

    /**
     * A tarefa está no processo de ser parada, aguardando a finalização
     * segura do seu ciclo atual.
     */
    STOPPING("Parando..."),

    /**
     * A tarefa foi parada com sucesso e já não está em execução.
     */
    STOPPED("Pausada"),

    /**
     * A tarefa foi completamente finalizada e os seus recursos foram libertados.
     */
    FINISHED("Finalizado"),

    /**
     * A tarefa encontrou um erro e parou a sua execução normal.
     * Pode estar a aguardar uma ação do utilizador, como reiniciar.
     */
    ERROR("Erro"),

    /**
     * A tarefa perdeu a conexão e está a tentar reconectar-se ativamente.
     */
    RECONNECTING("Reconnectando...");

    private final String displayName;

    TaskStatus(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}