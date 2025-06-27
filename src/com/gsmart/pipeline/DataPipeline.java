// Localização: src/main/java/com/gsmart/pipeline/DataPipeline.java
package com.gsmart.pipeline;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.gsmart.GSmartListener;
import com.gsmart.TaskStatus;
import com.gsmart.config.LogicConfig;
import com.gsmart.config.MetricConfig;
import com.gsmart.sources.IDataSource;
import conectiontingsboard.ExportacaoDadosPWBI;
import functrendz.GeradorDeInsights;
import functrendz.Manutencao;
import functrendz.PrevisaoFalhas;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
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
    private final boolean runBusinessLogic;

    private double consumoAcumuladoNaHora = 0.0;
    private ZonedDateTime horaDeInicioDoCiclo;
    private volatile boolean restartRequested = false;

    public DataPipeline(IDataSource dataSource, String powerBiPushUrl, String chaveDeAcumulo, List<MetricConfig> metricConfigs, LogicConfig logicConfig, GSmartListener listener, boolean runBusinessLogic) {
        this.dataSource = dataSource;
        this.powerBiPushUrl = powerBiPushUrl;
        this.chaveDeAcumulo = chaveDeAcumulo;
        this.metricConfigs = metricConfigs;
        this.logicConfig = logicConfig;
        this.listener = listener;
        this.runBusinessLogic = runBusinessLogic;
        this.horaDeInicioDoCiclo = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")).truncatedTo(ChronoUnit.HOURS);
    }

    public void requestRestart() {
        this.restartRequested = true;
    }

    public void run() {
        logger.info("🚀 INICIANDO PIPELINE GENÉRICA COM FONTE: {} 🚀", dataSource.getSourceName());
        logger.info("Métrica de acúmulo configurada para: '{}'", this.chaveDeAcumulo != null ? this.chaveDeAcumulo : "Nenhuma");
        logger.info("Execução da lógica de negócio: {}", this.runBusinessLogic ? "ATIVADA" : "DESATIVADA");

        long currentRetryDelay = 0; // Inicia o atraso da reconexão em 0
        final long MAX_RETRY_DELAY = 300; // Atraso máximo de 300 segundos
        final long RETRY_INCREMENT = 5; // Incremento de 5 segundos

        while (!Thread.currentThread().isInterrupted()) {
            try {
                if (restartRequested) {
                    logger.info("Pipeline reiniciada por comando do usuário.");
                    restartRequested = false;
                    currentRetryDelay = 0; // Resetar o atraso da reconexão após um reinício manual
                }

                // Se a conexão foi perdida e depois restaurada automaticamente, o listener já notificou.
                // Resetar o atraso para o próximo ciclo normal.
                if (currentRetryDelay > 0) {
                    logger.info("Conexão restaurada automaticamente ou manualmente. Retomando operação normal.");
                    currentRetryDelay = 0;
                }

                ZonedDateTime horaAtualBrasil = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"));
                logger.info("--- Iniciando novo ciclo de processamento em {} ---", horaAtualBrasil.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

                logger.info("[ETAPA 1/3] Buscando dados da fonte...");
                JsonObject telemetria = dataSource.fetchData();
                logger.debug("Dados recebidos da fonte: {}", dataSource.getSourceName());

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
                            if (valorFinal.isJsonPrimitive()) {
                                double valorNumerico;
                                try {
                                    valorNumerico = Double.parseDouble(valorFinal.getAsString());
                                } catch (NumberFormatException | UnsupportedOperationException ex) {
                                    pbiPayload.addProperty(alias, valorFinal.getAsString());
                                    continue;
                                }

                                String expressao = config.getExpression();
                                double valorParaEnviar = valorNumerico;

                                if (expressao != null && !expressao.trim().isEmpty()) {
                                    Expression e = new ExpressionBuilder(expressao).variable("valor").build().setVariable("valor", valorNumerico);
                                    valorParaEnviar = e.evaluate();
                                }
                                pbiPayload.addProperty(alias, valorParaEnviar);

                                if (logicConfig != null) {
                                    if (originalName.equals(logicConfig.temperaturaKey())) optTemperatura = Optional.of(valorNumerico);
                                    if (originalName.equals(logicConfig.fatorPotenciaKey())) optFatorPotencia = Optional.of(valorNumerico);
                                    if (originalName.equals(logicConfig.potenciaAtivaKey())) optPotenciaAtiva = Optional.of(valorNumerico);
                                }

                                if (originalName.equals(this.chaveDeAcumulo)) {
                                    valorParaAcumular = valorParaEnviar;
                                }
                            } else {
                                pbiPayload.addProperty(alias, valorFinal.toString());
                            }
                        } catch (Exception e) {
                            logger.warn("Não foi possível processar a chave '{}'. Pulando.", originalName, e);
                        }
                    }
                }

                double totalHoraFechada = 0.0;
                if (horaAtualBrasil.isAfter(horaDeInicioDoCiclo.plusHours(1))) {
                    totalHoraFechada = consumoAcumuladoNaHora;
                    consumoAcumuladoNaHora = 0.0;
                    horaDeInicioDoCiclo = horaDeInicioDoCiclo.plusHours(1);
                }
                consumoAcumuladoNaHora += valorParaAcumular;

                if (this.runBusinessLogic && logicConfig != null) {
                    double temperaturaAtual = optTemperatura.orElse(0.0);
                    double fatorPotenciaAtual = optFatorPotencia.orElse(0.0);
                    double potenciaAtivaAtual = optPotenciaAtiva.orElse(0.0);
                    Manutencao.StatusManutencao statusManutencao = Manutencao.verificarManutencao(listener, fatorPotenciaAtual, temperaturaAtual);
                    PrevisaoFalhas.registrarMetricas(temperaturaAtual, fatorPotenciaAtual, potenciaAtivaAtual);
                    boolean falhaPrevista = PrevisaoFalhas.preverFalhas(listener);
                    GeradorDeInsights.gerarInsightsDoCiclo(listener, 95.0, fatorPotenciaAtual, temperaturaAtual, statusManutencao, falhaPrevista);
                } else {
                    if (listener != null) listener.onInsight("[INFO] Lógica de negócio desativada.", "INFO");
                }

                pbiPayload.addProperty("timestamp", Instant.now().minus(3, ChronoUnit.HOURS).toString());
                pbiPayload.addProperty("HdDev", horaAtualBrasil.format(DateTimeFormatter.ofPattern("HH:mm:ss - dd/MM/yyyy")));
                pbiPayload.addProperty("OrigemDados", dataSource.getSourceName());
                pbiPayload.addProperty("ConsumoParcialDaHora", consumoAcumuladoNaHora);
                pbiPayload.addProperty("ConsumoTotalHoraFechada", totalHoraFechada);

                ExportacaoDadosPWBI.sendDataToPowerBI(pbiPayload, this.powerBiPushUrl);

                Thread.sleep(5000); // Standard delay between cycles

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Pipeline interrompida. Encerrando a execução.");
                break; // Exit pipeline on interruption
            } catch (Exception e) {
                String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                logger.error("Falha fatal na pipeline: {}", errorMessage, e);

                if (listener != null) {
                    listener.onConnectionLost(errorMessage);
                }

                // --- Reconnection Retry Logic ---
                boolean reconnected = false;
                while (!Thread.currentThread().isInterrupted() && !reconnected) {
                    // Increment delay, capping at MAX_RETRY_DELAY
                    currentRetryDelay += RETRY_INCREMENT;
                    if (currentRetryDelay > MAX_RETRY_DELAY) {
                        currentRetryDelay = MAX_RETRY_DELAY;
                    }

                    // Notify listener about the attempt and delay
                    if (listener != null) {
                        listener.onReconnectionAttempt(currentRetryDelay);
                    }

                    try {
                        Thread.sleep(currentRetryDelay * 1000); // Wait for the specified delay

                        // Attempt to fetch data again to check connection
                        logger.info("Attempting to reconnect and fetch data...");
                        dataSource.fetchData(); // This will throw an exception if connection is still bad

                        // If fetchData succeeds, it means connection is restored
                        reconnected = true;
                        if (listener != null) {
                            listener.onConnectionRestored(); // Notify listener that connection is restored
                        }
                        logger.info("Reconnection successful. Resuming pipeline execution.");
                        // Break out of the reconnection loop, the main while loop will continue

                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        logger.warn("Pipeline interrompida durante tentativa de reconexão. Encerrando a execução.");
                        break; // Exit pipeline on interruption
                    } catch (Exception retryEx) {
                        // Reconnection attempt failed, log and try again
                        errorMessage = retryEx.getMessage() != null ? retryEx.getMessage() : retryEx.getClass().getSimpleName();
                        logger.error("Reconnection attempt failed. Retrying in {} seconds. Error: {}", currentRetryDelay, errorMessage);
                        // The onConnectionLost dialog should remain visible and updated by subsequent onReconnectionAttempt calls
                    }
                }

                if (!reconnected) {
                    // If the loop finished without reconnecting (e.g., interrupted or a critical unrecoverable error)
                    logger.error("Failed to reconnect after multiple attempts. Pipeline will terminate.");
                    if (listener != null) {
                        listener.onStatusUpdate(TaskStatus.ERROR); // Set pipeline status to ERROR if reconnection fails permanently
                    }
                    break; // Exit the main pipeline loop
                }
                // If reconnected, the main while loop will automatically continue to the next iteration
            }
        }

        // This block is executed only when the main while loop exits.
        // Check if it exited due to interruption (manual stop) or an unrecoverable error.
        if (listener != null && !Thread.currentThread().isInterrupted()) {
            // If not interrupted, it means it finished due to an unrecoverable error or other natural termination
            listener.onStatusUpdate(TaskStatus.FINISHED); // Or TaskStatus.ERROR if that's the final state for unrecoverable.
            // For now, FINISHED if it wasn't interrupted.
        }
        logger.info("Execução da pipeline para {} finalizada.", dataSource.getSourceName());
    }
}