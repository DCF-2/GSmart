// Localização: src/main/java/com/gsmart/Main.java
package com.gsmart;

import com.gsmart.sources.DatabaseSource;
import com.gsmart.sources.IDataSource;
import com.gsmart.sources.ThingsBoardSource;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // --- INSTRUÇÕES DE CONFIGURAÇÃO ---
        // Preencha as variáveis abaixo com suas informações reais.

        // Detalhes da fonte de dados: ThingsBoard Produção
        String thingsboardProdUrl = "http://10.8.0.5:8080";
        String thingsboardProdDeviceId = "06e109e0-e8ff-11ee-bb8b-2563c61db9b0";

        // Detalhes da fonte de dados: Banco de Dados Espelho
        String dbUrl = "jdbc:postgresql://SEU_SERVIDOR:5432/SEU_BANCO";
        String dbUser = "SEU_USUARIO";
        String dbPassword = "SUA_SENHA";
        String dbQuery = "SELECT * FROM sua_tabela ORDER BY timestamp DESC LIMIT 1";

        // Detalhes do destino: Power BI Produção
        String pbiUrlProducao ="https://api.powerbi.com/beta/f06a7122-3027-4312-b120-38f60897fba4/datasets/b8512173-e419-4a24-9559-2f9f52935190/rows?experience=power-bi&key=%2F7U6mAvLW4ddN8tfVZJfT30CTl6LGrU4wmW%2FdotkmHnoj5eaVfUZh9fzlWFCgFnsSWI55MecpelCBmyb9reDpw%3D%3D";

        // --- Menu interativo ---
        System.out.println("BEM-VINDO AO GSmart | Escolha a fonte de dados:");
        System.out.println("1. ThingsBoard - Produção");
        System.out.println("2. Banco de Dados Espelho");

        IDataSource fonteDeDadosEscolhida = null;
        try (Scanner scanner = new Scanner(System.in)) {
            int escolha = scanner.nextInt();
            switch (escolha) {
                case 1:
                    fonteDeDadosEscolhida = new ThingsBoardSource(thingsboardProdUrl, thingsboardProdDeviceId);
                    break;
                case 2:
                    fonteDeDadosEscolhida = new DatabaseSource(dbUrl, dbUser, dbPassword, dbQuery);
                    break;
                default:
                    System.out.println("Opção inválida.");
                    System.exit(1);
            }
        }

        // --- Execução da Pipeline ---
        try {
            if (fonteDeDadosEscolhida != null) {
                DataPipeline pipeline = new DataPipeline(fonteDeDadosEscolhida, pbiUrlProducao);
                pipeline.run();
            }
        } catch (InterruptedException e) {
            System.err.println("A thread da pipeline foi interrompida.");
            Thread.currentThread().interrupt();
        }
    }
}