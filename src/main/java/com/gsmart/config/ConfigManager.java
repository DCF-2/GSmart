// Localização: src/main/java/com/gsmart/config/ConfigManager.java
package main.java.com.gsmart.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Gere a persistência das configurações da aplicação.
 *
 * Esta classe é responsável por carregar as configurações do utilizador da última
 * sessão (como URLs, nomes de utilizador, etc.) de um ficheiro {@code .properties}
 * quando a aplicação inicia, e por salvar as configurações atuais quando a
 * aplicação é fechada.
 *
 * Isto garante uma melhor experiência do utilizador, que não precisa de reintroduzir
 * os mesmos dados a cada execução.
 */
public class ConfigManager {

    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    private static final String CONFIG_FILE = "gsmart.properties";
    private static final String METRIC_CONFIGS_FILE = "metrics.ser";
    private static final String ACTIVE_PIPELINES_FILE = "active_pipelines.ser";

    /**
     * Carrega as propriedades do arquivo de configuração.
     * Se o arquivo não existir, retorna um objeto de propriedades vazio.
     * @return Um objeto Properties com as configurações carregadas.
     */
    public Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            props.load(input);
            logger.info("Arquivo de configuração '{}' carregado com sucesso.", CONFIG_FILE);
        } catch (IOException e) {
            logger.warn("Arquivo de configuração '{}' não encontrado. Usando valores padrão.", CONFIG_FILE);
            // O arquivo não existe na primeira vez, isso é normal.
        }
        return props;
    }

    /**
     * Salva as propriedades no arquivo de configuração.
     * @param props O objeto Properties contendo as configurações a serem salvas.
     */
    public void saveProperties(Properties props) {
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            props.store(output, "GSmart Application Configuration");
            logger.info("Configurações salvas com sucesso no arquivo '{}'.", CONFIG_FILE);
        } catch (IOException e) {
            logger.error("Falha ao salvar o arquivo de configuração '{}'.", CONFIG_FILE, e);
        }
    }
    private static final String ALERT_RULES_FILE = "alerts.ser";
    private static final String INSIGHT_RULES_FILE = "insights.ser";

    /**
     * Salva as listas de regras de Alerta e Alarme em ficheiros serializados.
     * @param alertRules A lista de regras de alerta a ser guardada.
     * @param insightRules A lista de regras de alarme a ser guardada.
     */
    public void saveRules(List<AlertRule> alertRules, List<InsightRule> insightRules) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ALERT_RULES_FILE))) {
            oos.writeObject(alertRules);
            logger.info("Regras de alerta salvas com sucesso em '{}'.", ALERT_RULES_FILE);
        } catch (IOException e) {
            logger.error("Falha ao salvar o ficheiro de regras de alerta.", e);
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(INSIGHT_RULES_FILE))) {
            oos.writeObject(insightRules);
            logger.info("Regras de alarme salvas com sucesso em '{}'.", INSIGHT_RULES_FILE);
        } catch (IOException e) {
            logger.error("Falha ao salvar o ficheiro de regras de alarme.", e);
        }
    }

    /**
     * Carrega a lista de regras de Alerta a partir de um ficheiro serializado.
     * @return Uma lista de AlertRule. Se o ficheiro não for encontrado, retorna uma lista vazia.
     */
    public List<AlertRule> loadAlertRules() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ALERT_RULES_FILE))) {
            List<AlertRule> rules = (List<AlertRule>) ois.readObject();
            logger.info("Regras de alerta carregadas com sucesso de '{}'.", ALERT_RULES_FILE);
            return rules;
        } catch (IOException | ClassNotFoundException e) {
            logger.warn("Ficheiro de regras de alerta não encontrado ou inválido. A iniciar com uma lista vazia.");
            return new ArrayList<>(); // Retorna uma lista vazia se houver erro ou o ficheiro não existir
        }
    }

    /**
     * Carrega a lista de regras de Alarme a partir de um ficheiro serializado.
     * @return Uma lista de InsightRule. Se o ficheiro não for encontrado, retorna uma lista vazia.
     */
    public List<InsightRule> loadInsightRules() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(INSIGHT_RULES_FILE))) {
            List<InsightRule> rules = (List<InsightRule>) ois.readObject();
            logger.info("Regras de alarme carregadas com sucesso de '{}'.", INSIGHT_RULES_FILE);
            return rules;
        } catch (IOException | ClassNotFoundException e) {
            logger.warn("Ficheiro de regras de alarme não encontrado ou inválido. A iniciar com uma lista vazia.");
            return new ArrayList<>(); // Retorna uma lista vazia se houver erro ou o ficheiro não existir
        }
    }

    /**
     * Salva a lista de configurações de métricas num ficheiro serializado.
     * @param metricConfigs A lista de MetricConfig a ser guardada.
     */
    public void saveMetricConfigs(List<MetricConfig> metricConfigs) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(METRIC_CONFIGS_FILE))) {
            oos.writeObject(metricConfigs);
            logger.info("Configurações de métricas salvas com sucesso em '{}'.", METRIC_CONFIGS_FILE);
        } catch (IOException e) {
            logger.error("Falha ao salvar o ficheiro de configurações de métricas.", e);
        }
    }

    /**
     * Carrega a lista de configurações de métricas a partir de um ficheiro serializado.
     * @return Uma lista de MetricConfig. Se o ficheiro não for encontrado, retorna uma lista vazia.
     */
    public List<MetricConfig> loadMetricConfigs() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(METRIC_CONFIGS_FILE))) {
            List<MetricConfig> configs = (List<MetricConfig>) ois.readObject();
            logger.info("Configurações de métricas carregadas com sucesso de '{}'.", METRIC_CONFIGS_FILE);
            return configs;
        } catch (IOException | ClassNotFoundException e) {
            logger.warn("Ficheiro de configurações de métricas não encontrado ou inválido. A iniciar com uma lista vazia.");
            return new ArrayList<>();
        }
    }

    /**
     * Salva a lista de configurações das pipelines que estavam ativas.
     * @param activePipelines A lista de configurações serializáveis a ser guardada.
     */
    public void saveActivePipelines(List<SerializablePipelineConfig> activePipelines) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ACTIVE_PIPELINES_FILE))) {
            oos.writeObject(activePipelines);
            logger.info("Configurações de {} pipelines ativas salvas com sucesso em '{}'.", activePipelines.size(), ACTIVE_PIPELINES_FILE);
        } catch (IOException e) {
            logger.error("Falha ao salvar o ficheiro de pipelines ativas.", e);
        }
    }

    /**
     * Carrega a lista de configurações das pipelines que estavam ativas na última sessão.
     * @return Uma lista de SerializablePipelineConfig. Se o ficheiro não for encontrado, retorna uma lista vazia.
     */
    public List<SerializablePipelineConfig> loadActivePipelines() {
        File file = new File(ACTIVE_PIPELINES_FILE);
        if (!file.exists()) {
            return new ArrayList<>(); // Ficheiro não existe, retorna lista vazia
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            List<SerializablePipelineConfig> configs = (List<SerializablePipelineConfig>) ois.readObject();
            logger.info("Configurações de {} pipelines ativas carregadas com sucesso de '{}'.", configs.size(), ACTIVE_PIPELINES_FILE);
            return configs;
        } catch (IOException | ClassNotFoundException e) {
            logger.warn("Ficheiro de pipelines ativas não encontrado ou inválido. A iniciar sem pipelines automáticas.");
            return new ArrayList<>();
        }
    }

}