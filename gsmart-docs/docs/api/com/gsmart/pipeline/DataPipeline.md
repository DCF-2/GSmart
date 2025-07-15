# Classe: `DataPipeline`

**Pacote:** `com.gsmart.pipeline`

## Descrição Geral

Classe utilitária estática que atua como o motor de regras de negócio.  
  
A sua responsabilidade é analisar um conjunto de métricas de um ciclo de processamento  
e gerar "insights" ou alertas humanamente legíveis com base em limiares pré-definidos.  
As regras avaliam custos, diagnósticos de manutenção e previsões de falhas,  
comunicando os resultados através do `GSmartListener`.

- **Ver Também:** com.gsmart.resources.GSmartListener


## Métodos da Classe

---

### `public void triggerManualReconnect()`

*Nenhuma documentação de método fornecida.*

---

### `public void requestStop()`

*Nenhuma documentação de método fornecida.*

---

### `public void run()`

*Nenhuma documentação de método fornecida.*

---

### `private void enviarAlertaParaNodeRED(String mensagem)`

*Nenhuma documentação de método fornecida.*

---

### `private void enviarInsightParaNodeRED(String mensagem, String tipo)`

Envia uma mensagem de alarme para o endpoint /insight do Node-RED.  
  
Este método formata um payload JSON contendo a mensagem e o tipo do alarme,  
e realiza uma requisição POST para um tópico MQTT diferente dos alertas críticos.

- **Parâmetro:** `mensagem` - O texto do alarme a ser enviado.
- **Parâmetro:** `tipo` - A categoria do alarme (ex: "CUSTO", "EFICIÊNCIA").


