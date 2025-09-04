// Localização: src/main/java/com/gsmart/utils/RuleTester.java
package main.java.com.gsmart.utils;

import main.java.com.gsmart.config.AlertRule;
import main.java.com.gsmart.config.InsightRule;
import main.java.com.gsmart.resources.ConditionType;

/**
 * Classe utilitária para avaliar se uma regra de alerta ou alarme seria
 * despoletada com base num valor simulado.
 */
public class RuleTester {

    /**
     * Avalia uma regra de Alerta com base num valor de teste.
     * @param rule A regra de alerta a ser testada.
     * @param testValue O valor numérico a ser usado na simulação.
     * @return true se a condição da regra for satisfeita, false caso contrário.
     */
    public static boolean evaluate(AlertRule rule, double testValue) {
        return checkCondition(testValue, rule.getCondition(), rule.getThresholdValue(), rule.getThresholdValueMax());
    }

    /**
     * Avalia uma regra de Alarme com base num valor de teste.
     * @param rule A regra de alarme a ser testada.
     * @param testValue O valor numérico a ser usado na simulação.
     * @return true se a condição da regra for satisfeita, false caso contrário.
     */
    public static boolean evaluate(InsightRule rule, double testValue) {
        return checkCondition(testValue, rule.getCondition(), rule.getThresholdValue(), rule.getThresholdValueMax());
    }

    /**
     * Lógica central que compara um valor com uma condição e os seus limiares.
     */
    private static boolean checkCondition(double value, ConditionType condition, double threshold, double thresholdMax) {
        switch (condition) {
            case GREATER_THAN:
                return value > threshold;
            case LESS_THAN:
                return value < threshold;
            case EQUALS:
                return value == threshold;
            case BETWEEN:
                // Garante que a verificação funciona mesmo que os valores estejam invertidos
                double min = Math.min(threshold, thresholdMax);
                double max = Math.max(threshold, thresholdMax);
                return value >= min && value <= max;
            default:
                return false;
        }
    }
}