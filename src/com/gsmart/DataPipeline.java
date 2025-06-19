// Localização: src/main/java/com/gsmart/DataPipeline.java
package com.gsmart;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.gsmart.sources.IDataSource;
import conectiontingsboard.ExportacaoDadosPWBI;
import functrendz.GeradorDeInsights;
import functrendz.Manutencao;
import functrendz.PrevisaoFalhas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

public class DataPipeline {
    private static final Logger logger = LoggerFactory.getLogger(DataPipeline.class);
    private final IDataSource dataSource;
    private final String powerBiPushUrl;
    private final String chaveDeAcumulo;
    private final List<MetricConfig> metricConfigs;
    private final LogicConfig logicConfig;
    private final GSmartListener listener;

    private boolean isConexaoOk = true;
    private int falhasConsecutivas = 0;
    private final long delayBaseSegundos = 5;
    private long delayAtualSegundos = delayBaseSegundos;
    private static final long DELAY_MAXIMO_SEGUNDOS = 300;
    private double consumoAcumuladoNaHora = 0.0;
    private ZonedDateTime horaDeInicioDoCiclo;

    public DataPipeline(IDataSource dataSource, String powerBiPushUrl, String chaveDeAcumulo, List<MetricConfig> metricConfigs, LogicConfig logicConfig, GSmartListener listener) {
        this.dataSource = dataSource;
        this.powerBiPushUrl = powerBiPushUrl;
        this.chaveDeAcumulo = chaveDeAcumulo;
        this.metricConfigs = metricConfigs;
        this.logicConfig = logicConfig;
        this.listener = listener;
        this.horaDeInicioDoCiclo = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")).truncatedTo(ChronoUnit.HOURS);
    }

    public void run() throws InterruptedException {
        logger.info("🚀 INICIANDO PIPELINE GENÉRICA COM FONTE: {} 🚀", dataSource.getSourceName());
        logger.info("Métrica de acúmulo configurada para: '{}'", this.chaveDeAcumulo != null ? this.chaveDeAcumulo : "Nenhuma");

        while (true) {
            try {
                if (!isConexaoOk) {
                    logger.info("✅ CONEXÃO RESTABELECIDA! Retomando operação normal.");
                    isConexaoOk = true;
                }

                ZonedDateTime horaAtualBrasil = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"));
                logger.info("--- Iniciando novo ciclo de processamento em {} ---", horaAtualBrasil.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

                logger.info("[ETAPA 1/3] Buscando dados da fonte...");
                JsonObject telemetria = dataSource.fetchData();
                logger.info("Dados recebidos com sucesso!");

                JsonObject pbiPayload = new JsonObject();
                double valorParaAcumular = 0.0;

                Optional<Double> optTemperatura = Optional.empty();
                Optional<Double> optFatorPotencia = Optional.empty();
                Optional<Double> optPotenciaAtiva = Optional.empty();

                for (MetricConfig config : this.metricConfigs) {
                    String originalName = config.getOriginalName();
                    String alias = config.getAlias();
                    if (telemetria.has(originalName)) {
                        try {
                            JsonElement valorFinal = telemetria.get(originalName).getAsJsonArray().get(0).getAsJsonObject().get("value");
                            if (valorFinal.isJsonPrimitive() && valorFinal.getAsJsonPrimitive().isNumber()) {
                                double valorNumerico = valorFinal.getAsDouble();
                                pbiPayload.addProperty(alias, valorNumerico);

                                if (originalName.equals(logicConfig.temperaturaKey())) optTemperatura = Optional.of(valorNumerico);
                                if (originalName.equals(logicConfig.fatorPotenciaKey())) optFatorPotencia = Optional.of(valorNumerico);
                                if (originalName.equals(logicConfig.potenciaAtivaKey())) optPotenciaAtiva = Optional.of(valorNumerico);

                                if (originalName.equals(this.chaveDeAcumulo)) {
                                    valorParaAcumular = valorNumerico;
                                }
                            } else {
                                pbiPayload.addProperty(alias, valorFinal.getAsString());
                            }
                        } catch (Exception e) {
                            logger.warn("Não foi possível processar a chave '{}'. Formato inesperado. Pulando.", originalName, e);
                        }
                    } else {
                        logger.warn("A chave selecionada '{}' não foi encontrada na resposta da fonte de dados. Pulando.", originalName);
                    }
                }

                double totalHoraFechada = 0.0;
                if (horaAtualBrasil.isAfter(horaDeInicioDoCiclo.plusHours(1))) {
                    logger.info("!!! HORA CONCLUÍDA !!! Total acumulado (de '{}'): {}", this.chaveDeAcumulo, consumoAcumuladoNaHora);
                    totalHoraFechada = consumoAcumuladoNaHora;
                    consumoAcumuladoNaHora = 0.0;
                    horaDeInicioDoCiclo = horaDeInicioDoCiclo.plusHours(1);
                }
                consumoAcumuladoNaHora += valorParaAcumular;

                logger.info("[ETAPA 2/3] Executando lógica de negócio (Insights, Falhas, Custo)...");
                double temperaturaAtual = optTemperatura.orElse(0.0);
                double fatorPotenciaAtual = optFatorPotencia.orElse(0.0);
                double potenciaAtivaAtual = optPotenciaAtiva.orElse(0.0);

                Manutencao.StatusManutencao statusManutencao = Manutencao.verificarManutencao(listener, fatorPotenciaAtual, temperaturaAtual);
                PrevisaoFalhas.registrarMetricas(temperaturaAtual, fatorPotenciaAtual, potenciaAtivaAtual);
                boolean falhaPrevista = PrevisaoFalhas.preverFalhas(listener);
                GeradorDeInsights.gerarInsightsDoCiclo(listener, 95.0, fatorPotenciaAtual, temperaturaAtual, statusManutencao, falhaPrevista);

                pbiPayload.addProperty("timestamp", Instant.now().minus(3, ChronoUnit.HOURS).toString());
                pbiPayload.addProperty("HdDev", horaAtualBrasil.format(DateTimeFormatter.ofPattern("HH:mm:ss - dd/MM/yyyy")));
                pbiPayload.addProperty("OrigemDados", dataSource.getSourceName());
                pbiPayload.addProperty("ConsumoParcialDaHora", consumoAcumuladoNaHora);
                pbiPayload.addProperty("ConsumoTotalHoraFechada", totalHoraFechada);

                logger.info("[ETAPA 3/3] Enviando {} campos para o Power BI...", pbiPayload.size());
                ExportacaoDadosPWBI.sendDataToPowerBI(pbiPayload, this.powerBiPushUrl);

                falhasConsecutivas = 0;
                delayAtualSegundos = delayBaseSegundos;

            } catch (Exception e) {
                isConexaoOk = false;
                falhasConsecutivas++;
                logger.error("Falha de conexão ou processamento (Tentativa #{})! Causa: {}", falhasConsecutivas, e.getMessage(), e);
                delayAtualSegundos = (long) (delayBaseSegundos * Math.pow(2, falhasConsecutivas));
                if (delayAtualSegundos > DELAY_MAXIMO_SEGUNDOS) { delayAtualSegundos = DELAY_MAXIMO_SEGUNDOS; }
                logger.warn("A próxima tentativa de reconexão será em {} segundos...", delayAtualSegundos);
            }

            logger.info("--- Ciclo concluído. Aguardando {} segundos para o próximo... ---", delayAtualSegundos);
            Thread.sleep(delayAtualSegundos * 1000);
        }
    }
}