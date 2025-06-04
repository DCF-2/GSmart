package functrendz;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class FuncTrendZ {

    // Estrutura para armazenar os dados históricos de consumo
    private static List<Double> historicoConsumo = new ArrayList<>();
    private static Instant inicioExecucao = Instant.now();
    private static final double TARIFA_ENERGIA = 0.50; // Tarifa em R$/kWh

    /**
     * Registra o consumo com base na potência e no tempo de execução.
     * @param potencia Potência do dispositivo em watts.
     */
    public static void registrarConsumo(double potencia) {
        Instant agora = Instant.now();
        Duration duracao = Duration.between(inicioExecucao, agora);
        double segundos = duracao.toMillis() / 1000.0; // Tempo em segundos

        // Calcular o consumo em kWh
        double consumo = (potencia / 1000) * (segundos / 3600.0); // Segundos convertidos para horas

        // Atualizar o histórico
        historicoConsumo.add(consumo);
        inicioExecucao = agora;

        // Exibir consumo atualizado
        System.out.printf("Consumo registrado: %.6f kWh%n", consumo);
    }

    /**
     * Calcula o custo baseado no consumo total registrado.
     * @return Custo total em reais.
     */
    public static double calcularCusto() {
        double consumoTotal = historicoConsumo.stream().mapToDouble(Double::doubleValue).sum();
        return consumoTotal * TARIFA_ENERGIA;
    }

    /**
     * Recupera o histórico de consumo.
     * @return Lista de consumos registrados em kWh.
     */
    public static List<Double> getHistoricoConsumo() {
        return historicoConsumo;
    }

    /**
     * Reseta o histórico de consumo (usado para testes ou reinicialização).
     */
    public static void resetarHistorico() {
        historicoConsumo.clear();
        inicioExecucao = Instant.now();
        System.out.println("Histórico de consumo resetado.");
    }
}
