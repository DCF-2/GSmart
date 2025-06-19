// Localização: src/main/java/com/gsmart/GSmartListener.java
package com.gsmart;

public interface GSmartListener {
    /**
     * Chamado para exibir uma mensagem informativa na área de texto principal.
     * @param message A mensagem do insight.
     * @param type O tipo de insight (ex: CUSTO, PROCESSO) para colorização.
     */
    void onInsight(String message, String type);

    /**
     * Chamado para exibir um alerta crítico em uma janela de pop-up.
     * @param title O título da janela de alerta.
     * @param message A mensagem do alerta.
     */
    void onAlert(String title, String message);
}