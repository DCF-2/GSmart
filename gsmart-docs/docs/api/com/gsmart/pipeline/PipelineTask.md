# Classe: `PipelineTask`

**Pacote:** `com.gsmart.pipeline`

## Descrição Geral

Representa uma tarefa de pipeline completa e o seu estado atual.  
  
Esta classe atua como um contentor que agrupa todos os elementos associados a um  
único processo de monitoramento: a thread de execução, a instância da pipeline,  
a configuração original e as janelas de UI associadas (monitoramento e erro).  
  
É o principal objeto gerido pelo `PipelineManager`.

- **Ver Também:** com.gsmart.pipeline.PipelineManager
- **Ver Também:** com.gsmart.pipeline.DataPipeline
- **Ver Também:** MonitoringWindow


## Métodos da Classe

---

### `public void stop()`

Orquestra uma parada graciosa e completa da tarefa e dos seus componentes.  
  
A sequência de parada é a seguinte:  
<ol>  
<li>Sinaliza para a instância da `DataPipeline` que ela deve encerrar o seu loop.</li>  
<li>Interrompe a thread da pipeline para acordá-la de qualquer estado de espera (sleep).</li>  
<li>Fecha e remove as janelas de UI associadas (monitor e diálogo de erro).</li>  
<li>Executa o callback para notificar o `PipelineManager` que a tarefa foi removida.</li>  
<li>Define o estado final como FINISHED.</li>  
</ol>

---

### `public void forceReconnect()`

Delega a solicitação de reconexão manual para a instância da DataPipeline subjacente.

---

### `public void setMonitoringWindow(MonitoringWindow w)`

Define a janela de monitoramento associada e atualiza seu status.

- **Parâmetro:** `w` - A instância da MonitoringWindow.


---

### `public void clearMonitoringWindow()`

Limpa a referência à janela de monitoramento, geralmente quando ela é fechada.

---

### `public void setConnectionErrorDialog(ConnectionErrorDialog d)`

Define o diálogo de erro de conexão associado a esta tarefa.

- **Parâmetro:** `d` - A instância do ConnectionErrorDialog.


---

### `public void clearConnectionErrorDialog()`

Limpa a referência ao diálogo de erro de conexão.

---

### `public void setStatus(TaskStatus status)`

*Nenhuma documentação de método fornecida.*

---

### `public MonitoringWindow getMonitoringWindow()`

*Nenhuma documentação de método fornecida.*

---

### `public ConnectionErrorDialog getConnectionErrorDialog()`

*Nenhuma documentação de método fornecida.*

---

### `public String getId()`

*Nenhuma documentação de método fornecida.*

---

### `public String getDescription()`

*Nenhuma documentação de método fornecida.*

---

### `public TaskStatus getStatus()`

*Nenhuma documentação de método fornecida.*

---

### `public PipelineConfiguration getOriginalConfig()`

*Nenhuma documentação de método fornecida.*

---

### `public boolean hasAlert()`

*Nenhuma documentação de método fornecida.*

---

### `public void setHasAlert(boolean hasAlert)`

*Nenhuma documentação de método fornecida.*

---

### `public long getStartTime()`

*Nenhuma documentação de método fornecida.*

