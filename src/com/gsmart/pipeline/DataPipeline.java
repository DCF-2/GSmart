// Localização: src/main/java/com/gsmart/pipeline/DataPipeline.java
package com.gsmart.pipeline;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.gsmart.resources.GSmartListener;
import com.gsmart.resources.TaskStatus;
import com.gsmart.config.LogicConfig;
import com.gsmart.config.MetricConfig;
import com.gsmart.resources.IDataSource;
import com.gsmart.sources.ThingsBoardSource;
import com.gsmart.utils.ReconnectionLogger;
import com.gsmart.conection.ExportacaoDadosPWBI;
import com.gsmart.controller.GeradorDeInsights;
import com.gsmart.controller.Manutencao;
import com.gsmart.controller.PrevisaoFalhas;
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
import java.util.concurrent.atomic.AtomicBoolean;

public class DataPipeline {
    public static final Logger logger = LoggerFactory.getLogger(DataPipeline.class);
    private static final Logger reconnectionLogger = LoggerFactory.getLogger("ReconnectionLogger");
    private final IDataSource dataSource;
    private final String powerBiPushUrl;
    private final List<MetricConfig> metricConfigs;
    private final LogicConfig logicConfig;
    private final GSmartListener listener;
    private final boolean runBusinessLogic;
    private final Manutencao manutencao;
    private final PrevisaoFalhas previsaoFalhas;
    private volatile boolean stopRequested = false;
    private final AtomicBoolean manualReconnectTrigger = new AtomicBoolean(false);

    public DataPipeline(IDataSource dataSource, String powerBiPushUrl, List<MetricConfig> metricConfigs, LogicConfig logicConfig, GSmartListener listener, boolean runBusinessLogic) {
        this.dataSource = dataSource;
        this.powerBiPushUrl = powerBiPushUrl;
        this.metricConfigs = metricConfigs;
        this.logicConfig = logicConfig;
        this.listener = listener;
        this.runBusinessLogic = runBusinessLogic;
        this.manutencao = new Manutencao();
        this.previsaoFalhas = new PrevisaoFalhas();
    }

    /**
     * Sinaliza para a thread da pipeline que uma reconexão manual e imediata deve ser tentada.
     */
    public void triggerManualReconnect() {
        logger.info("Sinal de reconexão manual recebido.");
        this.manualReconnectTrigger.set(true);
        // Interrompe o sono atual para que o loop de reconexão possa verificar a bandeira imediatamente.
        Thread.currentThread().interrupt();
    }

    /**
     * Sinaliza que a pipeline deve ser permanentemente parada.
     */
    public void requestStop() {
        this.stopRequested = true;
    }

    public void run() {
        logger.info("🚀 INICIANDO PIPELINE GENÉRICA COM FONTE: {} 🚀", dataSource.getSourceName());
        logger.info("Execução da lógica de negócio: {}", this.runBusinessLogic ? "ATIVADA" : "DESATIVADA");

        final long MAX_RETRY_DELAY = 300;
        final long RETRY_INCREMENT = 5;
        long currentRetryDelay = 0;

        // Loop principal da vida da pipeline. Só termina se stopRequested for true.
        while (!stopRequested) {
            try {
                // =================================================================
                //  BLOCO DE OPERAÇÃO NORMAL
                // =================================================================
                if (currentRetryDelay > 0) {
                    logger.info("Conexão restaurada. Retomando operação normal.");
                    currentRetryDelay = 0; // Reseta o delay após uma reconexão bem-sucedida
                }

                ZonedDateTime horaAtualBrasil = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"));
                logger.info("--- Iniciando novo ciclo de processamento em {} ---", horaAtualBrasil.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

                logger.info("[ETAPA 1/3] Buscando dados da fonte...");
                JsonObject telemetria = dataSource.fetchData();
                logger.debug("Dados recebidos da fonte: {}", dataSource.getSourceName());

                JsonObject pbiPayload = new JsonObject();
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
                            } else {
                                pbiPayload.addProperty(alias, valorFinal.toString());
                            }
                        } catch (Exception e) {
                            logger.warn("Não foi possível processar a chave '{}'. Pulando.", originalName, e);
                        }
                    }
                }

