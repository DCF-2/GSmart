// Localização: src/main/java/com/gsmart/DataPipeline.java
package com.gsmart;

import com.google.gson.JsonObject;
import com.gsmart.sources.IDataSource;
import conectiontingsboard.ExportacaoDadosPWBI;
import conectiontingsboard.ThingsBoardAPI;
import functrendz.CalculoDeCusto;
import functrendz.Manutencao;
import functrendz.PrevisaoFalhas;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataPipeline {
    private static final Logger logger = LoggerFactory.getLogger(DataPipeline.class);
    private final IDataSource dataSource;
    private final String powerBiPushUrl;
    private final long intervaloExecucaoSegundos = 5;
    private double consumoAcumuladoNaHora = 0.0;
    private ZonedDateTime horaDeInicioDoCiclo;
    private static final ZoneId FUSO_HORARIO_BRASIL = ZoneId.of("America/Sao_Paulo");


    public DataPipeline(IDataSource dataSource, String powerBiPushUrl) {
        this.dataSource = dataSource;
        this.powerBiPushUrl = powerBiPushUrl;
        this.horaDeInicioDoCiclo = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")).truncatedTo(ChronoUnit.HOURS);
    }

    public void run() throws InterruptedException {
        logger.info("🚀 INICIANDO PIPELINE COM FONTE: {} 🚀", dataSource.getSourceName());
        logger.info("O ciclo de acúmulo de consumo por hora iniciou em: {}", horaDeInicioDoCiclo.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        while (true) {
            try {
                ZonedDateTime horaAtualBrasil = ZonedDateTime.now(FUSO_HORARIO_BRASIL);
                System.out.printf("\n--- Iniciando novo ciclo de processamento em %s ---\n", horaAtualBrasil.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

                // 1. EXTRAÇÃO
                System.out.println("[ETAPA 1/3] Buscando dados do ThingsBoard...");
                String token = ThingsBoardAPI.getToken();
                JsonObject telemetria = ThingsBoardAPI.fetchData(token);
                System.out.println("Dados recebidos com sucesso!");

                double consDiaP = JsonHelper.getAsDouble(telemetria, "ConsDiaP", 0.0);
                double consHora = JsonHelper.getAsDouble(telemetria, "ConsHora", 0.0);
                double consHoraP = JsonHelper.getAsDouble(telemetria, "ConsHoraP", 0.0);
                double eakwh = JsonHelper.getAsDouble(telemetria, "EAkWh", 0.0);
                long envio = JsonHelper.getAsLong(telemetria, "Envio", 0L);
                double erro = JsonHelper.getAsDouble(telemetria, "Erro", 0.0);
                double ia_n = JsonHelper.getAsDouble(telemetria, "Ia_n", 0.0);
                double ib_n = JsonHelper.getAsDouble(telemetria, "Ib_n", 0.0);
                double ic_n = JsonHelper.getAsDouble(telemetria, "Ic_n", 0.0);
                String nserie = JsonHelper.getAsString(telemetria, "NSerie", "N/A");
                double temperature = JsonHelper.getAsDouble(telemetria, "temperature", 70.0);
                double va_n = JsonHelper.getAsDouble(telemetria, "Va_n", 0.0);
                double vb_n = JsonHelper.getAsDouble(telemetria, "Vb_n", 0.0);
                double vc_n = JsonHelper.getAsDouble(telemetria, "Vc_n", 0.0);
                double vfreq = JsonHelper.getAsDouble(telemetria, "VFreq", 0.0);
                long wifi_rssi = JsonHelper.getAsLong(telemetria, "WiFi.RSSI", -90L);
                double ptot = JsonHelper.getAsDouble(telemetria, "Ptot", 0.0);
                double fatorPotencia = JsonHelper.getAsDouble(telemetria, "Fator_Potencia", 1.0);
                double va_b = JsonHelper.getAsDouble(telemetria, "Va_b", 0.0);
                double vb_c = JsonHelper.getAsDouble(telemetria, "Vb_c", 0.0);
                double vc_a = JsonHelper.getAsDouble(telemetria, "Vc_a", 0.0);

                double totalHoraFechada = 0.0;
                if (horaAtualBrasil.isAfter(horaDeInicioDoCiclo.plusHours(1))) {
                    logger.info("!!! HORA CONCLUÍDA !!! Total acumulado: {}", consumoAcumuladoNaHora);
                    totalHoraFechada = consumoAcumuladoNaHora;
                    consumoAcumuladoNaHora = 0.0;
                    horaDeInicioDoCiclo = horaDeInicioDoCiclo.plusHours(1);
                    logger.info("Próximo ciclo de acúmulo se encerrará após: {}", horaDeInicioDoCiclo.plusHours(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }
                consumoAcumuladoNaHora += consHoraP;
                logger.debug("Valor ConsHoraP lido: {} | Acumulado parcial da hora: {}", String.format("%.4f", consHoraP), String.format("%.4f", consumoAcumuladoNaHora));

                logger.info("[ETAPA 2/3] Processando dados e gerando inteligência...");
                double custoPeriodo = CalculoDeCusto.calcularCustoDoPeriodo(eakwh);
                Manutencao.StatusManutencao statusManutencao = Manutencao.verificarManutencao(fatorPotencia, temperature);
                PrevisaoFalhas.registrarMetricas(temperature, fatorPotencia, ptot);
                boolean falhaPrevista = PrevisaoFalhas.preverFalhas();

                logger.info("[ETAPA 3/3] Enviando dados para o Power BI...");
                JsonObject pbiPayload = new JsonObject();
                Instant horaCorrigida = Instant.now().minus(3, ChronoUnit.HOURS);
                pbiPayload.addProperty("timestamp", horaCorrigida.toString());
                pbiPayload.addProperty("HdDev", horaAtualBrasil.format(DateTimeFormatter.ofPattern("HH:mm:ss - dd/MM/yyyy")));
                pbiPayload.addProperty("OrigemDados", dataSource.getSourceName());

                //DateTimeFormatter formatoHdDev = DateTimeFormatter.ofPattern("HH:mm:ss - dd/MM/yyyy");
                //pbiPayload.addProperty("HdDev", horaAtualBrasil.format(formatoHdDev));
                pbiPayload.addProperty("ConsDiaP", consDiaP );
                pbiPayload.addProperty("ConsHora", consHora );
                pbiPayload.addProperty("ConsHoraP", consHoraP * 1000000);
                pbiPayload.addProperty("ConsumoParcialDaHora", consumoAcumuladoNaHora);
                pbiPayload.addProperty("ConsumoTotalHoraFechada", totalHoraFechada);
                pbiPayload.addProperty("EAkWh", eakwh);
                pbiPayload.addProperty("Envio", envio);
                pbiPayload.addProperty("Erro", erro);
                pbiPayload.addProperty("Ia_n", ia_n);
                pbiPayload.addProperty("Ib_n", ib_n);
                pbiPayload.addProperty("Ic_n", ic_n);
                pbiPayload.addProperty("NSerie", nserie);
                pbiPayload.addProperty("Va_n", va_n);
                pbiPayload.addProperty("Vb_n", vb_n);
                pbiPayload.addProperty("Vc_n", vc_n);
                pbiPayload.addProperty("VFreq", vfreq);
                pbiPayload.addProperty("WiFi.RSSI", wifi_rssi);
                pbiPayload.addProperty("Ptot", ptot);
                pbiPayload.addProperty("CustoPeriodo", custoPeriodo);
                pbiPayload.addProperty("PrevisaoFalha", falhaPrevista ? 1 : 0);
                pbiPayload.addProperty("Va_b", va_b);
                pbiPayload.addProperty("Vb_c", vb_c);
                pbiPayload.addProperty("Vc_a", vc_a);

                ExportacaoDadosPWBI.sendDataToPowerBI(pbiPayload, this.powerBiPushUrl);

            } catch (Exception e) {
                logger.error("Ocorreu um erro no ciclo principal da pipeline: {}", e.getMessage(), e);
            }
            logger.info("--- Ciclo concluído. Aguardando {} segundos para o próximo... ---", intervaloExecucaoSegundos);
            Thread.sleep(intervaloExecucaoSegundos * 1000);
        }
    }
}