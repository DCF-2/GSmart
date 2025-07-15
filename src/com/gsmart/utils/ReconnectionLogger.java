// Em GSmart/src/com/gsmart/utils/ReconnectionLogger.java
package com.gsmart.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classe utilitária dedicada a registar eventos específicos de conexão da pipeline.
 *
 * Esta classe utiliza um logger com o nome "ReconnectionLogger", que é configurado
 * no ficheiro logback.xml para escrever num ficheiro de log separado (ex: reconnection.log).
 * Isto permite isolar e analisar facilmente os eventos de perda e restabelecimento
 * de conexão, facilitando a depuração de problemas de rede sem poluir o log geral da aplicação.
 */
public class ReconnectionLogger {
    // Este nome ("ReconnectionLogger") deve ser o mesmo do logger no logback.xml
    private static final Logger logger = LoggerFactory.getLogger("ReconnectionLogger");

    /**
     * Regista uma mensagem de "Conexão Perdida" no log de reconexão.
     * @param pipelineName O nome da pipeline que perdeu a conexão.
     */
    public static void logConnectionLost(String pipelineName) {
        logger.info("CONEXÃO PERDIDA - Pipeline: {}", pipelineName);
    }

    /**
     * Regista uma mensagem de "Conexão Restabelecida" no log de reconexão.
     * @param pipelineName O nome da pipeline que restabeleceu a conexão.
     */
    public static void logConnectionRestored(String pipelineName) {
        logger.info("CONEXÃO RESTABELECIDA - Pipeline: {}", pipelineName);
    }
}