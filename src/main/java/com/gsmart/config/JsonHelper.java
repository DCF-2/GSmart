// Localização: src/main/java/com/gsmart/JsonHelper.java
package main.java.com.gsmart.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Fornece métodos utilitários para trabalhar com objetos JSON.
 *
 * Esta classe abstrai operações comuns de parsing e manipulação de JSON,
 * garantindo que a lógica de conversão de JSON seja centralizada e consistente
 * em toda a aplicação.
 */
public class JsonHelper {



    /**
     * Extrai um valor de um objeto de telemetria do ThingsBoard como uma String.
     * <p>
     * Lida com a estrutura aninhada do JSON (`{"key":[{"ts":..., "value":...}]}`) de
     * forma segura, retornando um valor padrão se a chave não existir ou o formato for inesperado.
     *
     * @param telemetria O objeto JsonObject contendo os dados.
     * @param key A chave do valor a ser extraído.
     * @param defaultValue O valor a ser retornado em caso de falha.
     * @return O valor como String, ou o valor padrão.
     */
    public static String getAsString(JsonObject telemetria, String key, String defaultValue) {
        try {
            if (telemetria != null && telemetria.has(key)) {
                JsonElement valueElement = telemetria.getAsJsonArray(key).get(0).getAsJsonObject().get("value");
                return valueElement.isJsonNull() ? defaultValue : valueElement.getAsString();
            }
        } catch (Exception e) {
            System.out.println("[AVISO] Chave de texto '" + key + "' não encontrada ou com formato inesperado.");
        }
        return defaultValue;
    }


    /**
     * Extrai um valor de um objeto de telemetria do ThingsBoard como um double.
     *
     * @param telemetria O objeto JsonObject contendo os dados.
     * @param key A chave do valor a ser extraído.
     * @param defaultValue O valor a ser retornado em caso de falha.
     * @return O valor como double, ou o valor padrão.
     */
    public static double getAsDouble(JsonObject telemetria, String key, double defaultValue) {
        try {
            if (telemetria != null && telemetria.has(key)) {
                JsonElement valueElement = telemetria.getAsJsonArray(key).get(0).getAsJsonObject().get("value");
                return valueElement.isJsonNull() ? defaultValue : valueElement.getAsDouble();
            }
        } catch (Exception e) {
            System.out.println("[AVISO] Chave numérica '" + key + "' não encontrada ou com formato inesperado.");
        }
        return defaultValue;
    }

    /**
     * Extrai um valor de um objeto de telemetria do ThingsBoard como um long.
     *
     * @param telemetria O objeto JsonObject contendo os dados.
     * @param key A chave do valor a ser extraído.
     * @param defaultValue O valor a ser retornado em caso de falha.
     * @return O valor como long, ou o valor padrão.
     */
    public static long getAsLong(JsonObject telemetria, String key, long defaultValue) {
        try {
            if (telemetria != null && telemetria.has(key)) {
                JsonElement valueElement = telemetria.getAsJsonArray(key).get(0).getAsJsonObject().get("value");
                return valueElement.isJsonNull() ? defaultValue : valueElement.getAsLong();
            }
        } catch (Exception e) {
            System.out.println("[AVISO] Chave numérica longa '" + key + "' não encontrada ou com formato inesperado.");
        }
        return defaultValue;
    }
}