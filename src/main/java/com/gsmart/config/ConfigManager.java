// Localização: src/main/java/com/gsmart/config/ConfigManager.java
package main.java.com.gsmart.config;

import main.java.com.gsmart.utils.CryptoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    // --- caminhos para os ficheiros ---
    private static final String CONFIG_DIRECTORY = "config";
    private static final String CONFIG_FILE = CONFIG_DIRECTORY + "/gsmart.conf";
    private static final String METRIC_CONFIGS_FILE = CONFIG_DIRECTORY + "/metrics.conf";
    private static final String ACTIVE_PIPELINES_FILE = CONFIG_DIRECTORY + "/pipelines.conf";
    private static final String ALERT_RULES_FILE = CONFIG_DIRECTORY + "/alerts.conf";
    private static final String INSIGHT_RULES_FILE = CONFIG_DIRECTORY + "/insights.conf";
    private static final List<String> SENSITIVE_KEYS = List.of(
            "thingsboard.url", "thingsboard.user", "db.url", "db.user", "powerbi.url",
            "fabric.connectionstring", "mqtt.broker.url", "telegram.token", "telegram.chat_id"
    );
    public ConfigManager() {
        try {
            Files.createDirectories(Paths.get(CONFIG_DIRECTORY));
        } catch (IOException e) {
            logger.error("Falha crítica ao criar o diretório de configuração.", e);
        }
    }

    /**
     * Carrega as propriedades do ficheiro de configuração principal (`gsmart.conf`).
     * <p>
     * Se o ficheiro não for encontrado, retorna um objeto de propriedades vazio. Após o
     * carregamento, este método decifra automaticamente os valores de chaves sensíveis
     * (como URLs e tokens) usando o {@link main.java.com.gsmart.utils.CryptoUtils}.
     *
     * @return Um objeto {@link Properties} com as configurações carregadas e decifradas.
     */
    public Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            props.load(input);
            logger.info("Arquivo de configuração '{}' carregado.", CONFIG_FILE);

            // --- ALTERAÇÃO: Desencripta os valores após carregar ---
            for (String key : SENSITIVE_KEYS) {
                if (props.containsKey(key)) {
                    String encryptedValue = props.getProperty(key);
                    String decryptedValue = CryptoUtils.decrypt(encryptedValue);
                    props.setProperty(key, decryptedValue);
                }
            }
        } catch (IOException e) {
            logger.warn("Arquivo de configuração '{}' não encontrado. Usando valores padrão.", CONFIG_FILE);
        }
        return props;
    }

    /**
     * Guarda as propriedades no ficheiro de configuração principal (`gsmart.conf`).
     * <p>
     * Antes de guardar, este método cifra automaticamente os valores associados a chaves
     * sensíveis (definidas na lista {@code SENSITIVE_KEYS}) para garantir que não
     * são armazenados em texto puro.
     *
     * @param props O objeto {@link Properties} contendo as configurações a serem guardadas.
     */
    public void saveProperties(Properties props) {
        // --- Cria uma cópia para não modificar o objeto original em memória ---
        Properties propsToSave = new Properties();
        propsToSave.putAll(props);

        // --- Encripta os valores antes de salvar ---
        for (String key : SENSITIVE_KEYS) {
            if (propsToSave.containsKey(key)) {
                String plainValue = propsToSave.getProperty(key);
                String encryptedValue = CryptoUtils.encrypt(plainValue);
                propsToSave.setProperty(key, encryptedValue);
            }
        }

        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            propsToSave.store(output, "GSmart Application Configuration (Sensitive data encrypted)");
            logger.info("Configurações salvas com sucesso no arquivo '{}'.", CONFIG_FILE);
        } catch (IOException e) {
            logger.error("Falha ao salvar o arquivo de configuração '{}'.", CONFIG_FILE, e);
        }
    }

    /**
     * Guarda as listas de regras de Alerta e de Alarme em ficheiros serializados separados.
     * <p>
     * A serialização de objetos permite guardar o estado completo de cada regra de forma
     * eficiente, facilitando o seu carregamento na próxima sessão da aplicação.
     *
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
     * Carrega a lista de regras de Alerta a partir de um ficheiro serializado (`alerts.conf`).
     *
     * @return Uma lista de {@link AlertRule}. Se o ficheiro não for encontrado ou estiver
     * corrompido, retorna uma lista vazia para garantir que a aplicação inicie sem erros.
     */
    public List<AlertRule> loadAlertRules() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ALERT_RULES_FILE))) {
            List<AlertRule> rules = (List<AlertRule>) ois.readObject();
            logger.info("Regras de alerta carregadas com sucesso de '{}'.", ALERT_RULES_FILE);
            return rules;
        } catch (IOException | ClassNotFoundException e) {
            logger.warn("Ficheiro de regras de alerta não encontrado ou inválido. A iniciar com uma lista vazia.");
            return new ArrayList<>();
        }
    }

    /**
     * Carrega a lista de regras de Alarme a partir de um ficheiro serializado (`insights.conf`).
     *
     * @return Uma lista de {@link InsightRule}. Se o ficheiro não for encontrado ou estiver
     * corrompido, retorna uma lista vazia.
     */
    public List<InsightRule> loadInsightRules() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(INSIGHT_RULES_FILE))) {
            List<InsightRule> rules = (List<InsightRule>) ois.readObject();
            logger.info("Regras de alarme carregadas com sucesso de '{}'.", INSIGHT_RULES_FILE);
            return rules;
        } catch (IOException | ClassNotFoundException e) {
            logger.warn("Ficheiro de regras de alarme não encontrado ou inválido. A iniciar com uma lista vazia.");
            return new ArrayList<>();
        }
    }

    // --- O resto dos métodos de salvar/carregar usam os novos caminhos de ficheiro ---

    /**
     * Guarda a lista de configurações de métricas (incluindo aliases e expressões)
     * num ficheiro serializado (`metrics.conf`).
     * <p>
     * Isto permite que as personalizações feitas pelo utilizador na tabela de métricas
     * sejam preservadas entre as sessões.
     *
     * @param metricConfigs A lista de {@link MetricConfig} a ser guardada.
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
     * Carrega a lista de configurações de métricas a partir do ficheiro serializado (`metrics.conf`).
     *
     * @return Uma lista de {@link MetricConfig}. Se o ficheiro não for encontrado, retorna uma
     * lista vazia.
     */
    public List<MetricConfig> loadMetricConfigs() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(METRIC_CONFIGS_FILE))) {
            return (List<MetricConfig>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            logger.warn("Ficheiro de configs de métricas não encontrado. A iniciar com lista vazia.");
            return new ArrayList<>();
        }
    }

    /**
     * Guarda a lista de pipelines que estão atualmente em execução num ficheiro serializado (`pipelines.conf`).
     * <p>
     * Esta funcionalidade permite que a aplicação reinicie automaticamente as pipelines ativas
     * na próxima vez que for iniciada.
     *
     * @param activePipelines A lista de configurações de pipelines ativas a serem guardadas.
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
     * Carrega a lista de configurações de pipelines que estavam ativas na última sessão.
     *
     * @return Uma lista de {@link SerializablePipelineConfig} prontas para serem usadas
     * para reiniciar as pipelines. Retorna uma lista vazia se o ficheiro não for encontrado.
     */
    public List<SerializablePipelineConfig> loadActivePipelines() {
        File file = new File(ACTIVE_PIPELINES_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<SerializablePipelineConfig>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            logger.warn("Ficheiro de pipelines ativas não encontrado ou inválido.");
            return new ArrayList<>();
        }
    }
}