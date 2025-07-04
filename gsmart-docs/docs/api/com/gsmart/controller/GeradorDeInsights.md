# Classe: GeradorDeInsights

**Pacote:** `com.gsmart.controller`

## Descrição Geral

Classe utilitária estática que atua como o motor de regras de negócio.
Sua responsabilidade é analisar um conjunto de métricas de um ciclo de processamento
e gerar "insights" ou alertas humanamente legíveis com base em limiares pré-definidos.

As regras avaliam custos, diagnósticos de manutenção e previsões de falhas,
comunicando os resultados através do {@link GSmartListener}.

## Métodos

---

### `public static void gerarInsightsDoCiclo(GSmartListener listener, double eficienciaMaterial, double fatorPotencia, double temperatura, StatusManutencao statusManutencao, boolean falhaPrevista)`

Analisa as métricas consolidadas de um ciclo de pipeline e gera os insights correspondentes.
Compara os valores de entrada com os limiares da classe e, se uma condição for atendida,
formata uma mensagem descritiva e a envia através do listener.

