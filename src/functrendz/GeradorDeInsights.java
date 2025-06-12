package functrendz;

import java.util.Locale;
import static functrendz.Manutencao.StatusManutencao;

public class GeradorDeInsights {

    private static final double LIMIAR_EFICIENCIA_MATERIAL = 97.0; // %
    private static final double LIMIAR_TEMPERATURA_ALTA = 78.0;   // Celsius
    private static final double LIMIAR_FATOR_POTENCIA_BAIXO = 0.90;

    public static void gerarInsightsDoCiclo(double eficienciaMaterial, double fatorPotencia, double temperatura,
                                            StatusManutencao statusManutencao, boolean falhaPrevista) {

        System.out.println("\n[ANÁLISE E DIAGNÓSTICO DO CICLO]");
        boolean insightGerado = false;

        // Regra 1: Causa-Raiz para Baixa Eficiência
        if (eficienciaMaterial < LIMIAR_EFICIENCIA_MATERIAL && temperatura > LIMIAR_TEMPERATURA_ALTA) {
            System.out.printf(Locale.US, """
                -> INSIGHT DE PROCESSO [CAUSA E EFEITO]
                   - Causa: Alta temperatura de operação (%.1f°C).
                   - Efeito: Eficiência de material (%.2f%%) abaixo da meta de %.1f%%.
                   - Hipótese: Superaquecimento pode estar degradando a matéria-prima.%n""",
                    temperatura, eficienciaMaterial, LIMIAR_EFICIENCIA_MATERIAL);
            insightGerado = true;
        }

        // Regra 2: Oportunidade de Custo
        if (fatorPotencia < LIMIAR_FATOR_POTENCIA_BAIXO) {
            System.out.printf(Locale.US, """
                -> INSIGHT DE CUSTO [OPORTUNIDADE]
                   - Condição: Fator de potência (%.2f) abaixo do limiar de %.2f.
                   - Oportunidade: Corrigir o fator de potência pode reduzir custos com energia elétrica e evitar multas.%n""",
                    fatorPotencia, LIMIAR_FATOR_POTENCIA_BAIXO);
            insightGerado = true;
        }

        // Regra 3: Confirmação de Problema
        if (statusManutencao == StatusManutencao.ALERTA_TEMPERATURA) {
            System.out.printf(Locale.US, """
                -> INSIGHT DE MANUTENÇÃO [URGÊNCIA]
                   - Evento: Alerta de manutenção por superaquecimento foi acionado.
                   - Impacto: A eficiência de material (%.2f%%) pode ser afetada se o problema persistir.
                   - Diagnóstico: O problema de manutenção deve ser tratado para evitar perdas de produção.%n""",
                    eficienciaMaterial);
            insightGerado = true;
        }

        // Regra 4: Causa-Raiz da Previsão de Falha
        if (falhaPrevista) {
            System.out.println("""
                -> INSIGHT DE FALHA [CRÍTICO]
                   - Diagnóstico: Previsão de falha acionada!
                   - Evidência: Desvio contínuo nos padrões de telemetria (temperatura e potência).
                   - Ação Imediata: Inspecionar sistema de refrigeração e alimentação elétrica como causa raiz provável.""");
            insightGerado = true;
        }

        if (!insightGerado) {
            System.out.println("-> Nenhum insight crítico gerado. O processo está operando dentro dos parâmetros esperados.");
        }
    }
}