# Classe: PipelineTask

**Pacote:** `com.gsmart.pipeline`

## Descrição Geral

Representa uma tarefa de pipeline completa e seu estado atual.
Esta classe atua como um contêiner que agrupa todos os elementos associados a um
único processo de monitoramento: a thread de execução, a instância da pipeline,
a configuração original e as janelas de UI associadas (monitoramento e erro).

Ela é o principal objeto gerenciado pelo {@link PipelineManager}.

## Métodos

---

### `public void stop()`

Orquestra uma parada graciosa e completa da tarefa e de seus componentes.
A sequência de parada é a seguinte:
<ol>
<li>Sinaliza para a instância da {@link DataPipeline} que ela deve encerrar seu loop.</li>
<li>Interrompe a thread da pipeline para acordá-la de qualquer estado de espera (sleep).</li>
<li>Fecha e remove as janelas de UI associadas (monitor e diálogo de erro).</li>
<li>Executa o callback para notificar o {@link PipelineManager} de que a tarefa foi removida.</li>
<li>Define o status final como FINISHED.</li>
</ol>

---

### `public void forceReconnect()`

Delega a solicitação de reconexão manual para a instância da DataPipeline subjacente.

---

### `public void setMonitoringWindow(MonitoringWindow w)`

Define a janela de monitoramento associada e atualiza seu status.

---

### `public void clearMonitoringWindow()`

Limpa a referência à janela de monitoramento, geralmente quando ela é fechada.

---

### `public void setConnectionErrorDialog(ConnectionErrorDialog d)`

Define o diálogo de erro de conexão associado a esta tarefa.

---

### `public void clearConnectionErrorDialog()`

Limpa a referência ao diálogo de erro de conexão.

