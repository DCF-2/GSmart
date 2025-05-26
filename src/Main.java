import thingsboard.SimuladordeDispositivo;
import conectiontingsboard.ExportacaoDadosPWBI;
import conectiontingsboard.ThingsBoardAPI;

import com.google.gson.JsonObject;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        // Configurações iniciais
        String deviceToken = "Bj8Fb7s9Oxj8n2GblelN";
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

                    // Enviar os dados
                    SimuladordeDispositivo.sendToThingsBoard(deviceToken, jsonData);

                    // **AQUI COMEÇA A PARTE REAL-TIME**:
                    // Logo após o envio, já buscar e exportar:
                    try {
                        String token = ThingsBoardAPI.getToken();
                        JsonObject data = ThingsBoardAPI.fetchData(token);
                        ExportacaoDadosPWBI.exportToCSV(data);
                    } catch (Exception ex) {
                        System.err.println("Erro no export real-time: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                    // **FIM DA PARTE REAL-TIME**

                    // Esperar 10 segundos antes do próximo envio
                    Thread.sleep(10000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        simuladorThread.start();

        /*
        // Se ainda quiser manter o export a cada 60s, descomente este bloco:
        while (true) {
            try {
                Thread.sleep(60000); // 60 segundos

                // Obter token e dados do ThingsBoard
                String token = ThingsBoardAPI.getToken();
                JsonObject data = ThingsBoardAPI.fetchData(token);

                // Exportar para CSV
                ExportacaoDadosPWBI.exportToCSV(data);

            } catch (Exception e) {
                System.err.println("Erro ao exportar dados: " + e.getMessage());
                e.printStackTrace();
            }
        }
        */
    }
}
