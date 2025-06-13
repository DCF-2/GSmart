import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import conectiontingsboard.ExportacaoDadosPWBI;
import conectiontingsboard.ThingsBoardAPI;
import functrendz.CalculoDeCusto;
import functrendz.GeradorDeInsights;
import functrendz.Manutencao;
import functrendz.PrevisaoFalhas;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class Main {
    private static double consumoAcumuladoNaHora = 0.0;
    // Define a hora de início do primeiro ciclo de acúmulo, truncada para a hora exata.
    private static ZonedDateTime horaDeInicioDoCiclo = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")).truncatedTo(ChronoUnit.HOURS);


    public static void main(String[] args) throws InterruptedException {
        final long INTERVALO_EXECUCAO_SEGUNDOS = 5;
        final ZoneId FUSO_HORARIO_BRASIL = ZoneId.of("America/Sao_Paulo");
        System.out.println("🚀 INICIANDO GETSMART - PROCESSADOR DE DADOS DE ENERGIA (v6) 🚀");
        System.out.println("-----------------------------------------------------------------");

        while (true) {
            try {
                ZonedDateTime horaAtualBrasil = ZonedDateTime.now(FUSO_HORARIO_BRASIL);
                System.out.printf("\n--- Iniciando novo ciclo de processamento em %s ---\n", horaAtualBrasil.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));


                // 1. EXTRAÇÃO
                System.out.println("[ETAPA 1/3] Buscando dados do ThingsBoard...");
                String token = ThingsBoardAPI.getToken();
                JsonObject telemetria = ThingsBoardAPI.fetchData(token);
                System.out.println("Dados recebidos com sucesso!");

                // Extraindo TODAS as 20 métricas do JSON de forma segura
                double consDiaP = getTelemetryAsDouble(telemetria, "ConsDiaP", 0.0);
                double consHora = getTelemetryAsDouble(telemetria, "ConsHora", 0.0);
                double consHoraP = getTelemetryAsDouble(telemetria, "ConsHoraP", 0.0);
                double totalHoraFechada = 0.0; // Variável para enviar o total da hora anterior

                // Verifica se a hora atual ultrapassou o fim do ciclo de 1 hora
                if (horaAtualBrasil.isAfter(horaDeInicioDoCiclo.plusHours(1))) {
                    System.out.println("!!! HORA CONCLUÍDA !!!");
                    System.out.println("Total de consumo acumulado na hora anterior: " + consumoAcumuladoNaHora);

                    // Guarda o total da hora fechada para ser enviado UMA VEZ ao Power BI
                    totalHoraFechada = consumoAcumuladoNaHora;

                    // Zera o acumulador para a nova hora
                    consumoAcumuladoNaHora = 0.0;

                    // Atualiza a hora de início para o início da próxima hora
                    horaDeInicioDoCiclo = horaDeInicioDoCiclo.plusHours(1);
                    System.out.println("Próximo ciclo de acúmulo se encerrará após: " + horaDeInicioDoCiclo.plusHours(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }

                // Acumula o valor lido nesta execução
                consumoAcumuladoNaHora += consHoraP;
                System.out.printf("Valor ConsHoraP lido: %.4f | Acumulado parcial da hora: %.4f\n", consHoraP, consumoAcumuladoNaHora);

                // <<< FIM DA LÓGICA DE ACÚMULO HORÁRIO >>>
                double eakwh = getTelemetryAsDouble(telemetria, "EAkWh", 0.0);
                long envio = getTelemetryAsLong(telemetria, "Envio", 0L);
                double erro = getTelemetryAsDouble(telemetria, "Erro", 0.0);
                String hdDev = getTelemetryAsString(telemetria, "HdDev", Instant.now().toString());
                double ia_n = getTelemetryAsDouble(telemetria, "Ia_n", 0.0);
                double ib_n = getTelemetryAsDouble(telemetria, "Ib_n", 0.0);
                double ic_n = getTelemetryAsDouble(telemetria, "Ic_n", 0.0);
                String nserie = getTelemetryAsString(telemetria, "NSerie", "N/A");
                double temperature = getTelemetryAsDouble(telemetria, "temperature", 70.0);
                double va_n = getTelemetryAsDouble(telemetria, "Va_n", 0.0);
                double vb_n = getTelemetryAsDouble(telemetria, "Vb_n", 0.0);
                double vc_n = getTelemetryAsDouble(telemetria, "Vc_n", 0.0);
                double vfreq = getTelemetryAsDouble(telemetria, "VFreq", 0.0);
                long wifi_rssi = getTelemetryAsLong(telemetria, "WiFi.RSSI", -90L);
                double ptot = getTelemetryAsDouble(telemetria, "Ptot", 0.0);
                double fatorPotencia = getTelemetryAsDouble(telemetria, "Fator_Potencia", 1.0);
                double va_b = getTelemetryAsDouble(telemetria, "Va_b", 0.0);
                double vb_c = getTelemetryAsDouble(telemetria, "Vb_c", 0.0);
                double vc_a = getTelemetryAsDouble(telemetria, "Vc_a", 0.0);

                // 2. TRANSFORMAÇÃO
                System.out.println("[ETAPA 2/3] Processando dados e gerando inteligência...");
                double custoPeriodo = CalculoDeCusto.calcularCustoDoPeriodo(eakwh);
                Manutencao.StatusManutencao statusManutencao = Manutencao.verificarManutencao(fatorPotencia, temperature);
                PrevisaoFalhas.registrarMetricas(temperature, fatorPotencia, ptot);
                boolean falhaPrevista = PrevisaoFalhas.preverFalhas();

                // 3. CARGA
                System.out.println("[ETAPA 3/3] Enviando dados para o Power BI...");
                JsonObject pbiPayload = new JsonObject();
                pbiPayload.addProperty("timestamp", Instant.now().toString());
                pbiPayload.addProperty("ConsDiaP", consDiaP);
                pbiPayload.addProperty("ConsHora", consHora * 1000000);
                pbiPayload.addProperty("ConsHoraP", consHoraP * 1000000);
                // Envia o valor que está sendo acumulado em tempo real (vai subir ao longo da hora)
                pbiPayload.addProperty("ConsumoParcialDaHora", consumoAcumuladoNaHora);
                // Envia o total da hora anterior. Será > 0 apenas na primeira execução após a virada da hora.
                pbiPayload.addProperty("ConsumoTotalHoraFechada", totalHoraFechada);
                pbiPayload.addProperty("EAkWh", eakwh);
                pbiPayload.addProperty("Envio", envio);
                pbiPayload.addProperty("Erro", erro);
                pbiPayload.addProperty("HdDev", hdDev);
                pbiPayload.addProperty("Ia_n", ia_n);
                pbiPayload.addProperty("Ib_n", ib_n);
                pbiPayload.addProperty("Ic_n", ic_n);
                pbiPayload.addProperty("NSerie", nserie);
                //pbiPayload.addProperty("temperature", temperature);
                pbiPayload.addProperty("Va_n", va_n);
                pbiPayload.addProperty("Vb_n", vb_n);
                pbiPayload.addProperty("Vc_n", vc_n);
                pbiPayload.addProperty("VFreq", vfreq);
                pbiPayload.addProperty("WiFi.RSSI", wifi_rssi);
                pbiPayload.addProperty("Ptot", ptot);
                //pbiPayload.addProperty("Fator_Potencia", fatorPotencia);
                pbiPayload.addProperty("CustoPeriodo", custoPeriodo);
                pbiPayload.addProperty("PrevisaoFalha", falhaPrevista ? 1 : 0);
                pbiPayload.addProperty("Va_b", va_b);
                pbiPayload.addProperty("Vb_c", vb_c);
                pbiPayload.addProperty("Vc_a", vc_a);

                ExportacaoDadosPWBI.sendDataToPowerBI(pbiPayload);

            } catch (Exception e) {
                System.err.println("Ocorreu um erro no ciclo principal: " + e.getMessage());
                e.printStackTrace();
            }

            System.out.printf("--- Ciclo concluído. Aguardando %d segundos para o próximo... ---\n", INTERVALO_EXECUCAO_SEGUNDOS);
            Thread.sleep(INTERVALO_EXECUCAO_SEGUNDOS * 1000);
        }
    }

    // --- FUNÇÕES AUXILIARES PARA LER O JSON DE FORMA SEGURA ---
    private static String getTelemetryAsString(JsonObject telemetria, String key, String defaultValue) {
        try { if (telemetria.has(key)) { JsonElement valueElement = telemetria.getAsJsonArray(key).get(0).getAsJsonObject().get("value"); return valueElement.isJsonNull() ? defaultValue : valueElement.getAsString(); } } catch (Exception e) { System.out.println("[AVISO] Chave de texto '" + key + "' não encontrada ou com formato inesperado."); } return defaultValue;
    }
    private static double getTelemetryAsDouble(JsonObject telemetria, String key, double defaultValue) {
        try { if (telemetria.has(key)) { JsonElement valueElement = telemetria.getAsJsonArray(key).get(0).getAsJsonObject().get("value"); return valueElement.isJsonNull() ? defaultValue : valueElement.getAsDouble(); } } catch (Exception e) { System.out.println("[AVISO] Chave numérica '" + key + "' não encontrada ou com formato inesperado."); } return defaultValue;
    }
    private static long getTelemetryAsLong(JsonObject telemetria, String key, long defaultValue) {
        try { if (telemetria.has(key)) { JsonElement valueElement = telemetria.getAsJsonArray(key).get(0).getAsJsonObject().get("value"); return valueElement.isJsonNull() ? defaultValue : valueElement.getAsLong(); } } catch (Exception e) { System.out.println("[AVISO] Chave numérica longa '" + key + "' não encontrada ou com formato inesperado."); } return defaultValue;
    }
}