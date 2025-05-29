import thingsboard.SimuladordeDispositivo;
import conectiontingsboard.ExportacaoDadosPWBI;
// import conectiontingsboard.ThingsBoardAPI;

import java.time.Instant;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        // Configurações iniciais
        String deviceToken = "Bj8Fb7s9Oxj8n2GblelN"; // Token do dispositivo no ThingsBoard
        Random rand = new Random();

        // Thread para simular envio contínuo de dados
        Thread simuladorThread = new Thread(() -> {
            while (true) {
                try {
                    // Geração de dados aleatórios (igual ao simulador)
                    double Tensao = 200 + rand.nextInt(100) - 50;
                    double Corrente = 30 + rand.nextInt(20) - 10;
                    double PotenciaAtiva = 100 + rand.nextInt(80) - 40;
                    double PotenciaReativa = 50 + rand.nextInt(60) - 30;
                    double Fator_Potencia = 0.3 + rand.nextDouble() * 0.7;
                    double temperature = 10 + rand.nextInt(80);

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

                    // Enviar os dados para o Power BI
                    Instant now = Instant.now();

                    ExportacaoDadosPWBI.sendFormattedToPowerBI("Tensao", Tensao, now);
                    ExportacaoDadosPWBI.sendFormattedToPowerBI("Corrente", Corrente, now);
                    ExportacaoDadosPWBI.sendFormattedToPowerBI("PotenciaAtiva", PotenciaAtiva, now);
                    ExportacaoDadosPWBI.sendFormattedToPowerBI("PotenciaReativa", PotenciaReativa, now);
                    ExportacaoDadosPWBI.sendFormattedToPowerBI("Fator_Potencia", Fator_Potencia, now);
                    ExportacaoDadosPWBI.sendFormattedToPowerBI("Temperatura", temperature, now);

                    // Aguardar 10 segundos antes do próximo envio
                    Thread.sleep(10000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        simuladorThread.start();

        /*
        // FUNCIONALIDADE ANTIGA: Exportar CSV a cada 60 segundos (inutilizada)
        // import conectiontingsboard.ThingsBoardAPI;
        // while (true) {
        //     try {
        //         Thread.sleep(60000); // 60 segundos
        //
        //         // Obter token e buscar dados do ThingsBoard
        //         String token = ThingsBoardAPI.getToken();
        //         JsonObject data = ThingsBoardAPI.fetchData(token);
        //
        //         // Exportar dados para o arquivo CSV
        //         ExportacaoDadosPWBI.exportToCSV(data);
        //
        //     } catch (Exception e) {
        //         System.err.println("Erro ao exportar dados: " + e.getMessage());
        //         e.printStackTrace();
        //     }
        // }
        */
    }
}
