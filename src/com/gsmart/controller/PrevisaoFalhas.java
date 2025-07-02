// Localização: src/main/java/functrendz/PrevisaoFalhas.java
package com.gsmart.controller;

import com.gsmart.resources.GSmartListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PrevisaoFalhas {
    private final List<Double> historicoTemperatura = new ArrayList<>();
    private final List<Double> historicoFatorPotencia = new ArrayList<>();
    private final List<Double> historicoPotenciaAtiva = new ArrayList<>();

    private static final int JANELA_ANALISE = 10;
    private static final double MULTIPLICADOR_DESVIO = 2.0;

    public  void registrarMetricas(double temperatura, double fatorPotencia, double potenciaAtiva) {
        if (temperatura != 0.0) historicoTemperatura.add(temperatura);
        if (fatorPotencia != 0.0) historicoFatorPotencia.add(fatorPotencia);
        if (potenciaAtiva != 0.0) historicoPotenciaAtiva.add(potenciaAtiva);

        if (historicoTemperatura.size() > 100) historicoTemperatura.remove(0);
        if (historicoFatorPotencia.size() > 100) historicoFatorPotencia.remove(0);
        if (historicoPotenciaAtiva.size() > 100) historicoPotenciaAtiva.remove(0);
    }

    public  boolean preverFalhas(GSmartListener listener) {
        if (historicoTemperatura.size() < JANELA_ANALISE) {
            return false;
        }

        List<Double> ultimasTemperaturas = historicoTemperatura.subList(historicoTemperatura.size() - JANELA_ANALISE, historicoTemperatura.size());
        double mediaTemperatura = calcularMedia(ultimasTemperaturas);
        double desvioPadrao = calcularDesvioPadrao(ultimasTemperaturas, mediaTemperatura);
        double limiteSuperior = mediaTemperatura + MULTIPLICADOR_DESVIO * desvioPadrao;
        double limiteInferior = mediaTemperatura - MULTIPLICADOR_DESVIO * desvioPadrao;
        double ultimaTemperatura = ultimasTemperaturas.get(ultimasTemperaturas.size() - 1);

        if (ultimaTemperatura < limiteInferior || ultimaTemperatura > limiteSuperior) {
            String alerta = String.format(Locale.US, "Causa: Desvio anômalo de Temperatura detectado!\n- Última Leitura: %.2f°C\n- Média Recente: %.2f°C\n- Limites Aceitáveis: Entre %.2f°C e %.2f°C.",
                    ultimaTemperatura, mediaTemperatura, limiteInferior, limiteSuperior);
            listener.onAlert("Alerta de Previsão de Falha", alerta);
            return true;
        }
        return false;
    }

    private static double calcularMedia(List<Double> valores) {
        return valores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    private static double calcularDesvioPadrao(List<Double> valores, double media) {
        double somaQuadrados = valores.stream().mapToDouble(valor -> Math.pow(valor - media, 2)).sum();
        return Math.sqrt(somaQuadrados / valores.size());
    }
}