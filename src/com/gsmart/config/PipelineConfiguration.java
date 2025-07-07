// Localização: src/main/java/com/gsmart/pipeline/PipelineConfiguration.java
package com.gsmart.config;

import com.gsmart.resources.IDataSource;
import com.gsmart.windows.LogViewerWindow;

import java.util.List;

/**
 * Representa um conjunto de configuração imutável para uma única instância de pipeline.
 *
 * Esta classe, implementada como um record, agrupa todos os parâmetros essenciais
 * que definem como um pipeline deve operar, desde a sua origem até ao seu destino,
 * incluindo as regras de processamento e a lógica de negócio a ser aplicada.
 *
 * É criada na {@code GSmartGui} e utilizada pelo {@code PipelineManager} para lançar
 * uma nova {@code DataPipeline}.
 *
 * @param dataSource A implementação de {@code IDataSource} que servirá como fonte dos dados.
 * @param powerBiUrl A URL de push do Power BI para onde os dados serão enviados.
 * @param metricConfigs Uma lista de {@code MetricConfig} que define as métricas a serem processadas.
 * @param logicConfig A configuração {@code LogicConfig} com as chaves para a lógica de negócio.
 * @param runBusinessLogic Um booleano que ativa ou desativa a execução da lógica de negócio.
 */
public record PipelineConfiguration(
        IDataSource dataSource,
        String powerBiUrl,
        List<MetricConfig> metricConfigs,
        LogicConfig logicConfig,
        LogViewerWindow logViewer,
        boolean runBusinessLogic
) {
}