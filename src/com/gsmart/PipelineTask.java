// Localização: src/main/java/com/gsmart/PipelineTask.java
package com.gsmart;

import java.util.UUID;

public class PipelineTask {
    private final String id;
    private final String description;
    private final Thread pipelineThread;
    private MonitoringWindow monitoringWindow;
    private TaskStatus status;
    private final Runnable onStopCallback;

    public PipelineTask(String description, Thread pipelineThread, Runnable onStopCallback) {
        this.id = UUID.randomUUID().toString();
        this.description = description;
        this.pipelineThread = pipelineThread;
        this.onStopCallback = onStopCallback;
        this.status = TaskStatus.RUNNING;
        this.monitoringWindow = null; // A janela começa nula, é criada sob demanda.
    }

    public void stop() {
        if (status == TaskStatus.RUNNING) {
            setStatus(TaskStatus.STOPPING);
            if (pipelineThread != null && pipelineThread.isAlive()) {
                pipelineThread.interrupt();
            }
            if (monitoringWindow != null) {
                monitoringWindow.dispose();
            }
            if (onStopCallback != null) {
                onStopCallback.run();
            }
            setStatus(TaskStatus.FINISHED);
        }
    }

    public void showMonitor() {
        if (monitoringWindow != null) {
            monitoringWindow.setVisible(true);
            monitoringWindow.toFront();
        }
    }

    public void setMonitoringWindow(MonitoringWindow monitoringWindow) {
        this.monitoringWindow = monitoringWindow;
        if (this.monitoringWindow != null) {
            this.monitoringWindow.setStatus(this.status);
        }
    }

    // --- NOVO MeTODO ---
    /**
     * Limpa a referência à janela de monitoramento.
     * Chamado quando a janela é fechada pelo usuário sem parar a pipeline.
     */
    public void clearMonitoringWindow() {
        this.monitoringWindow = null;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
        if (this.monitoringWindow != null) {
            this.monitoringWindow.setStatus(status);
        }
    }

    // --- Getters ---
    public String getId() { return id; }
    public String getDescription() { return description; }
    public TaskStatus getStatus() { return status; }
    public MonitoringWindow getMonitoringWindow() { return monitoringWindow; }
}