// Localização: src/main/java/com/gsmart/pipeline/PipelineConfiguration.java
package com.gsmart.config;

import com.gsmart.sources.IDataSource;
import com.gsmart.windows.LogViewerWindow;

import java.util.List;

public record PipelineConfiguration(
        IDataSource dataSource,
        String powerBiUrl,
        String acumuloKey,
        List<MetricConfig> metricConfigs,
        LogicConfig logicConfig,
        LogViewerWindow logViewer,
        boolean runBusinessLogic // --- NOVO CAMPO ---
) {
}