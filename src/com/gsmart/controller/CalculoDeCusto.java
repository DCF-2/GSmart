package com.gsmart.controller;

public class CalculoDeCusto {
    // Tarifa base em R$/kWh para a região. Pode ser atualizada pela API.
    private static double tarifaEnergia = 0.70; // Ex: R$ 0,70 por kWh
    private static double ultimoConsumoTotal_kWh = -1.0;

    /**
     * Calcula o custo do consumo de energia desde a última medição.
     * @param consumoTotalAtual_kWh O valor total de kWh lido do medidor (EAkWh).
     * @return O custo em Reais (R$) para o período.
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
     * Placeholder para uma futura integração com a API da Neoenergia.
     * Atualmente, retorna um valor fixo.
     * @return A tarifa de energia em R$/kWh.
     */
    public static double getTarifaFromNeoenergiaAPI() {
        // A lógica para conectar na API e buscar a tarifa entraria aqui.
        // Por enquanto, usamos o valor fixo.
        System.out.println("[API Externa] Usando tarifa de energia pré-configurada.");
        return tarifaEnergia;
    }
}


