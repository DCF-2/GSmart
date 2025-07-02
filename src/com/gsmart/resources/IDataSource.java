// Localização: src/main/java/com/gsmart/sources/IDataSource.java
package com.gsmart.resources;

import com.google.gson.JsonObject;

/**
 * Define o contrato que toda fonte de dados para o GSmart deve seguir.
 * Garante que a classe DataPipeline possa trabalhar com qualquer fonte
 * de forma padronizada.
 */
public interface IDataSource {

    /**
     * Busca os dados de telemetria mais recentes da fonte.
     * @return Um JsonObject contendo os dados de telemetria.
     * @throws Exception Se ocorrer um erro durante a busca dos dados.
     */
    JsonObject fetchData() throws Exception;

    /**
     * Retorna um nome descritivo da fonte de dados.
     * @return Uma String com o nome da fonte (ex: "ThingsBoard" ou "Banco de Dados").
     */
    String getSourceName();
}