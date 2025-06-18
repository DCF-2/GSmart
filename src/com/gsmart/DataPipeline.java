// Localização: src/main/java/com/gsmart/DataPipeline.java
package com.gsmart;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.gsmart.sources.IDataSource;
import conectiontingsboard.ExportacaoDadosPWBI;
// Os imports da sua lógica de negócio podem ser mantidos para uso futuro
// import functrendz.CalculoDeCusto;
// import functrendz.Manutencao;
// import functrendz.PrevisaoFalhas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

public class DataPipeline {
    private static final Logger logger = LoggerFactory.getLogger(DataPipeline.class);
    private final IDataSource dataSource;
    private final String powerBiPushUrl;
    private final String chaveDeAcumulo;
    private final List<String> colunasSelecionadas; // Guarda a lista de colunas a processar

    // --- Variáveis de estado para reconexão e acúmulo ---
    private boolean isConexaoOk = true;
    private int falhasConsecutivas = 0;
    private final long delayBaseSegundos = 5;
    private long delayAtualSegundos = delayBaseSegundos;
    private static final long DELAY_MAXIMO_SEGUNDOS = 300;
    private double consumoAcumuladoNaHora = 0.0;
    private ZonedDateTime horaDeInicioDoCiclo;

    public DataPipeline(IDataSource dataSource, String powerBiPushUrl, String chaveDeAcumulo, List<String> colunasSelecionadas) {
        this.dataSource = dataSource;
        this.powerBiPushUrl = powerBiPushUrl;
        this.chaveDeAcumulo = chaveDeAcumulo;
        this.colunasSelecionadas = colunasSelecionadas;
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

                // ETAPA 1: EXTRAÇÃO GENÉRICA
                logger.info("[ETAPA 1/3] Buscando dados da fonte...");
                JsonObject telemetria = dataSource.fetchData();
                logger.info("Dados recebidos com sucesso!");

                JsonObject pbiPayload = new JsonObject();
                double valorParaAcumular = 0.0;

                // LÓGICA CORRIGIDA E RIGOROSA: Itera sobre a lista de colunas que o USUÁRIO selecionou.
                for (String chave : this.colunasSelecionadas) {
                    // Verifica se a telemetria recebida realmente contém a chave que o usuário pediu
                    if (telemetria.has(chave)) {
                        try {
                            // Extrai o valor final da estrutura do JSON do ThingsBoard
                            JsonElement valorFinal = telemetria.get(chave).getAsJsonArray().get(0).getAsJsonObject().get("value");

                            // Adiciona a chave e o valor ao payload do Power BI
                            if (valorFinal.isJsonPrimitive() && valorFinal.getAsJsonPrimitive().isNumber()) {
                                pbiPayload.addProperty(chave, valorFinal.getAsNumber());
                            } else {
                                pbiPayload.addProperty(chave, valorFinal.getAsString());
                            }

                            // Se esta for a chave especial de acúmulo, guarda seu valor
                            if (chave.equals(this.chaveDeAcumulo) && valorFinal.isJsonPrimitive() && valorFinal.getAsJsonPrimitive().isNumber()) {
                                valorParaAcumular = valorFinal.getAsDouble();
                            }
                        } catch (Exception parseException) {
                            logger.warn("Não foi possível processar a chave '{}'. Formato inesperado. Pulando.", chave);
                        }
                    } else {
                        logger.warn("A chave selecionada '{}' não foi encontrada na resposta da fonte de dados. Pulando.", chave);
                    }
                }

                // Lógica de acúmulo horário
                double totalHoraFechada = 0.0;
                if (horaAtualBrasil.isAfter(horaDeInicioDoCiclo.plusHours(1))) {
                    logger.info("!!! HORA CONCLUÍDA !!! Total acumulado (de '{}'): {}", this.chaveDeAcumulo, consumoAcumuladoNaHora);
                    totalHoraFechada = consumoAcumuladoNaHora;
                    consumoAcumuladoNaHora = 0.0;
                    horaDeInicioDoCiclo = horaDeInicioDoCiclo.plusHours(1);
                }
                consumoAcumuladoNaHora += valorParaAcumular;

                // ETAPA 2: Lógica de negócio desativada temporariamente
                logger.info("[ETAPA 2/3] Lógica de negócio (Insights, Falhas, Custo) em espera.");

                // ETAPA 3: CARGA
                // Adiciona campos calculados e de metadados ao payload
                pbiPayload.addProperty("timestamp", Instant.now().minus(3, ChronoUnit.HOURS).toString());
                pbiPayload.addProperty("HdDev", horaAtualBrasil.format(DateTimeFormatter.ofPattern("HH:mm:ss - dd/MM/yyyy")));
                pbiPayload.addProperty("OrigemDados", dataSource.getSourceName());
                pbiPayload.addProperty("ConsumoParcialDaHora", consumoAcumuladoNaHora);
                pbiPayload.addProperty("ConsumoTotalHoraFechada", totalHoraFechada);

                logger.info("[ETAPA 3/3] Enviando {} campos para o Power BI...", pbiPayload.size());
                ExportacaoDadosPWBI.sendDataToPowerBI(pbiPayload, this.powerBiPushUrl);

                // Se chegamos até aqui, o ciclo foi um sucesso. Resetamos os contadores de falha.
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