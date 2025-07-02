// Localização: src/main/java/functrendz/GeradorDeInsights.java
package com.gsmart.controller;

import com.gsmart.resources.GSmartListener;
import java.util.Locale;
import static com.gsmart.controller.Manutencao.StatusManutencao;

public class GeradorDeInsights {

    private static final double LIMIAR_EFICIENCIA_MATERIAL = 97.0;
    private static final double LIMIAR_TEMPERATURA_ALTA = 78.0;
    private static final double LIMIAR_FATOR_POTENCIA_BAIXO = 0.90;

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