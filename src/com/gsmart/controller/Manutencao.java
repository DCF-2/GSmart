// Localização: src/main/java/functrendz/Manutencao.java
package com.gsmart.controller;

import com.gsmart.resources.GSmartListener;
import java.util.Locale;

/**
 * Modela a lógica de negócio para a manutenção preditiva.
 *
 * Esta classe avalia as métricas de telemetria, como fator de potência e temperatura,
 * para determinar o estado de manutenção de um equipamento ou sistema. Ela pode
 * acionar alertas através do {@code GSmartListener} quando os limiares de
 * manutenção são atingidos.
 */
public class Manutencao {
    private int ciclosBaixoFator = 0;
    private int ciclosAltaTemperatura = 0;
    private static final double LIMITE_FATOR_POTENCIA = 0.90;
    private static final double LIMITE_TEMPERATURA = 75.0;

    public enum StatusManutencao {
        OK,
        ALERTA_FATOR_POTENCIA,
        ALERTA_TEMPERATURA
    }

    public  StatusManutencao verificarManutencao(GSmartListener listener, double fatorPotencia, double temperatura) {
        StatusManutencao status = StatusManutencao.OK;

        if (fatorPotencia < LIMITE_FATOR_POTENCIA && fatorPotencia != 0.0) {
            ciclosBaixoFator++;
        } else {
            ciclosBaixoFator = 0;
        }

        if (ciclosBaixoFator >= 3) {
            String alerta = String.format(Locale.US, "Causa: Baixo Fator de Potência persistente por %d ciclos.\n- Última Leitura: %.2f (Limite: > %.2f)\n\nAção Recomendada:\nAgendar verificação da rede elétrica ou banco de capacitores.",
                    ciclosBaixoFator, fatorPotencia, LIMITE_FATOR_POTENCIA);
            listener.onAlert("Alerta de Manutenção Preventiva", alerta);
            status = StatusManutencao.ALERTA_FATOR_POTENCIA;
        }

        if (temperatura > LIMITE_TEMPERATURA) {
            ciclosAltaTemperatura++;
        } else {
            ciclosAltaTemperatura = 0;
        }

        if (ciclosAltaTemperatura >= 5) {
            String alerta = String.format(Locale.US, "Causa: Superaquecimento persistente por %d ciclos.\n- Última Leitura: %.1f°C (Limite: < %.1f°C)\n\nAção Recomendada:\nAgendar manutenção IMEDIATA do sistema de refrigeração.",
                    ciclosAltaTemperatura, temperatura, LIMITE_TEMPERATURA);
            listener.onAlert("Alerta de Manutenção Crítica", alerta);
            status = StatusManutencao.ALERTA_TEMPERATURA;
        }

        return status;
    }
}