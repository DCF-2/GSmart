import thingsboard.SimuladordeDispositivo;
import conectiontingsboard.ExportacaoDadosPWBI;
import functrendz.FuncTrendZ;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        // Configurações iniciais
        String deviceToken = "Bj8Fb7s9Oxj8n2GblelN"; // Token do dispositivo no ThingsBoard
        Random rand = new Random();

        // Thread para simular envio contínuo de dados
        Thread simuladorThread = new Thread(() -> {
            int contador = 0; // Identificar cada ciclo
            while (true) {
                try {
                    contador++;
                    // Geração de dados aleatórios (igual ao simulador)
                    double Tensao = 200 + rand.nextInt(100) - 50;
                    double Corrente = 30 + rand.nextInt(20) - 10;
                    double PotenciaAtiva = 100 + rand.nextInt(80) - 40;
                    double PotenciaReativa = 50 + rand.nextInt(60) - 30;
                    double Fator_Potencia = 0.3 + rand.nextDouble() * 0.7;
                    double temperature = 10 + rand.nextInt(80);

                    // Dados em formato JSON para o ThingsBoard
                    String jsonData = "{"
                            + "\"Tensao\": " + Tensao + ","
                            + "\"Corrente\": " + Corrente + ","
                            + "\"PotenciaAtiva\": " + PotenciaAtiva + ","
                            + "\"PotenciaReativa\": " + PotenciaReativa + ","
                            + "\"Fator_Potencia\": " + Fator_Potencia + ","
                            + "\"temperature\": " + temperature
                            + "}";

                    // Enviar os dados simulados ao ThingsBoard
                    SimuladordeDispositivo.sendToThingsBoard(deviceToken, jsonData);
                    //System.out.println("Dados enviados com sucesso para o ThingsBoard!");

                    // Registrar o consumo no FuncTrendZ
                    FuncTrendZ.registrarConsumo(PotenciaAtiva);

                    // Consolidar os dados para envio ao Power BI
                    Map<String, Double> metrics = new HashMap<>();
                    metrics.put("Tensao", Tensao);
                    metrics.put("Corrente", Corrente);
                    metrics.put("PotenciaAtiva", PotenciaAtiva);
                    metrics.put("PotenciaReativa", PotenciaReativa);
                    metrics.put("Fator_Potencia", Fator_Potencia);
                    metrics.put("Temperatura", temperature);

                    // Enviar todos os dados ao Power BI de uma vez
                    Instant now = Instant.now();
                    ExportacaoDadosPWBI.sendAllMetricsToPowerBI(metrics, now);

                    // Exibir dados acumulados a cada 10 ciclos
                    if (contador % 10 == 0) {
                        double consumoTotal = FuncTrendZ.getHistoricoConsumo().stream().mapToDouble(Double::doubleValue).sum();
                        double custoTotal = FuncTrendZ.calcularCusto();
                        System.out.printf("Ciclo %d concluído. Consumo acumulado: %.2f kWh, Custo total: R$ %.2f.%n",
                                contador, consumoTotal, custoTotal);
                    }

                    // Aguardar 10 segundos antes do próximo envio
                    System.out.printf("Ciclo %d concluído. Aguardando 10 segundos...%n", contador);
                    Thread.sleep(10000);
                } catch (Exception e) {
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


