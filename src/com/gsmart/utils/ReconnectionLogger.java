// Em GSmart/src/com/gsmart/utils/ReconnectionLogger.java
package com.gsmart.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReconnectionLogger {
    // Este nome ("ReconnectionLogger") deve ser o mesmo do logger no logback.xml
    private static final Logger logger = LoggerFactory.getLogger("ReconnectionLogger");

    public static void logConnectionLost(String pipelineName) {
        logger.info("CONEXÃO PERDIDA - Pipeline: {}", pipelineName);
    }

    public static void logConnectionRestored(String pipelineName) {
        logger.info("CONEXÃO RESTABELECIDA - Pipeline: {}", pipelineName);
    }
}