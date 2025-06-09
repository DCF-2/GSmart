package functrendz;

public class GerenciamentoRecurso {
    private static double quantidadeInicialRecurso = 1000.0;
    private static double quantidadeAtualRecurso = quantidadeInicialRecurso;
    private static double quantidadeReprocessada = 0.0;
    private static final double LIMITE_ALERTA = 100.0;

    public static void registrarConsumoRecurso(double consumo) {
        if (consumo <= 0) {
            System.out.println("Consumo inválido.");
            return;
        }

        if (consumo > quantidadeAtualRecurso) {
            System.out.println("Consumo excede a quantidade disponível.");
            consumo = quantidadeAtualRecurso;
        }

        quantidadeAtualRecurso -= consumo;
        System.out.printf("Consumo registrado: %.2f kg. Quantidade restante: %.2f kg.%n", consumo, quantidadeAtualRecurso);

        if (quantidadeAtualRecurso <= LIMITE_ALERTA) {
            System.out.println("ALERTA: Massa de biscoito está próxima de acabar! Verifique o estoque.");
        }
    }

    public static void injetarMassa(double quantidade, boolean reprocessada) {
        if (quantidade <= 0) {
            System.out.println("Quantidade inválida para injeção.");
            return;
        }

        quantidadeAtualRecurso += quantidade;

        if (reprocessada) {
            quantidadeReprocessada += quantidade;
            System.out.printf("Massa reprocessada adicionada: %.2f kg.%n", quantidade);
        } else {
            System.out.printf("Massa nova adicionada: %.2f kg.%n", quantidade);
        }

        System.out.printf("Quantidade total disponível: %.2f kg.%n", quantidadeAtualRecurso);
        System.out.printf("Quantidade total reprocessada: %.2f kg.%n", quantidadeReprocessada);
    }

    public static void exibirStatusRecurso() {
        System.out.printf("Recurso disponível: %.2f kg de massa de biscoito.%n", quantidadeAtualRecurso);
        System.out.printf("Recurso reprocessado: %.2f kg de massa.%n", quantidadeReprocessada);
    }
}
