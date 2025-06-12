package functrendz;

import java.util.Locale;

public class Manutencao {
    private static int ciclosBaixoFator = 0;
    private static int ciclosAltaTemperatura = 0;
    private static final double LIMITE_FATOR_POTENCIA = 0.90;
    private static final double LIMITE_TEMPERATURA = 75.0;

    public enum StatusManutencao {
        OK,
        ALERTA_FATOR_POTENCIA,
        ALERTA_TEMPERATURA
    }

    public static StatusManutencao verificarManutencao(double fatorPotencia, double temperatura) {
        StatusManutencao status = StatusManutencao.OK;

        // Lógica para Fator de Potência
        if (fatorPotencia < LIMITE_FATOR_POTENCIA) {
            ciclosBaixoFator++;
        } else {
            ciclosBaixoFator = 0;
        }

        if (ciclosBaixoFator >= 3) {
            // --- MUDANÇA AQUI: Alerta detalhado ---
            System.out.println("\n--- ALERTA DE MANUTENÇÃO PREVENTIVA ---");
            System.out.printf(Locale.US, """
                Causa: Baixo Fator de Potência persistente.
                  - Ciclos com Anomalia: %d de 3
                  - Limite Aceitável: > %.2f
                  - Última Leitura: %.2f
                  - Ação: Agendar verificação da rede elétrica/banco de capacitores.
                ------------------------------------------%n""",
                    ciclosBaixoFator, LIMITE_FATOR_POTENCIA, fatorPotencia);
            status = StatusManutencao.ALERTA_FATOR_POTENCIA;
        }

        // Lógica para Temperatura
        if (temperatura > LIMITE_TEMPERATURA) {
            ciclosAltaTemperatura++;
        } else {
            ciclosAltaTemperatura = 0;
        }

        if (ciclosAltaTemperatura >= 5) {
            // --- MUDANÇA AQUI: Alerta detalhado ---
            System.out.println("\n--- ALERTA DE MANUTENÇÃO CRÍTICA ---");
            System.out.printf(Locale.US, """
                Causa: Superaquecimento persistente.
                  - Ciclos com Anomalia: %d de 5
                  - Limite Aceitável: < %.1f°C
                  - Última Leitura: %.1f°C
                  - Ação: Agendar manutenção IMEDIATA do sistema de refrigeração.
                ------------------------------------------%n""",
                    ciclosAltaTemperatura, LIMITE_TEMPERATURA, temperatura);
            status = StatusManutencao.ALERTA_TEMPERATURA;
        }

        return status;
    }
}