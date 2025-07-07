// Localização: src/main/java/com/gsmart/GSmartListener.java
package com.gsmart.resources;

/**
 * Define uma interface de "ouvinte" para comunicação entre a lógica de pipeline
 * e a interface do utilizador (UI).
 *
 * As classes que implementam esta interface podem receber notificações em tempo real
 * sobre eventos importantes que ocorrem dentro de uma {@code DataPipeline}, como
 * mudanças de estado, perda de conexão ou a geração de novos insights.
 */
public interface GSmartListener {

    /**
     * Chamado quando um novo insight é gerado pela lógica de negócio.
     * @param message A mensagem do insight a ser exibida.
     * @param type O tipo de insight (ex: "CUSTO", "MANUTENCAO", "FALHA").
     */
    void onInsight(String message, String type);

    /**
     * Chamado quando um alerta crítico que requer atenção do utilizador é gerado.
     * @param title O título do alerta.
     * @param message A mensagem detalhada do alerta.
     */
    void onAlert(String title, String message);

    /**
     * Chamado sempre que o estado de uma tarefa de pipeline muda.
     * @param status O novo {@code TaskStatus} da tarefa.
     */
    void onStatusUpdate(TaskStatus status);

    /**
     * Chamado quando a pipeline perde a sua conexão com a fonte de dados.
     * @param errorMessage A mensagem de erro que causou a perda de conexão.
     */
    void onConnectionLost(String errorMessage);

    /**
     * Chamado quando a pipeline inicia uma nova tentativa de reconexão.
     * @param delayInSeconds O tempo em segundos que a pipeline aguardará antes da próxima tentativa.
     */
    void onReconnectionAttempt(long delayInSeconds);

    /**
     * Chamado quando a conexão com a fonte de dados é restaurada com sucesso.
     */
    void onConnectionRestored();
}