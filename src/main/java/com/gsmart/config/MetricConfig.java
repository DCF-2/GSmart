// Localização: src/main/java/com/gsmart/config/MetricConfig.java
package main.java.com.gsmart.config;
import java.io.Serializable;

public class MetricConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean selected;
    private final String originalName;
    private String alias;
    private String expression;
    private final boolean isSystemMetric;

    /**
     * Constrói uma configuração detalhada para uma única métrica a ser processada pela pipeline.
     * <p>
     * Esta classe contém todos os detalhes sobre como uma métrica específica, vinda da
     * fonte de dados, deve ser selecionada, transformada e nomeada. Uma lista destes
     * objetos é usada pela {@code DataPipeline} para construir o payload final.
     *
     * @param originalName O nome da métrica tal como vem da fonte de dados (ex: "temperature").
     * @param isSelected Define se esta métrica deve ser processada e enviada para o destino.
     * @param isSystem Define se esta é uma métrica gerada pelo sistema (ex: "timestamp") e não pela fonte de dados.
     */
    public MetricConfig(String originalName, boolean isSelected, boolean isSystem) {
        this.selected = isSelected;
        this.originalName = originalName;
        this.alias = originalName;
        this.expression = "";
        this.isSystemMetric = isSystem;
    }

    /**
     * Construtor simplificado para métricas que vêm diretamente da fonte de dados.
     * <p>
     * Por padrão, estas métricas são marcadas como selecionadas e não são do sistema.
     *
     * @param originalName O nome da métrica vinda da fonte de dados.
     */
    public MetricConfig(String originalName) {
        this(originalName, true, false);
    }

    public boolean isSelected() {return selected;}

    public void setSelected(boolean selected) {this.selected = selected;}

    public String getOriginalName() {return originalName;}

    public String getAlias() {return alias;}

    public void setAlias(String alias) {this.alias = alias;}

    public String getExpression() {return expression;}

    public void setExpression(String expression) {this.expression = expression;}

    public boolean isSystemMetric() {return isSystemMetric;}
}