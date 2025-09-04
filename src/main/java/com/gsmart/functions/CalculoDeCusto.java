package main.java.com.gsmart.functions;

/**
 * Classe utilitária para calcular o custo de consumo de energia.
 * <p>
 * Esta classe mantém o estado do último valor de consumo de energia registado
 * para calcular o custo incremental (delta) entre as medições. Inclui também
 * um placeholder para uma futura integração com APIs de tarifas de energia,
 - * como a da Neoenergia.
 */
public class CalculoDeCusto {
    // Tarifa base em R$/kWh para a região. Pode ser atualizada pela API.
    private static double tarifaEnergia = 0.70; // Ex: R$ 0,70 por kWh
    private static double ultimoConsumoTotal_kWh = -1.0;

    /**
     * Calcula o custo do consumo de energia desde a última medição.
     * <p>
     * Este método é stateful: ele armazena o último valor total de consumo para
     * calcular a diferença (delta) na chamada seguinte. Se o medidor for reiniciado
     * (valor atual menor que o anterior), o cálculo é reiniciado.
     *
     * @param consumoTotalAtual_kWh O valor total de kWh lido do medidor (ex: EAkWh).
     * @return O custo em Reais (R$) para o período, ou 0.0 na primeira chamada.
     */
    public static double calcularCustoDoPeriodo(double consumoTotalAtual_kWh) {
        if (ultimoConsumoTotal_kWh == -1.0) {
            // Primeira execução, apenas armazena o valor inicial.
            ultimoConsumoTotal_kWh = consumoTotalAtual_kWh;
            return 0.0; // Não há consumo delta para calcular ainda.
        }

        // Calcula o consumo desde a última leitura
        double consumoDelta_kWh = consumoTotalAtual_kWh - ultimoConsumoTotal_kWh;

        // Atualiza o último valor para a próxima chamada
        ultimoConsumoTotal_kWh = consumoTotalAtual_kWh;

        if (consumoDelta_kWh < 0) {
            // O medidor pode ter sido resetado, reinicia a contagem.
            return 0.0;
        }

        // Futuramente, podemos chamar a função da API da Neoenergia aqui
        // double tarifaReal = getTarifaFromNeoenergiaAPI();
        // return consumoDelta_kWh * tarifaReal;

        return consumoDelta_kWh * tarifaEnergia;
    }

    /**
     * Placeholder para uma futura integração com uma API de tarifas de energia.
     * <p>
     * Atualmente, retorna um valor fixo pré-configurado. A lógica para se conectar
     * a uma API externa (ex: Neoenergia) e buscar a tarifa em tempo real seria
     * implementada aqui.
     *
     * @return A tarifa de energia em R$/kWh.
     */
    public static double getTarifaFromNeoenergiaAPI() {
        // A lógica para conectar na API e buscar a tarifa entraria aqui.
        // Por enquanto, usamos o valor fixo.
        System.out.println("[API Externa] Usando tarifa de energia pré-configurada.");
        return tarifaEnergia;
    }
}


