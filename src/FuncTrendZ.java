import java.util.Random;

// Importa estaticamente os métodos de todas as classes de funcionalidade
import static functrendz.GerenciamentoRecurso.*;
import static functrendz.ConsumoEnergia.*;
import static functrendz.Manutencao.*;
import static functrendz.PrevisaoFalhas.*;

/**
 * Classe principal que orquestra uma simulação de processo industrial,
 * integrando gerenciamento de recursos, consumo de energia, manutenção
 * e previsão de falhas.
 */
public class FuncTrendZ {

    private static final Random random = new Random();

    /**
     * Ponto de entrada da simulação.
     * @param args Argumentos de linha de comando (não utilizados).
     * @throws InterruptedException Se a thread for interrompida durante o sleep.
     */
    public static void main(String[] args) throws InterruptedException {

        System.out.println("🚀 INICIANDO SIMULAÇÃO DE PROCESSO INDUSTRIAL - FUNCTRENDZ 🚀");
        System.out.println("================================================================");

        // --- 1. CONFIGURAÇÃO INICIAL ---
        System.out.println("\n[SETUP] Configurando recursos iniciais...");
        injetarMassa(500, false); // Carga inicial de massa nova
        resetarHistorico(); // Reseta o histórico de consumo de energia

        // --- 2. CICLO DE SIMULAÇÃO ---
        int totalCiclos = 15;
        System.out.printf("\n[SIMULAÇÃO] Executando %d ciclos de produção...\n", totalCiclos);

        for (int ciclo = 1; ciclo <= totalCiclos; ciclo++) {
            System.out.printf("\n--- CICLO DE PRODUÇÃO Nº %d ---\n", ciclo);

            // Gerar métricas simuladas para o ciclo atual
            double consumoMassa = gerarValorAleatorio(20.0, 35.0);
            double potenciaAtiva = gerarValorAleatorio(80.0, 95.0); // em kW
            double fatorPotencia = gerarValorAleatorio(0.85, 0.95);
            double temperatura = gerarValorAleatorio(65.0, 72.0);

            // -> INTRODUZINDO ANOMALIAS PARA TESTAR ALERTAS <-
            if (ciclo >= 5 && ciclo <= 7) {
                System.out.println("[ANOMALIA] Simulando baixo fator de potência...");
                fatorPotencia = 0.55; // Força baixo fator de potência por 3 ciclos
            }
            if (ciclo > 10) {
                System.out.println("[ANOMALIA] Simulando superaquecimento...");
                temperatura = 80.0; // Força alta temperatura
            }
            if (ciclo == 13) {
                System.out.println("[ANOMALIA] Simulando desvio brusco de temperatura para teste de falha...");
                temperatura = 95.0; // Força desvio para previsão de falha
            }


            // 1. Gerenciamento de Recurso
            registrarConsumoRecurso(consumoMassa);

            // 2. Consumo de Energia
            // Aguarda um tempo para simular a duração do ciclo
            Thread.sleep(100); // 100 milissegundos
            registrarConsumo(potenciaAtiva);

            // 3. Registrar Métricas para Análise
            registrarMetricas(temperatura, fatorPotencia, potenciaAtiva);
            System.out.printf("[MÉTRICAS] Temp: %.1f°C, Fator Potência: %.2f, Potência: %.1f kW%n", temperatura, fatorPotencia, potenciaAtiva);


            // 4. Verificação de Manutenção
            verificarManutencao(fatorPotencia, temperatura);

            // 5. Previsão de Falhas
            if (preverFalhas()) {
                System.out.println("!!! PARADA DE EMERGÊNCIA: FALHA CRÍTICA PREVISTA. SIMULAÇÃO INTERROMPIDA. !!!");
                break; // Interrompe a simulação se uma falha crítica for detectada
            }
        }

        // --- 3. RELATÓRIO FINAL ---
        System.out.println("\n================================================================");
        System.out.println("🏁 FIM DA SIMULAÇÃO 🏁");
        System.out.println("----------------------------------------------------------------");

        System.out.println("\n[RELATÓRIO FINAL DE RECURSOS]");
        exibirStatusRecurso();

        System.out.println("\n[RELATÓRIO FINAL DE ENERGIA]");
        System.out.printf("Custo total de energia estimado: R$ %.2f%n", calcularCusto());
        System.out.println("================================================================");
    }

    /**
     * Gera um valor aleatório do tipo double dentro de um intervalo.
     * Esta função corrige o erro da falta de uma função para gerar valores aleatórios.
     *
     * @param min O valor mínimo do intervalo.
     * @param max O valor máximo do intervalo.
     * @return Um valor double aleatório.
     */
    public static double gerarValorAleatorio(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }
}