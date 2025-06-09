package functrendz;
// functrendz/Manutencao.java

public class Manutencao {
    private static int ciclosBaixoFator = 0;
    private static int ciclosAltaTemperatura = 0;
    private static final double LIMITE_FATOR_POTENCIA = 0.90; // Um pouco mais rigoroso
    private static final double LIMITE_TEMPERATURA = 75.0;

    // Enum para representar os possíveis status de manutenção
    public enum StatusManutencao {
        OK,
        ALERTA_FATOR_POTENCIA,
        ALERTA_TEMPERATURA
    }

    /**
     * Verifica a necessidade de manutenção e RETORNA o status.
     * @param fatorPotencia Fator de potência atual.
     * @param temperatura Temperatura atual.
     * @return O status de manutenção (OK, ALERTA_FATOR_POTENCIA, ALERTA_TEMPERATURA).
     */
    public static StatusManutencao verificarManutencao(double fatorPotencia, double temperatura) {
        StatusManutencao status = StatusManutencao.OK;

        if (fatorPotencia < LIMITE_FATOR_POTENCIA) {
            ciclosBaixoFator++;
        } else {
            ciclosBaixoFator = 0;
        }

        if (ciclosBaixoFator >= 3) {
            System.out.println("[ALERTA MANUTENÇÃO] Baixo fator de potência detectado por 3 ciclos. Agendar manutenção!");
            status = StatusManutencao.ALERTA_FATOR_POTENCIA;
        }

        if (temperatura > LIMITE_TEMPERATURA) {
            ciclosAltaTemperatura++;
        } else {
            ciclosAltaTemperatura = 0;
        }

        if (ciclosAltaTemperatura >= 5) {
            System.out.println("[ALERTA MANUTENÇÃO] Alta temperatura detectada por 5 ciclos. Agendar manutenção!");
            // Se já havia um alerta de fator de potência, o de temperatura é mais crítico
            status = StatusManutencao.ALERTA_TEMPERATURA;
        }

        return status;
    }
}