// Localização: src/main/java/com/gsmart/GSmartConsoleLauncher.java
package com.gsmart;

import com.gsmart.sources.DatabaseSource;
import com.gsmart.sources.IDataSource;
import com.gsmart.sources.ThingsBoardSource;
import java.util.Scanner;

public class GSmartConsoleLauncher {

    public static void main(String[] args) {
        // --- Configurações ---
        String thingsboardProdUrl = "http://10.8.0.5:8080";
        String thingsboardProdDeviceId = "06e109e0-e8ff-11ee-bb8b-2563c61db9b0";
        String dbUrl = "jdbc:postgresql://SEU_SERVIDOR/SEU_BANCO";
        String dbUser = "SEU_USUARIO";
        String dbPassword = "SUA_SENHA";
        String dbQuery = "SELECT * FROM sua_tabela ORDER BY timestamp DESC LIMIT 1";
        String pbiUrlProducao = "SUA_URL_DE_PUSH_DO_POWER_BI_AQUI";

        // --- Menu Interativo ---
        System.out.println("GSmart [MODO CONSOLE] | Escolha a fonte de dados:");
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