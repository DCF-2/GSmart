package functrendz;

import java.util.Locale;

/**
 * Classe responsável por calcular e apresentar métricas de eficiência
 * do processo de fabricação.
 */
public class AnaliseEficiencia {

    private static double totalMassaConsumida = 0.0;
    private static double totalMassaReprocessada = 0.0;
    private static double totalEnergiaConsumida_kWh = 0.0;

    /**
     * Registra os dados de um ciclo de produção para análise posterior.
     * @param massaConsumidaNesteCiclo A quantidade de massa consumida.
     * @param massaReprocessadaNesteCiclo A quantidade de massa que virou reprocesso.
     * @param energiaConsumidaNesteCiclo_kWh A energia gasta no ciclo.
     */
    public static void registrarCiclo(double massaConsumidaNesteCiclo, double massaReprocessadaNesteCiclo, double energiaConsumidaNesteCiclo_kWh) {
        totalMassaConsumida += massaConsumidaNesteCiclo;
        totalMassaReprocessada += massaReprocessadaNesteCiclo;
        totalEnergiaConsumida_kWh += energiaConsumidaNesteCiclo_kWh;
    }

    /**
     * Calcula a eficiência do uso de material (rendimento).
     * @return O percentual de eficiência de material.
     */
    public static double calcularEficienciaMaterial() {
        if (totalMassaConsumida == 0) {
            return 0.0; // Evita divisão por zero
        }
        double massaUtil = totalMassaConsumida - totalMassaReprocessada;
        return (massaUtil / totalMassaConsumida) * 100.0;
    }

    /**
     * Calcula a eficiência energética do processo.
     * @return A quantidade de massa produzida por kWh de energia.
     */
    public static double calcularEficienciaEnergetica() {
        if (totalEnergiaConsumida_kWh == 0) {
            return 0.0; // Evita divisão por zero
        }
        return totalMassaConsumida / totalEnergiaConsumida_kWh;
    }

    /**
     * Exibe um relatório formatado com os KPIs de eficiência.
     */
    public static void exibirRelatorioEficiencia() {
        System.out.printf(Locale.US, "[EFICIÊNCIA DE MATERIAL] Rendimento: %.2f%%%n", calcularEficienciaMaterial());
        System.out.printf(Locale.US, "[EFICIÊNCIA ENERGÉTICA] Produção: %.2f kg por kWh%n", calcularEficienciaEnergetica());
    }

    /**
     * Reseta os contadores para iniciar um novo período de análise.
     */
    public static void resetarAnalise() {
        totalMassaConsumida = 0.0;
        totalMassaReprocessada = 0.0;
        totalEnergiaConsumida_kWh = 0.0;
        System.out.println("[SETUP] Análise de eficiência resetada.");
    }
}