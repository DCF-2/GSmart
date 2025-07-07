// Localização: src/main/java/com/gsmart/pipeline/PipelineTask.java
package com.gsmart.pipeline;

import com.gsmart.resources.TaskStatus;
import com.gsmart.config.PipelineConfiguration;
import com.gsmart.windows.ConnectionErrorDialog;
import com.gsmart.windows.MonitoringWindow;

import java.util.UUID;
import static com.gsmart.pipeline.DataPipeline.logger;

/**
 * Representa uma tarefa de pipeline completa e o seu estado atual.
 *
 * Esta classe atua como um contentor que agrupa todos os elementos associados a um
 * único processo de monitoramento: a thread de execução, a instância da pipeline,
 * a configuração original e as janelas de UI associadas (monitoramento e erro).
 *
 * É o principal objeto gerido pelo {@code PipelineManager}.
 *
 * @see com.gsmart.pipeline.PipelineManager
 * @see com.gsmart.pipeline.DataPipeline
 * @see com.gsmart.windows.MonitoringWindow
 */
public class PipelineTask {
    private final String id;
    private final String description;
    private final Thread pipelineThread;
    private MonitoringWindow monitoringWindow;
    private ConnectionErrorDialog errorDialog;
    private TaskStatus status;
    private final Runnable onStopCallback;
    private final PipelineConfiguration originalConfig;
    private final DataPipeline pipeline;
    private boolean hasAlert = false;
    private final long startTime;

    /**
     * Construtor da classe PipelineTask.
     *
     * @param description Uma descrição legível da tarefa (ex: "Fonte: ThingsBoard").
     * @param pipelineThread A thread na qual a instância da DataPipeline está a ser executada.
     * @param pipeline A instância da DataPipeline que contém a lógica de execução.
     * @param config A configuração original usada para iniciar esta tarefa, útil para reiniciá-la.
     * @param onStopCallback Um callback a ser executado quando esta tarefa é parada, para notificar o PipelineManager.
     */
    public PipelineTask(String description, Thread pipelineThread, DataPipeline pipeline, PipelineConfiguration config, Runnable onStopCallback) {
        this.id = UUID.randomUUID().toString();
        this.description = description;
        this.pipelineThread = pipelineThread;
        this.pipeline = pipeline;
        this.originalConfig = config;
        this.onStopCallback = onStopCallback;
        this.status = TaskStatus.RUNNING;
        this.monitoringWindow = null;
        this.errorDialog = null;
        this.startTime = System.currentTimeMillis();
    }

    /**
     * Orquestra uma parada graciosa e completa da tarefa e dos seus componentes.
     *
     * A sequência de parada é a seguinte:
     * <ol>
     * <li>Sinaliza para a instância da {@code DataPipeline} que ela deve encerrar o seu loop.</li>
     * <li>Interrompe a thread da pipeline para acordá-la de qualquer estado de espera (sleep).</li>
     * <li>Fecha e remove as janelas de UI associadas (monitor e diálogo de erro).</li>
     * <li>Executa o callback para notificar o {@code PipelineManager} que a tarefa foi removida.</li>
     * <li>Define o estado final como FINISHED.</li>
     * </ol>
     */
    public void stop() {
        if (status == TaskStatus.RUNNING || status == TaskStatus.ERROR || status == TaskStatus.STOPPING) {
            // Evita chamadas múltiplas para parar
            if (status == TaskStatus.STOPPING || status == TaskStatus.FINISHED) return;

            setStatus(TaskStatus.STOPPING);

            // 1. Sinaliza para a pipeline que ela deve parar.
            if (pipeline != null) {
                pipeline.requestStop();
            }

            // 2. Interrompe a thread da pipeline para que ela saia de qualquer estado de 'sleep'
            //    e possa verificar a flag 'stopRequested'.
            if (pipelineThread != null && pipelineThread.isAlive()) {
                pipelineThread.interrupt();
            }

            // 3. Fecha as janelas associadas
            if (monitoringWindow != null) monitoringWindow.dispose();
            if (errorDialog != null) errorDialog.dispose();

            // 4. Notifica o PipelineManager (isso também remove a tarefa da lista)
            if (onStopCallback != null) onStopCallback.run();

            // 5. O status final será definido pela própria pipeline quando ela sair do loop,
            //    ou aqui como um fallback após um pequeno tempo.
            setStatus(TaskStatus.FINISHED);
            logger.info("Comando de parada processado para a tarefa '{}'.", description);
        }
    }

    /**
     * Delega a solicitação de reconexão manual para a instância da DataPipeline subjacente.
     */
    public void forceReconnect() {
        if (pipeline != null) {
            pipeline.triggerManualReconnect();
        }
    }

    // --- Getters e Setters ---
    /**
     * Define a janela de monitoramento associada e atualiza seu status.
     * @param w A instância da MonitoringWindow.
     */
    public void setMonitoringWindow(MonitoringWindow w) { this.monitoringWindow = w; if(w!=null) w.updateStatus(this.status); }

    /**
     * Limpa a referência à janela de monitoramento, geralmente quando ela é fechada.
     */
    public void clearMonitoringWindow() { this.monitoringWindow = null; }

    /**
     * Define o diálogo de erro de conexão associado a esta tarefa.
     * @param d A instância do ConnectionErrorDialog.
     */
    public void setConnectionErrorDialog(ConnectionErrorDialog d) { this.errorDialog = d; }

    /**
     * Limpa a referência ao diálogo de erro de conexão.
     */
    public void clearConnectionErrorDialog() { this.errorDialog = null; }

    /**
     * Atualiza o status da tarefa e notifica a janela de monitoramento, se existir.
     * @param status O novo TaskStatus.
     */

    public void setStatus(TaskStatus status) { this.status = status; if (this.monitoringWindow != null) this.monitoringWindow.updateStatus(status); }
    public MonitoringWindow getMonitoringWindow() { return monitoringWindow; }
    public ConnectionErrorDialog getConnectionErrorDialog() { return errorDialog; }
    public String getId() { return id; }
    public String getDescription() { return description; }
    public TaskStatus getStatus() { return status; }
    public PipelineConfiguration getOriginalConfig() { return originalConfig; }
    public boolean hasAlert() {return hasAlert;}
    public void setHasAlert(boolean hasAlert) {this.hasAlert = hasAlert;}
    public long getStartTime() {return startTime;}
}