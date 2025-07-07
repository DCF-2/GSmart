# Classe: GeradorDeInsights

**Pacote:** `com.gsmart.controller`

## Descrição Geral

Classe utilitária estática que atua como o motor de regras de negócio.  
  
A sua responsabilidade é analisar um conjunto de métricas de um ciclo de processamento  
e gerar "insights" ou alertas humanamente legíveis com base em limiares pré-definidos.  
As regras avaliam custos, diagnósticos de manutenção e previsões de falhas,  
comunicando os resultados através do `GSmartListener`.

- **`@see`**: com.gsmart.resources.GSmartListener


## Métodos da Classe

---

### `public static void gerarInsightsDoCiclo(GSmartListener listener, double eficienciaMaterial, double fatorPotencia, double temperatura, StatusManutencao statusManutencao, boolean falhaPrevista)`

Analisa as métricas consolidadas de um ciclo de pipeline e gera os insights correspondentes.  
  
Compara os valores de entrada com os limiares definidos na classe e, se uma condição for  
atendida, formata uma mensagem descritiva e a envia através do listener para a UI.

- **Parâmetro:** `listener` - O canal de comunicação para enviar os insights gerados.
- **Parâmetro:** `eficienciaMaterial` - O valor da eficiência de material a ser avaliado.
- **Parâmetro:** `fatorPotencia` - O valor do fator de potência a ser avaliado.
- **Parâmetro:** `temperatura` - O valor da temperatura a ser avaliado.
- **Parâmetro:** `statusManutencao` - O estado atual da manutenção, vindo da classe `Manutencao`.
- **Parâmetro:** `falhaPrevista` - Um booleano indicando se uma falha foi prevista pela classe `PrevisaoFalhas`.


