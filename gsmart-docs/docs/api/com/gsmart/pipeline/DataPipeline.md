# Classe: DataPipeline

**Pacote:** `com.gsmart.pipeline`

## Descrição Geral

Representa a lógica de execução de um único pipeline de dados.  
  
Esta classe opera como um "worker" que roda em sua própria thread e é responsável  
por todo o fluxo de trabalho de um monitoramento contínuo. Suas principais  
responsabilidades incluem a conexão com a fonte de dados, a coleta periódica,  
o processamento e o envio dos dados para o destino final.  
  
Ela também implementa uma lógica robusta de tratamento de falhas e reconexão  
automática para garantir a resiliência do sistema.

- **`@see`**: com.gsmart.pipeline.PipelineManager
- **`@see`**: com.gsmart.resources.IDataSource
- **`@see`**: com.gsmart.resources.GSmartListener


## Métodos da Classe

---

### `public void triggerManualReconnect()`

Sinaliza para a thread da pipeline que uma reconexão manual e imediata deve ser tentada.  
  
Este método interrompe o estado de espera (`sleep`) da thread para forçar uma verificação  
imediata do gatilho de reconexão, sendo útil para ações iniciadas pelo usuário.

---

### `public void requestStop()`

Solicita que a pipeline pare sua execução de forma graciosa.  
  
A flag de parada será verificada no final do ciclo atual ou durante um loop de reconexão,  
garantindo que a thread termine de forma segura sem interromper uma operação no meio.

---

### `public void run()`

Ponto de entrada principal para a execução da pipeline na sua thread.  
  
Contém o loop de vida da pipeline, que consiste em um bloco de operação normal  
(coleta, processamento, envio) e um bloco de tratamento de falhas e reconexão.  
O loop só termina quando o método `requestStop()` é chamado.