                if (this.runBusinessLogic && logicConfig != null) {
                    double temperaturaAtual = optTemperatura.orElse(0.0);
                    double fatorPotenciaAtual = optFatorPotencia.orElse(0.0);
                    double potenciaAtivaAtual = optPotenciaAtiva.orElse(0.0);

                    Manutencao.StatusManutencao statusManutencao = this.manutencao.verificarManutencao(listener, fatorPotenciaAtual, temperaturaAtual);
                    this.previsaoFalhas.registrarMetricas(temperaturaAtual, fatorPotenciaAtual, potenciaAtivaAtual);
                    boolean falhaPrevista = this.previsaoFalhas.preverFalhas(listener);

                    GeradorDeInsights.gerarInsightsDoCiclo(listener, 95.0, fatorPotenciaAtual, temperaturaAtual, statusManutencao, falhaPrevista);
                } else {
                    if (listener != null) listener.onInsight("[INFO] Lógica de negócio desativada.", "INFO");
                }

                pbiPayload.addProperty("timestamp", Instant.now().minus(3, ChronoUnit.HOURS).toString());
                pbiPayload.addProperty("HdDev", horaAtualBrasil.format(DateTimeFormatter.ofPattern("HH:mm:ss - dd/MM/yyyy")));
                pbiPayload.addProperty("OrigemDados", dataSource.getSourceName());

                ExportacaoDadosPWBI.sendDataToPowerBI(pbiPayload, this.powerBiPushUrl);

                // Espera antes do próximo ciclo
                Thread.sleep(5000);

            } catch (InterruptedException e) {
                // A interrupção é o sinal para PARAR.
                logger.warn("Sinal de interrupção recebido. Encerrando a pipeline...");
                this.stopRequested = true; // Garante que a flag de parada seja definida para sair do loop principal.

            } catch (Exception e) {
                // =================================================================
                //  BLOCO DE TRATAMENTO DE FALHA E RECONEXÃO
                // =================================================================
                String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                logger.error("Falha na pipeline: {}", errorMessage);
                if (dataSource instanceof ThingsBoardSource) {
                    ((ThingsBoardSource) dataSource).clearAuthToken();
                }
                if (listener != null) {
                    listener.onConnectionLost(errorMessage);
                    // MUDANÇA: Chame o logger diretamente
                    reconnectionLogger.info("CONEXÃO PERDIDA - Pipeline: {}", dataSource.getSourceName());
                }

                // Loop de reconexão. Só sai se reconectar ou se a parada for solicitada.
                while (!stopRequested) {
                    try {
                        if (manualReconnectTrigger.compareAndSet(true, false)) {
                            logger.info("Iniciando tentativa de reconexão manual imediata...");
                            currentRetryDelay = 0; // Reseta o delay para futuras falhas.
                        } else {
                            currentRetryDelay += RETRY_INCREMENT;
                            if (currentRetryDelay > MAX_RETRY_DELAY) currentRetryDelay = MAX_RETRY_DELAY;
                            if (listener != null) listener.onReconnectionAttempt(currentRetryDelay);
                            Thread.sleep(currentRetryDelay * 1000);
                        }

                        logger.info("Tentando reconectar...");
                        dataSource.fetchData(); // Se isso funcionar, não lança exceção.

                        if (listener != null) listener.onConnectionRestored();
                        reconnectionLogger.info("CONEXÃO RESTABELECIDA - Pipeline: {}", dataSource.getSourceName());
                        break; // Sucesso! Sai do loop de reconexão e volta para o loop principal.

                    } catch (InterruptedException ie) {
                        logger.warn("Sinal de interrupção recebido durante a reconexão. Encerrando a pipeline...");
                        this.stopRequested = true; // Garante a saída de todos os loops.
                    } catch (Exception retryEx) {
                        logger.error("Tentativa de reconexão falhou: {}", retryEx.getMessage());
                        // MUDANÇA: Chame o logger diretamente
                        reconnectionLogger.info("TENTATIVA DE RECONEXÃO FALHOU - Pipeline: {}", dataSource.getSourceName());

                    }
                }
            }
        } // Fim do loop while principal

        // O código só chega aqui quando o loop 'while' é encerrado (stopRequested = true)
        if (listener != null) {
            listener.onStatusUpdate(TaskStatus.FINISHED);
        }
        logger.info("Execução da pipeline para {} finalizada.", dataSource.getSourceName());
    }
}