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

### `private void publicarAlertaMqtt(String mensagem)`

Publica uma mensagem de alerta crítico no tópico MQTT 'gsmart/alerta'.  
Utiliza o `com.gsmart.services.MqttService` para a comunicação direta com o broker.

- **Parâmetro:** `mensagem` - O conteúdo da mensagem de alerta a ser publicada.


---

### `private void publicarAlarmeMqtt(String mensagem, String tipo)`

Publica uma mensagem de alarme (insight) num subtópico MQTT dinâmico.  
O tópico é formatado como 'gsmart/alarme/{tipo}', permitindo uma filtragem fácil  
por parte dos clientes MQTT.

- **Parâmetro:** `mensagem` - O conteúdo do alarme a ser publicado.
- **Parâmetro:** `tipo` - A categoria do alarme (ex: "CUSTO", "MANUTENCAO"), que definirá o subtópico.


---

### `public void exportRemainingData()`

Exporta todos os dados de telemetria acumulados no buffer de memória para um ficheiro CSV.  
Este método é chamado quando a pipeline é parada.

