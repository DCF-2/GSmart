// Localização: src/main/java/com/gsmart/pipeline/PipelineTask.java
package com.gsmart.pipeline;

import com.gsmart.resources.TaskStatus;
import com.gsmart.config.PipelineConfiguration;
import com.gsmart.windows.ConnectionErrorDialog;
import com.gsmart.windows.MonitoringWindow;

import java.util.UUID;

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
        if (status == TaskStatus.RUNNING || status == TaskStatus.ERROR) {
            setStatus(TaskStatus.STOPPING);

            if (pipeline != null) {
                pipeline.requestStop();
            } else if (pipelineThread != null && pipelineThread.isAlive()) {
                pipelineThread.interrupt(); // Fallback
            }
            if (monitoringWindow != null) monitoringWindow.dispose();
            if (errorDialog != null) errorDialog.dispose();
            if (onStopCallback != null) onStopCallback.run();
            setStatus(TaskStatus.FINISHED);
        }
    }

    public void forceReconnect() {
        if (pipelineThread != null && pipelineThread.isAlive()) {
            pipelineThread.interrupt();
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