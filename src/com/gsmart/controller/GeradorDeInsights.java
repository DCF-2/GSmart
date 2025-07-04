// Localização: src/main/java/functrendz/GeradorDeInsights.java
package com.gsmart.controller;

import com.gsmart.resources.GSmartListener;
import java.util.Locale;
import static com.gsmart.controller.Manutencao.StatusManutencao;

/**
 * Classe utilitária estática que atua como o motor de regras de negócio.
 * Sua responsabilidade é analisar um conjunto de métricas de um ciclo de processamento
 * e gerar "insights" ou alertas humanamente legíveis com base em limiares pré-definidos.
 *
 * As regras avaliam custos, diagnósticos de manutenção e previsões de falhas,
 * comunicando os resultados através do {@link GSmartListener}.
 */
public class GeradorDeInsights {

    private static final double LIMIAR_EFICIENCIA_MATERIAL = 97.0;
    private static final double LIMIAR_TEMPERATURA_ALTA = 78.0;
    private static final double LIMIAR_FATOR_POTENCIA_BAIXO = 0.90;

    /**
     * Analisa as métricas consolidadas de um ciclo de pipeline e gera os insights correspondentes.
     * Compara os valores de entrada com os limiares da classe e, se uma condição for atendida,
     * formata uma mensagem descritiva e a envia através do listener.
     *
     * @param listener O canal de comunicação para enviar os insights gerados para a GUI.
     * @param eficienciaMaterial O valor da eficiência de material a ser avaliado.
     * @param fatorPotencia O valor do fator de potência a ser avaliado.
     * @param temperatura O valor da temperatura a ser avaliado.
     * @param statusManutencao O status atual da manutenção, vindo da classe {@link Manutencao}.
     * @param falhaPrevista Um booleano indicando se uma falha foi prevista pela classe {@link PrevisaoFalhas}.
     */
    public static void gerarInsightsDoCiclo(GSmartListener listener, double eficienciaMaterial, double fatorPotencia, double temperatura,
                                            StatusManutencao statusManutencao, boolean falhaPrevista) {

        boolean insightGerado = false;

        if (fatorPotencia < LIMIAR_FATOR_POTENCIA_BAIXO && fatorPotencia != 0.0) { // Ignora valor não inicializado
            String insight = String.format(Locale.US, "[OPORTUNIDADE DE CUSTO]\n  - Fator de potência (%.2f) abaixo do ideal (%.2f).\n  - Corrigir pode reduzir custos com energia.",
                    fatorPotencia, LIMIAR_FATOR_POTENCIA_BAIXO);
            listener.onInsight(insight, "CUSTO");
            insightGerado = true;
        }

        if (statusManutencao == StatusManutencao.ALERTA_TEMPERATURA) {
            String insight = String.format(Locale.US, "[DIAGNÓSTICO DE MANUTENÇÃO]\n  - Alerta de superaquecimento acionado.\n  - A eficiência de material (%.2f%%) pode ser impactada.", eficienciaMaterial);
            listener.onInsight(insight, "MANUTENCAO");
            insightGerado = true;
        }

        if (falhaPrevista) {
            String insight = "[DIAGNÓSTICO DE FALHA]\n  - Previsão de falha acionada devido a desvios de telemetria.\n  - Inspecionar sistema de refrigeração e alimentação.";
            listener.onInsight(insight, "FALHA");
            insightGerado = true;
        }

        if (!insightGerado) {
            listener.onInsight("[INFO] Nenhum insight crítico gerado nesta análise.", "INFO");
        }
    }
}