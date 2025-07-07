# Classe: PipelineManager

**Pacote:** `com.gsmart.pipeline`

## Descrição Geral

Gerencia o ciclo de vida de múltiplas tarefas de pipeline (`PipelineTask`).  
Esta classe é o orquestrador central que lança, reinicia e para os pipelines.  
  
<h3>Responsabilidades:</h3>  
<ul>  
<li>Lançar novos pipelines com base em uma configuração.</li>  
<li>Manter uma lista de todas as tarefas em execução.</li>  
<li>Gerenciar a comunicação entre a lógica de fundo e a interface gráfica.</li>  
<li>Exibir janelas de monitoramento e diálogos de erro de conexão.</li>  
</ul>

- **`@see`**: com.gsmart.pipeline.PipelineTask
- **`@see`**: com.gsmart.pipeline.DataPipeline
- **`@see`**: com.gsmart.resources.GSmartListener


## Métodos da Classe

---

### `public void setOnTaskListUpdated(Runnable onTaskListUpdated)`

Define um callback (Runnable) a ser executado sempre que a lista de tarefas  
for atualizada (adição ou remoção).  
Isso é usado para notificar a GUI para redesenhar a lista de tarefas.

- **Parâmetro:** `onTaskListUpdated` - O Runnable a ser executado.


---

### `public List<PipelineTask> getRunningTasks()`

Retorna uma cópia da lista de tarefas atualmente em execução.  
A lista é copiada para evitar problemas de concorrência (ConcurrentModificationException)  
ao iterar sobre ela enquanto a original pode ser modificada.

- **`@return`**: Uma nova lista contendo as tarefas em execução.


---

### `public void launchPipeline(PipelineConfiguration config)`

Lança um novo pipeline com base em uma configuração fornecida.  
  
Este método instancia e configura todos os componentes necessários para uma nova  
tarefa de monitoramento, incluindo a `DataPipeline`, a `Thread` de execução  
e o listener de eventos, encapsulando tudo em um objeto `PipelineTask`.

- **Parâmetro:** `config` - O objeto de configuração contendo todos os parâmetros necessários  
para o pipeline, como a fonte de dados, métricas e URL de destino.


---

### `public void relaunchPipeline(PipelineTask oldTask)`

Para uma tarefa antiga e lança uma nova com base na configuração original da tarefa.  
Muito útil para reiniciar um pipeline que parou ou encontrou um erro.

- **Parâmetro:** `oldTask` - A tarefa existente que precisa ser reiniciada.


---

### `public void showMonitorFor(PipelineTask task)`

Exibe a janela de monitoramento para uma tarefa específica.  
  
Se uma janela de monitoramento para esta tarefa ainda não existir ou tiver sido  
fechada, uma nova é criada. Se já existir, a janela existente é trazida  
para a frente, garantindo que apenas uma instância do monitor seja exibida por tarefa.

- **Parâmetro:** `task` - A tarefa para a qual o monitor deve ser exibido.


---

### `public void stopAllPipelines()`

Tenta parar todas as tarefas de monitoramento que estão em execução.  
  
Exibe um diálogo de confirmação ao utilizador antes de prosseguir. Se a ação for  
confirmada, o metodo `stop()` de cada tarefa ativa é invocado para  
     - * garantir uma finalização segura.  
     + * garantir uma finalização segura.

