package conectiontingsboard;

import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ExportacaoDadosPWBI {
    public static void exportToCSV(JsonObject data) throws Exception {
        // Configurar o formato de números com ponto decimal
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        decimalFormat.setDecimalSeparatorAlwaysShown(false);

        // Escrevendo o arquivo CSV
        try (FileWriter writer = new FileWriter("C:\\Users\\Desn01\\Documents\\Powerbi/output.csv", false)) { // "false" sobrescreve o arquivo
            // Cabeçalhos do arquivo
            writer.append("Data/Hora,Metricas,Value\n");

            // Iterar pelos valores recebidos
            for (String key : data.keySet()) {
                for (JsonElement element : data.getAsJsonArray(key)) {
                    JsonObject record = element.getAsJsonObject();

                    // Converter timestamp para formato de data legível
                    long timestamp = record.get("ts").getAsLong();
                    String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            .format(new Date(timestamp));

                    // Obter o valor e formatar, se necessário
                    String value = record.get("value").getAsString();
                    if (key.equalsIgnoreCase("Fator_Potencia")) {
                        value = decimalFormat.format(Double.parseDouble(value));
                    }

                    // Escrever os dados no arquivo CSV
                    writer.append(formattedDate)
                            .append(",")
                            .append(key)
                            .append(",")
                            .append(value)
                            .append("\n");
                }
            }
        }

        // Mensagem de sucesso
        System.out.println("Arquivo 'output.csv' atualizado com sucesso!");
    }

    public static void main(String[] args) throws Exception {
        try {
            // Obter o token e buscar os dados do ThingsBoard
            String token = ThingsBoardAPI.getToken();
            JsonObject data = ThingsBoardAPI.fetchData(token);

            // Exportar os dados para o CSV
            exportToCSV(data);
        } catch (Exception e) {
            // Tratamento de erros
            System.err.println("Erro ao exportar os dados: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

