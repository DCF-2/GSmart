// Localização: src/com/gsmart/services/CsvExportService.java
package com.gsmart.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Serviço responsável por exportar dados de telemetria para ficheiros CSV.
 *
 * Esta classe gere a criação, escrita e rotação de ficheiros de log de telemetria,
 * garantindo que apenas os 10 registos mais recentes (correspondentes a 200 horas
 * de dados) são mantidos, implementando uma lógica de pilha (buffer circular).
 */
public class CsvExportService {

    private static final Logger logger = LoggerFactory.getLogger(CsvExportService.class);
    private static final String EXPORT_DIRECTORY_NAME = "GSmart_Exports";
    private static final int MAX_FILES = 10;
    private final File exportDirectory;

    /**
     * Construtor do serviço de exportação.
     * Garante que o diretório de exportação existe.
     */
    public CsvExportService() {
        this.exportDirectory = new File(EXPORT_DIRECTORY_NAME);
        if (!exportDirectory.exists()) {
            boolean wasCreated = exportDirectory.mkdirs();
            if (wasCreated) {
                logger.info("Diretório de exportação criado em: {}", exportDirectory.getAbsolutePath());
            } else {
                logger.error("Falha ao criar o diretório de exportação em: {}", exportDirectory.getAbsolutePath());
            }
        }
    }

    /**
     * Método principal que recebe os dados de telemetria e os escreve num novo ficheiro CSV.
     * Antes de escrever, ele gere a rotação dos ficheiros para garantir que o limite não é excedido.
     *
     * @param telemetryData Uma lista de mapas, onde cada mapa representa uma linha de dados.
     */
    public void exportData(List<Map<String, Object>> telemetryData) {
        if (telemetryData == null || telemetryData.isEmpty()) {
            logger.warn("Tentativa de exportar dados de telemetria vazios. A operação foi ignorada.");
            return;
        }

        // Passo 1: Gerir a rotação de ficheiros.
        manageFileRotation();

        // Passo 2: Criar um nome de ficheiro único com base na data e hora atuais.
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "telemetria_" + timestamp + ".csv";
        File outputFile = new File(exportDirectory, fileName);

        // Passo 3: Escrever os dados no ficheiro CSV.
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            logger.info("A exportar {} registos para o ficheiro: {}", telemetryData.size(), outputFile.getName());

            // 3.1. Extrai o cabeçalho (as chaves) do primeiro registo de dados.
            String[] headers = telemetryData.get(0).keySet().toArray(new String[0]);
            writer.println(String.join(";", headers));

            // 3.2. Itera sobre cada registo de telemetria (cada mapa)
            for (Map<String, Object> rowData : telemetryData) {
                StringBuilder line = new StringBuilder();
                for (int i = 0; i < headers.length; i++) {
                    Object value = rowData.get(headers[i]);
                    line.append(value != null ? value.toString() : "");
                    if (i < headers.length - 1) {
                        line.append(";");
                    }
                }
                writer.println(line); // Escreve a linha de dados no ficheiro
            }
            logger.info("Exportação para {} concluída com sucesso.", outputFile.getName());

        } catch (IOException e) {
            logger.error("Ocorreu um erro ao escrever no ficheiro CSV: " + outputFile.getName(), e);
            JOptionPane.showMessageDialog(null, "Falha ao exportar os dados de telemetria para o ficheiro CSV.", "Erro de Exportação", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Verifica o número de ficheiros no diretório de exportação.
     * Se o número de ficheiros exceder o limite (MAX_FILES), o ficheiro mais antigo é removido.
     * Este método será chamado antes de cada nova exportação.
     */
    /**
     * Verifica o número de ficheiros no diretório de exportação.
     * Se o número de ficheiros exceder o limite, o ficheiro mais antigo é removido.
     */
    private void manageFileRotation() {
        // 1. Obtém a lista de ficheiros .csv no diretório.
        File[] files = exportDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
        if (files == null || files.length < MAX_FILES) {
            // Se não houver ficheiros ou se houver espaço, não faz nada.
            return;
        }

        // 2. Pergunta ao utilizador se deseja substituir o ficheiro mais antigo.
        int confirmation = JOptionPane.showConfirmDialog(
                null, // Janela pai (null para centrar no ecrã)
                "A pasta de exportação de telemetria está cheia (10 arquivos).\n" +
                        "Deseja substituir o arquivo mais antigo para continuar a salvar?",
                "Pasta de Exportação Cheia",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            logger.warn("O utilizador escolheu não substituir o ficheiro de telemetria mais antigo. A exportação foi cancelada.");
            return; // O utilizador cancelou, então não fazemos a rotação.
        }

        // 3. Ordena os ficheiros pela data de modificação (do mais antigo para o mais recente).
        Arrays.sort(files, Comparator.comparingLong(File::lastModified));

        // 4. O ficheiro mais antigo é o primeiro da lista.
        File oldestFile = files[0];
        logger.info("Limite de ficheiros atingido. A remover o ficheiro mais antigo: {}", oldestFile.getName());

        // 5. Apaga o ficheiro mais antigo.
        if (!oldestFile.delete()) {
            logger.error("Não foi possível apagar o ficheiro mais antigo: {}", oldestFile.getName());
        }
    }
}
