# Classe: `PipelineManager`

**Pacote:** `com.gsmart.pipeline`

## Descrição Geral

Orquestrador central para o ciclo de vida de múltiplas tarefas de pipeline (`PipelineTask`).  
  
Esta classe atua como o "maestro" do sistema, responsável por instanciar,  
gerir e finalizar os processos de monitorização de dados. Ela faz a ponte  
entre as configurações definidas na `com.gsmart.GSmartGui` e a execução  
real das `DataPipeline` em threads separadas.  
  
<h1>Principais Responsabilidades:</h1>  
<ul>  
<li>Lançar novas tarefas de pipeline com base numa `com.gsmart.config.PipelineConfiguration` fornecida pela GUI.</li>  
<li>Manter e fornecer uma lista atualizada de todas as tarefas em execução.</li>  
<li>Gerir a comunicação entre a lógica de fundo (`DataPipeline`) e a interface gráfica (GUI), utilizando um `com.gsmart.resources.GSmartListener`.</li>  
<li>Controlar a exibição de janelas de monitorização individuais e diálogos de erro de conexão.</li>  
<li>Fornecer métodos para reiniciar ou parar tarefas de forma segura.</li>  
</ul>

- **Ver Também:** com.gsmart.pipeline.PipelineTask
- **Ver Também:** com.gsmart.pipeline.DataPipeline
- **Ver Também:** com.gsmart.GSmartGui


## Métodos da Classe

---

### `public void setOnTaskListUpdated(Runnable onTaskListUpdated)`

Define um callback (Runnable) a ser executado sempre que a lista de tarefas  
for atualizada (adição ou remoção).  
Isso é usado para notificar a GUI para redesenhar a lista de tarefas.

- **Parâmetro:** `onTaskListUpdated` - O Runnable a ser executado.


---

### `public void setParentComponent(Component parentComponent)`

*Nenhuma documentação de método fornecida.*

---

### `public void setGlobalLogViewer(LogViewerWindow logViewer)`

*Nenhuma documentação de método fornecida.*

---

### `private void notifyUpdate()`

*Nenhuma documentação de método fornecida.*

---

### `public List<PipelineTask> getRunningTasks()`

Retorna uma cópia da lista de tarefas atualmente em execução.  
A lista é copiada para evitar problemas de concorrência (ConcurrentModificationException)  
ao iterar sobre ela enquanto a original pode ser modificada.

- **Retorna:** Uma nova lista contendo as tarefas em execução.


---

### `public void launchPipeline(PipelineConfiguration config)`

*Nenhuma documentação de método fornecida.*

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

