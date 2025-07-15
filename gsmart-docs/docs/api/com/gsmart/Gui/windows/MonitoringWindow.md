# Classe: `MonitoringWindow`

**Pacote:** `com.gsmart.Gui.windows`

## Descrição Geral

Representa a janela de monitorização detalhada para uma única tarefa de pipeline.  
  
Esta janela fornece uma visão em tempo real do estado de uma pipeline específica,  
exibindo o seu estado atual (a correr, erro, etc.) e uma área de log para  
insights e alertas gerados por essa tarefa.  
  
Cada instância desta classe está associada a uma única `PipelineTask`.

- **Ver Também:** com.gsmart.pipeline.PipelineTask


## Métodos da Classe

---

### `public void onInsight(String message, String type)`

*Nenhuma documentação de método fornecida.*

---

### `public void onAlert(String title, String message)`

*Nenhuma documentação de método fornecida.*

---

### `public void onStatusUpdate(TaskStatus status)`

*Nenhuma documentação de método fornecida.*

---

### `public void onConnectionLost(String errorMessage)`

*Nenhuma documentação de método fornecida.*

---

### `public void onReconnectionAttempt(long delayInSeconds)`

*Nenhuma documentação de método fornecida.*

---

### `public void onConnectionRestored()`

*Nenhuma documentação de método fornecida.*

---

### `public void updateStatus(TaskStatus status)`

*Nenhuma documentação de método fornecida.*

---

### `private Color getColorForType(String type)`

*Nenhuma documentação de método fornecida.*

---

### `private void appendColoredText(String text, Color color)`

*Nenhuma documentação de método fornecida.*

