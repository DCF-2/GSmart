// Localização: src/main/java/com/gsmart/pipeline/PipelineTask.java
package com.gsmart.pipeline;

import com.gsmart.resources.TaskStatus;
import com.gsmart.config.PipelineConfiguration;
import com.gsmart.windows.ConnectionErrorDialog;
import com.gsmart.windows.MonitoringWindow;

import java.util.UUID;
import static com.gsmart.pipeline.DataPipeline.logger;

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

    public void forceReconnect() {
        if (pipeline != null) {
            pipeline.triggerManualReconnect();
        }
    }

    // --- Getters e Setters (sem alterações) ---
    public void setMonitoringWindow(MonitoringWindow w) { this.monitoringWindow = w; if(w!=null) w.updateStatus(this.status); }
    public void clearMonitoringWindow() { this.monitoringWindow = null; }
    public void setConnectionErrorDialog(ConnectionErrorDialog d) { this.errorDialog = d; }
    public void clearConnectionErrorDialog() { this.errorDialog = null; }
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