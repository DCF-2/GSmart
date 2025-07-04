# Classe: PipelineManager

**Pacote:** `com.gsmart.pipeline`

## Descrição Geral

Gerencia o ciclo de vida de múltiplas tarefas de pipeline (DataPipeline).
Esta classe é responsável por iniciar, parar e monitorar todas as tarefas ativas,
mantendo um registro delas em um mapa.
Ela utiliza um ExecutorService para gerenciar as threads de forma eficiente.

## Métodos

---

### `public void setOnTaskListUpdated(Runnable onTaskListUpdated)`

Define um callback (Runnable) a ser executado sempre que a lista de tarefas
for atualizada (adição ou remoção).
Isso é usado para notificar a GUI para redesenhar a lista de tarefas.

---

### `public List<PipelineTask> getRunningTasks()`

Retorna uma cópia da lista de tarefas atualmente em execução.
A lista é copiada para evitar problemas de concorrência (ConcurrentModificationException)
ao iterar sobre ela enquanto a original pode ser modificada.

---

### `public void launchPipeline(PipelineConfiguration config)`

Lança um novo pipeline com base em uma configuração fornecida.
Este método orquestra a criação de todos os componentes necessários:
1. O GSmartListener para capturar eventos da pipeline.
2. A instância de DataPipeline (a lógica principal).
3. A Thread que executará a pipeline.
4. O PipelineTask que encapsula todos esses componentes e seu estado.

---

### `public void relaunchPipeline(PipelineTask oldTask)`

Para uma tarefa antiga e lança uma nova com base na configuração original da tarefa.
Útil para reiniciar uma pipeline que parou ou encontrou um erro.

---

### `public void showMonitorFor(PipelineTask task)`

Exibe a janela de monitoramento para uma tarefa específica.
Se a janela ainda não existir ou tiver sido fechada, uma nova é criada.
Se já existir, ela é trazida para a frente.

---

### `public void stopAllPipelines()`

Tenta parar todas as tarefas de monitoramento em execução.
Exibe um diálogo de confirmação ao usuário antes de prosseguir. Se confirmado,
invoca o método stop() de cada tarefa ativa.

