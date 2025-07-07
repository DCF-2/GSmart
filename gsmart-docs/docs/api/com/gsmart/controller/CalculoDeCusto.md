# Classe: CalculoDeCusto

**Pacote:** `com.gsmart.controller`

## Descrição Geral

Ponto de entrada (entry point) principal para a aplicação GSmart.  
  
A única responsabilidade desta classe é conter o método `main`, que  
inicializa e exibe a primeira janela da interface gráfica, a `LoginWindow`,  
dando início à execução do programa.

## Métodos da Classe

---

### `public static double calcularCustoDoPeriodo(double consumoTotalAtual_kWh)`

Calcula o custo do consumo de energia desde a última medição.

- **Parâmetro:** `consumoTotalAtual_kWh` - O valor total de kWh lido do medidor (EAkWh).
- **`@return`**: O custo em Reais (R$) para o período.


---

### `public static double getTarifaFromNeoenergiaAPI()`

Placeholder para uma futura integração com a API da Neoenergia.  
Atualmente, retorna um valor fixo.

- **`@return`**: A tarifa de energia em R$/kWh.


