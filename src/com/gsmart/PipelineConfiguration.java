// Localização: src/main/java/com/gsmart/PipelineConfiguration.java
package com.gsmart;

import com.gsmart.sources.IDataSource;
import java.util.List;

/**
 * Um record para agrupar todas as configurações de uma única pipeline.
 * Isso simplifica a passagem de parâmetros entre a GUI e o gerenciador.
 */
public record PipelineConfiguration(
        IDataSource dataSource,
        String powerBiUrl,
        String acumuloKey,
        List<MetricConfig> metricConfigs,
        LogicConfig logicConfig,
        LogViewerWindow logViewer
) {
}