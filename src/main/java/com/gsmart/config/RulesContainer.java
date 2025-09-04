// Localização: src/main/java/com/gsmart/config/RulesContainer.java
package main.java.com.gsmart.config;

import java.io.Serializable;
import java.util.List;

/**
 * Um objeto "contentor" para guardar as listas de regras de Alerta e de Alarme,
 * facilitando a sua serialização para um único ficheiro de importação/exportação.
 */
public class RulesContainer implements Serializable {

    private static final long serialVersionUID = 1L; // Para controlo de versão

    private final List<AlertRule> alertRules;
    private final List<InsightRule> insightRules;

    public RulesContainer(List<AlertRule> alertRules, List<InsightRule> insightRules) {
        this.alertRules = alertRules;
        this.insightRules = insightRules;
    }

    // Getters para que possamos extrair as listas após carregar o ficheiro
    public List<AlertRule> getAlertRules() {
        return alertRules;
    }

    public List<InsightRule> getInsightRules() {
        return insightRules;
    }
}