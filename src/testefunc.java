import functrendz.FuncTrendZ;

class testfunc{

    public static void main(String[] args) {
        System.out.println("=== Testando Verificar Manutenção ===");
        double[] fatoresPotencia = {0.5, 0.4, 0.3, 0.7, 0.5, 0.3, 0.4, 0.6, 0.2, 0.8, 0.3};
        double[] temperaturas = {70.0, 72.0, 80.0, 85.0, 78.0, 90.0, 76.0, 88.0, 95.0, 68.0, 10.0};


        for (int i = 0; i < fatoresPotencia.length; i++) {
            System.out.printf("Ciclo %d: Fator de Potência: %.2f, Temperatura: %.1f°C%n",
                    i + 1, fatoresPotencia[i], temperaturas[i]);
            FuncTrendZ.verificarManutencao(fatoresPotencia[i], temperaturas[i]);
        }

        System.out.println("\n=== Testando Prever Falhas ===");
        double[] potenciasAtivas = {120, 150, 500, 160, 800, 300, 100, 200, 400, 250, 900};
        double[] fatoresPotenciaFalha = {0.9, 0.7, 0.2, 0.4, 0.1, 0.2, 0.3, 0.5, 0.4, 0.6, 1.5};// Valores simulando quedas bruscas

        for (int i = 0; i < potenciasAtivas.length; i++) {
            System.out.printf("Ciclo %d -> ", i + 1);
            if (i < potenciasAtivas.length -1) {
                System.out.printf("Ciclo %d: Potência Ativa: %.0f -> %.0f, Fator de Potência: %.2f -> %.2f%n",
                        i + 1, potenciasAtivas[i], potenciasAtivas[i + 1],
                        fatoresPotenciaFalha[i], fatoresPotenciaFalha[i + 1]);

                // Registrar métricas
                FuncTrendZ.registrarMetricas(temperaturas[i], fatoresPotenciaFalha[i], potenciasAtivas[i]);

                // Prever falhas
                boolean falhaDetectada = FuncTrendZ.preverFalhas();
                if (falhaDetectada) {
                    System.out.println("ALERTA: Falha detectada no sistema! Investigar imediatamente.");
                }
            }
        }
    }
}
