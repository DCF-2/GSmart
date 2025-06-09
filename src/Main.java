import thingsboard.SimuladordeDispositivo;
import conectiontingsboard.ExportacaoDadosPWBI;
import static functrendz.GerenciamentoRecurso.*;
import static functrendz.ConsumoEnergia.*;
import static functrendz.Manutencao.*;
import static functrendz.PrevisaoFalhas.*;
import static functrendz.AnaliseEficiencia.*;
import static functrendz.GeradorDeInsights.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class Main {

    public static void main(String[] args) {
        // --- 1. CONFIGURAÇÕES INICIAIS ---
        String deviceToken = "Bj8Fb7s9Oxj8n2GblelN";
        Random rand = new Random();
        final double TEMPO_CICLO_EM_SEGUNDOS = 10.0;

        System.out.println("🚀 INICIANDO GETSMART - SIMULADOR INDUSTRIAL INTEGRADO v4.0 🚀");
        System.out.println("[SETUP] Configurando serviços e recursos iniciais...");
        injetarMassa(500, false);
        resetarHistorico();
        resetarAnalise();
        System.out.println("-----------------------------------------------------------------");

        // --- 2. THREAD DE SIMULAÇÃO CONTÍNUA ---
        Thread simuladorThread = new Thread(() -> {
            int contador = 0;
            while (true) {
                try {
                    contador++;
                    System.out.printf("\n--- Iniciando Ciclo de Simulação Nº %d ---\n", contador);

                    // a. Geração de dados do processo
                    double Tensao = 200 + rand.nextInt(100) - 50;
                    double Corrente = 30 + rand.nextInt(20) - 10;
                    double PotenciaAtiva = 100 + rand.nextInt(80) - 40;
                    double PotenciaReativa = 50 + rand.nextInt(60) - 30;
                    double Fator_Potencia = 0.85 + rand.nextDouble() * 0.1;
                    double temperature = 65 + rand.nextInt(20);
                    double consumoMassa = 20 + rand.nextDouble() * 15;
                    double massaReprocessadaCiclo = consumoMassa * (rand.nextDouble() * 0.05);

                    // b. Módulos de Execução
                    registrarConsumoRecurso(consumoMassa);
                    registrarConsumo(PotenciaAtiva);
                    double tempoCicloEmHoras = TEMPO_CICLO_EM_SEGUNDOS / 3600.0;
                    double energiaConsumidaCiclo = (PotenciaAtiva / 1000) * tempoCicloEmHoras;
                    registrarCiclo(consumoMassa, massaReprocessadaCiclo, energiaConsumidaCiclo);

                    // c. Módulos de Análise
                    StatusManutencao statusManutencao = verificarManutencao(Fator_Potencia, temperature);
                    registrarMetricas(temperature, Fator_Potencia, PotenciaAtiva);
                    boolean falhaDetectada = preverFalhas();

                    // Captura das novas métricas de eficiência para o alarme
                    double eficienciaMaterial = calcularEficienciaMaterial();

                    // vvV ADICIONE OU VERIFIQUE ESTA LINHA vvv
                    eficienciaMaterial = 92.5; // FORÇAR VALOR BAIXO PARA TESTE
                    // ^^^ ADICIONE OU VERIFIQUE ESTA LINHA ^^^

                    double eficienciaEnergetica = calcularEficienciaEnergetica();


                    // d. Geração de Insights
                    System.out.println("\n[ANÁLISE E DIAGNÓSTICO DO CICLO]");
                    gerarInsightsDoCiclo(eficienciaMaterial, Fator_Potencia, temperature, statusManutencao, falhaDetectada);

                    // e. Envio de Dados para Plataformas Externas
                    // A linha abaixo já inclui "eficienciaMaterial" para acionar seu alarme
                    String jsonData = String.format(Locale.US,
                            "{\"Tensao\":%.2f, \"Corrente\":%.2f, \"PotenciaAtiva\":%.2f, \"PotenciaReativa\":%.2f, \"Fator_Potencia\":%.2f, \"temperature\":%.2f, \"consumoMassa\":%.2f, \"massaReprocessada\":%.2f, \"eficienciaMaterial\":%.2f, \"eficienciaEnergetica\":%.2f, \"previsaoFalha\":%b}",
                            Tensao, Corrente, PotenciaAtiva, PotenciaReativa, Fator_Potencia, temperature, consumoMassa, massaReprocessadaCiclo, eficienciaMaterial, eficienciaEnergetica, falhaDetectada
                    );
                    SimuladordeDispositivo.sendToThingsBoard(deviceToken, jsonData);

                    Map<String, Double> metrics = new HashMap<>();
                    metrics.put("Tensao", Tensao);
                    metrics.put("Corrente", Corrente);
                    metrics.put("PotenciaAtiva", PotenciaAtiva);
                    metrics.put("PotenciaReativa", PotenciaReativa);
                    metrics.put("Fator_Potencia", Fator_Potencia);
                    metrics.put("Temperatura", temperature);
                    metrics.put("ConsumoMassa", consumoMassa);
                    metrics.put("MassaReprocessada", massaReprocessadaCiclo);
                    metrics.put("EficienciaMaterial", eficienciaMaterial);
                    metrics.put("EficienciaEnergetica", eficienciaEnergetica);
                    metrics.put("PrevisaoFalha", falhaDetectada ? 1.0 : 0.0);
                    ExportacaoDadosPWBI.sendAllMetricsToPowerBI(metrics, Instant.now());

                    // f. Relatório Periódico
                    if (contador % 10 == 0) {
                        System.out.println("\n--- RELATÓRIO PERIÓDICO (FIM DO CICLO " + contador + ") ---");
                        double consumoTotalEnergia = getHistoricoConsumo().stream().mapToDouble(Double::doubleValue).sum();
                        double custoTotal = calcularCusto();
                        System.out.printf(Locale.US, "[ENERGIA] Consumo para custo: %.2f kWh | Custo total: R$ %.2f%n", consumoTotalEnergia, custoTotal);
                        System.out.print("[RECURSO] ");
                        exibirStatusRecurso();
                        exibirRelatorioEficiencia();
                        System.out.println("----------------------------------------------------");
                    }

                    // g. Aguardar próximo ciclo
                    System.out.printf("\n>> Ciclo %d concluído. Aguardando %.0f segundos...%n", contador, TEMPO_CICLO_EM_SEGUNDOS);
                    Thread.sleep((long) (TEMPO_CICLO_EM_SEGUNDOS * 1000));

                } catch (Exception e) {
                    System.err.println("Ocorreu um erro na thread do simulador: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        simuladorThread.start();
    }
}

/*
         * FUNCIONALIDADE ANTIGA: Exportar CSV a cada 60 segundos (inutilizada)
         * Este trecho foi desativado para evitar duplicação de envios ou funcionalidades desnecessárias.
         *
         * // while (true) {
         * //     try {
         * //         Thread.sleep(60000); // 60 segundos
         * //
         * //         // Obter token e buscar dados do ThingsBoard
         * //         String token = ThingsBoardAPI.getToken();
         * //         JsonObject data = ThingsBoardAPI.fetchData(token);
         * //
         * //         // Exportar dados para o arquivo CSV
         * //         ExportacaoDadosPWBI.exportToCSV(data);
         * //
         * //     } catch (Exception e) {
         * //         System.err.println("Erro ao exportar dados: " + e.getMessage());
         * //         e.printStackTrace();
         * //     }
         * // }
         */


