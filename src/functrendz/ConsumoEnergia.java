package functrendz;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ConsumoEnergia {
    private static final double TARIFA_ENERGIA = 0.50; // Tarifa em R$/kWh
    private static List<Double> historicoConsumo = new ArrayList<>();
    private static Instant inicioExecucao = Instant.now();

    public static void registrarConsumo(double potencia) {
        Instant agora = Instant.now();
        Duration duracao = Duration.between(inicioExecucao, agora);
        double segundos = duracao.toMillis() / 1000.0; // Tempo em segundos

        double consumo = (potencia / 1000) * (segundos / 3600.0); // Consumo em kWh
        historicoConsumo.add(consumo);
        inicioExecucao = agora;

        System.out.printf("Consumo registrado: %.6f kWh%n", consumo);
    }

    public static double calcularCusto() {
        double consumoTotal = historicoConsumo.stream().mapToDouble(Double::doubleValue).sum();
        return consumoTotal * TARIFA_ENERGIA;
    }

    public static List<Double> getHistoricoConsumo() {
        return historicoConsumo;
    }

    public static void resetarHistorico() {
        historicoConsumo.clear();
        inicioExecucao = Instant.now();
        System.out.println("Histórico de consumo resetado.\n");
    }
}