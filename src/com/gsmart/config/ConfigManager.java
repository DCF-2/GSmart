// Localização: src/main/java/com/gsmart/config/ConfigManager.java
package com.gsmart.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class ConfigManager {

    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    private static final String CONFIG_FILE = "gsmart.properties";

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
}