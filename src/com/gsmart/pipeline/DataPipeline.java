// Localização: src/main/java/com/gsmart/pipeline/DataPipeline.java
package com.gsmart.pipeline;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.gsmart.GSmartListener;
import com.gsmart.TaskStatus;
import com.gsmart.config.LogicConfig;
import com.gsmart.config.MetricConfig;
import com.gsmart.sources.IDataSource;
import com.gsmart.sources.ThingsBoardSource;
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

    // --- INÍCIO DA CORREÇÃO (Novos Campos de Instância) ---
    private final Manutencao manutencao;
    private final PrevisaoFalhas previsaoFalhas;
    // --- FIM DA CORREÇÃO ---

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

        // --- INÍCIO DA CORREÇÃO (Inicializar as instâncias) ---
        this.manutencao = new Manutencao();
        this.previsaoFalhas = new PrevisaoFalhas();
        // --- FIM DA CORREÇÃO ---
    }

    public void requestRestart() {
        this.restartRequested = true;
    }

    public void run() {
        logger.info("🚀 INICIANDO PIPELINE GENÉRICA COM FONTE: {} 🚀", dataSource.getSourceName());
        logger.info("Métrica de acúmulo configurada para: '{}'", this.chaveDeAcumulo != null ? this.chaveDeAcumulo : "Nenhuma");
        logger.info("Execução da lógica de negócio: {}", this.runBusinessLogic ? "ATIVADA" : "DESATIVADA");

        long currentRetryDelay = 0;
        final long MAX_RETRY_DELAY = 300;
        final long RETRY_INCREMENT = 5;

        while (!Thread.currentThread().isInterrupted()) {
            try {
                if (restartRequested) {
                    logger.info("Pipeline reiniciada por comando do usuário.");
                    restartRequested = false;
                    currentRetryDelay = 0;
                }

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

                    // --- INÍCIO DA CORREÇÃO (Chamar métodos da instância) ---
                    Manutencao.StatusManutencao statusManutencao = this.manutencao.verificarManutencao(listener, fatorPotenciaAtual, temperaturaAtual);
                    this.previsaoFalhas.registrarMetricas(temperaturaAtual, fatorPotenciaAtual, potenciaAtivaAtual);
                    boolean falhaPrevista = this.previsaoFalhas.preverFalhas(listener);
                    // --- FIM DA CORREÇÃO ---

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

                Thread.sleep(5000);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Pipeline interrompida. Encerrando a execução.");
                break;
            } catch (Exception e) {
                String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                logger.error("Falha fatal na pipeline: {}", errorMessage, e);

                if (dataSource instanceof ThingsBoardSource) {
                    ((ThingsBoardSource) dataSource).clearAuthToken();
                }

                if (listener != null) {
                    listener.onConnectionLost(errorMessage);
                }

                boolean reconnected = false;
                while (!Thread.currentThread().isInterrupted() && !reconnected) {
                    currentRetryDelay += RETRY_INCREMENT;
                    if (currentRetryDelay > MAX_RETRY_DELAY) {
                        currentRetryDelay = MAX_RETRY_DELAY;
                    }

                    if (listener != null) {
                        listener.onReconnectionAttempt(currentRetryDelay);
                    }

                    try {
                        Thread.sleep(currentRetryDelay * 1000);

                        logger.info("Attempting to reconnect and fetch data...");
                        dataSource.fetchData();

                        reconnected = true;
                        if (listener != null) {
                            listener.onConnectionRestored();
                        }
                        logger.info("Reconnection successful. Resuming pipeline execution.");

                    } catch (InterruptedException ie) {
                        // O clique em "Reconectar" interrompe o sleep e causa esta exceção.
                        // Em vez de encerrar, vamos apenas registrar e continuar para a próxima iteração do loop,
                        // que tentará a reconexão imediatamente.
                        logger.info("Tentativa de reconexão manual acionada pelo usuário.");
                        // Não precisamos fazer mais nada, o loop continuará para a tentativa de reconexão.

                    } catch (Exception retryEx) {
                        errorMessage = retryEx.getMessage() != null ? retryEx.getMessage() : retryEx.getClass().getSimpleName();
                        logger.error("Reconnection attempt failed. Retrying in {} seconds. Error: {}", currentRetryDelay, errorMessage);
                    }
                }

                if (!reconnected) {
                    logger.error("Failed to reconnect after multiple attempts. Pipeline will terminate.");
                    if (listener != null) {
                        listener.onStatusUpdate(TaskStatus.ERROR);
                    }
                    break;
                }
            }
        }

        if (listener != null && !Thread.currentThread().isInterrupted()) {
            listener.onStatusUpdate(TaskStatus.FINISHED);
        }
        logger.info("Execução da pipeline para {} finalizada.", dataSource.getSourceName());
    }
}