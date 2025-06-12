package functrendz;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PrevisaoFalhas {
    private static final List<Double> historicoTemperatura = new ArrayList<>();
    private static final List<Double> historicoFatorPotencia = new ArrayList<>();
    private static final List<Double> historicoPotenciaAtiva = new ArrayList<>();

    // Parâmetros ajustados para um alerta menos sensível e mais robusto
    private static final int JANELA_ANALISE = 10; // Analisando os últimos 10 ciclos, não 3
    private static final double MULTIPLICADOR_DESVIO = 2.0; // Usando 2 desvios padrão, um limiar estatístico mais comum

    public static void registrarMetricas(double temperatura, double fatorPotencia, double potenciaAtiva) {
        historicoTemperatura.add(temperatura);
        historicoFatorPotencia.add(fatorPotencia);
        historicoPotenciaAtiva.add(potenciaAtiva);

        // Limita o tamanho do histórico para não consumir memória indefinidamente
        if (historicoTemperatura.size() > 100) historicoTemperatura.remove(0);
        if (historicoFatorPotencia.size() > 100) historicoFatorPotencia.remove(0);
        if (historicoPotenciaAtiva.size() > 100) historicoPotenciaAtiva.remove(0);
    }

    public static boolean preverFalhas() {
        if (historicoTemperatura.size() < JANELA_ANALISE) { // Precisa de dados suficientes para começar
            return false;
        }

        List<Double> ultimasTemperaturas = historicoTemperatura.subList(historicoTemperatura.size() - JANELA_ANALISE, historicoTemperatura.size());

        double mediaTemperatura = calcularMedia(ultimasTemperaturas);
        double desvioTemperatura = calcularDesvioPadrao(ultimasTemperaturas, mediaTemperatura);

        double limiteInferiorTemperatura = mediaTemperatura - MULTIPLICADOR_DESVIO * desvioTemperatura;
        double limiteSuperiorTemperatura = mediaTemperatura + MULTIPLICADOR_DESVIO * desvioTemperatura;

        double ultimaTemperatura = ultimasTemperaturas.get(ultimasTemperaturas.size() - 1);

        if (ultimaTemperatura < limiteInferiorTemperatura || ultimaTemperatura > limiteSuperiorTemperatura) {
            System.out.println("\n--- ALERTA DE PREVISÃO DE FALHA (JAVA) ---");
            System.out.printf(Locale.US, """
                Causa: Desvio anômalo de Temperatura detectado!
                  - Última Leitura: %.2f°C
                  - Média Recente (%d ciclos): %.2f°C
                  - Limite Aceitável: Entre %.2f°C e %.2f°C
                ------------------------------------------%n""",
                    ultimaTemperatura, JANELA_ANALISE, mediaTemperatura, limiteInferiorTemperatura, limiteSuperiorTemperatura);
            return true;
        }
        return false;
    }

    /**
     * Calcula a média de uma lista de valores double.
     * @param valores A lista de números.
     * @return A média dos valores.
     */
    private static double calcularMedia(List<Double> valores) {
        return valores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    /**
     * Calcula o desvio padrão populacional de uma lista de valores.
     * @param valores A lista de números.
     * @param media A média pré-calculada dos valores.
     * @return O desvio padrão.
     */
    private static double calcularDesvioPadrao(List<Double> valores, double media) {
        double somaQuadrados = 0.0;
        for (double valor : valores) {
            somaQuadrados += Math.pow(valor - media, 2);
        }
        return Math.sqrt(somaQuadrados / valores.size());
    }
}