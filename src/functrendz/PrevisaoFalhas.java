package functrendz;

import java.util.ArrayList;
import java.util.List;

public class PrevisaoFalhas {
    private static final List<Double> historicoTemperatura = new ArrayList<>();
    private static final List<Double> historicoFatorPotencia = new ArrayList<>();
    private static final List<Double> historicoPotenciaAtiva = new ArrayList<>();
    private static final double LIMITE_DESVIO_PADRAO = 1.5;

    public static void registrarMetricas(double temperatura, double fatorPotencia, double potenciaAtiva) {
        historicoTemperatura.add(temperatura);
        historicoFatorPotencia.add(fatorPotencia);
        historicoPotenciaAtiva.add(potenciaAtiva);

        if (historicoTemperatura.size() > 100) historicoTemperatura.remove(0);
        if (historicoFatorPotencia.size() > 100) historicoFatorPotencia.remove(0);
        if (historicoPotenciaAtiva.size() > 100) historicoPotenciaAtiva.remove(0);
    }

    public static boolean preverFalhas() {
        if (historicoTemperatura.size() < 3) {
            System.out.println("Dados insuficientes para análise. Aguardando mais ciclos.");
            return false;
        }

        List<Double> ultimasTemperaturas = historicoTemperatura.subList(historicoTemperatura.size() - 3, historicoTemperatura.size());
        List<Double> ultimosFatoresPotencia = historicoFatorPotencia.subList(historicoFatorPotencia.size() - 3, historicoFatorPotencia.size());
        List<Double> ultimasPotenciasAtivas = historicoPotenciaAtiva.subList(historicoPotenciaAtiva.size() - 3, historicoPotenciaAtiva.size());

        double mediaTemperatura = calcularMedia(ultimasTemperaturas);
        double desvioTemperatura = calcularDesvioPadrao(ultimasTemperaturas, mediaTemperatura);
        double mediaFatorPotencia = calcularMedia(ultimosFatoresPotencia);
        double desvioFatorPotencia = calcularDesvioPadrao(ultimosFatoresPotencia, mediaFatorPotencia);
        double mediaPotenciaAtiva = calcularMedia(ultimasPotenciasAtivas);
        double desvioPotenciaAtiva = calcularDesvioPadrao(ultimasPotenciasAtivas, mediaPotenciaAtiva);

        double limiteInferiorTemperatura = mediaTemperatura - 0.8 * desvioTemperatura;
        double limiteSuperiorTemperatura = mediaTemperatura + 0.8 * desvioTemperatura;

        double ultimaTemperatura = ultimasTemperaturas.get(ultimasTemperaturas.size() - 1);

        if (ultimaTemperatura < limiteInferiorTemperatura || ultimaTemperatura > limiteSuperiorTemperatura) {
            System.out.println("ALERTA: Falha detectada! Investigação necessária.");
            return true;
        }

        System.out.println("Nenhuma falha detectada no momento.");
        return false;
    }

    private static double calcularMedia(List<Double> valores) {
        double soma = 0.0;
        for (double valor : valores) {
            soma += valor;
        }
        return soma / valores.size();
    }

    private static double calcularDesvioPadrao(List<Double> valores, double media) {
        double somaQuadrados = 0.0;
        for (double valor : valores) {
            somaQuadrados += Math.pow(valor - media, 2);
        }
        return Math.sqrt(somaQuadrados / valores.size());
    }
}
