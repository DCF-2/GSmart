// Localização: src/main/java/com/gsmart/LogicConfig.java
package com.gsmart.config;

/**
 * Agrupa as configurações que mapeiam as chaves de telemetria brutas para
 * os papéis específicos exigidos pela lógica de negócio da aplicação.
 *
 * Esta classe é essencial para que os módulos de controlo, como o {@code GeradorDeInsights}
 * ou a {@code PrevisaoFalhas}, saibam qual métrica corresponde à temperatura,
 * ao fator de potência, etc., independentemente do nome original na fonte de dados.
 */
public record LogicConfig(String temperaturaKey, String fatorPotenciaKey, String potenciaAtivaKey) {
}