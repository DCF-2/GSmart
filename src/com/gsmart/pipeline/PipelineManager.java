// Localização: src/main/java/com/gsmart/pipeline/PipelineManager.java
package com.gsmart.pipeline;

import com.gsmart.GSmartListener;
import com.gsmart.TaskStatus;
import com.gsmart.config.PipelineConfiguration;
import com.gsmart.windows.ConnectionErrorDialog;
import com.gsmart.windows.LogViewerWindow;
import com.gsmart.windows.MonitoringWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PipelineManager {

    private static final Logger logger = LoggerFactory.getLogger(PipelineManager.class);
    private final List<PipelineTask> runningTasks = new ArrayList<>();
    private Runnable onTaskListUpdated;
    private LogViewerWindow globalLogViewer;
    private Component parentComponentForDialogs;

    public void setOnTaskListUpdated(Runnable onTaskListUpdated) { this.onTaskListUpdated = onTaskListUpdated; }
    public void setParentComponent(Component parentComponent) { this.parentComponentForDialogs = parentComponent; }
    public void setGlobalLogViewer(LogViewerWindow logViewer) { this.globalLogViewer = logViewer; }

    private void notifyUpdate() {
        if (onTaskListUpdated != null) SwingUtilities.invokeLater(onTaskListUpdated);
    }

    public List<PipelineTask> getRunningTasks() { return new ArrayList<>(runningTasks); }

    public void launchPipeline(PipelineConfiguration config) {
        String taskDescription = config.dataSource().getSourceName();
        logger.info("Recebida ordem para lançar pipeline: {}", taskDescription);

        final PipelineTask[] taskWrapper = new PipelineTask[1];

        GSmartListener listener = new GSmartListener() {
            @Override public void onInsight(String message, String type) {
                // --- INÍCIO DA MUDANÇA ---
                PipelineTask task = taskWrapper[0];
                if (task != null && task.getMonitoringWindow() != null) {
                    task.getMonitoringWindow().onInsight(message, type);
                }
                // --- FIM DA MUDANÇA ---
            }
            @Override public void onAlert(String title, String message) {
                // --- INÍCIO DA MUDANÇA ---
                PipelineTask task = taskWrapper[0];
                if (task != null) {
                    task.setHasAlert(true); // Marca que a tarefa tem um alerta
                    if (task.getMonitoringWindow() != null) {
                        task.getMonitoringWindow().onAlert(title, message);
                    }
                    notifyUpdate(); // Notifica a TaskManagerWindow para redesenhar
                }
                // --- FIM DA MUDANÇA ---
            }

            @Override public void onStatusUpdate(TaskStatus status) {
                // --- INÍCIO DA MUDANÇA ---
                PipelineTask task = taskWrapper[0];
                if (task != null) {
                    task.setStatus(status);
                }
                notifyUpdate();
                // --- FIM DA MUDANÇA ---
            }

            @Override
            public void onConnectionLost(String errorMessage) {
                PipelineTask task = taskWrapper[0];
                if (task == null) return;

                if (task.getConnectionErrorDialog() == null || !task.getConnectionErrorDialog().isDisplayable()) {
                    SwingUtilities.invokeLater(() -> {
                        ConnectionErrorDialog newDialog = new ConnectionErrorDialog(
                                (Frame) parentComponentForDialogs, task.getDescription(),
                                task::forceReconnect, task::stop, task::clearConnectionErrorDialog);
                        task.setConnectionErrorDialog(newDialog);
                        newDialog.showConnectionLost(errorMessage);
                    });
                }
            }

            @Override
            public void onReconnectionAttempt(long delayInSeconds) {
                PipelineTask task = taskWrapper[0];
                if (task != null && task.getConnectionErrorDialog() != null) {
                    task.getConnectionErrorDialog().startCountdown(delayInSeconds);
                }
            }

            @Override
            public void onConnectionRestored() {
                PipelineTask task = taskWrapper[0];
                if (task != null && task.getConnectionErrorDialog() != null) {
                    task.getConnectionErrorDialog().showConnectionRestored();
                }
                if (task != null) {
                    task.setStatus(TaskStatus.RUNNING);
                }
                notifyUpdate();
            }
        };
        DataPipeline pipeline = new DataPipeline(config.dataSource(), config.powerBiUrl(), config.metricConfigs(), config.logicConfig(), listener, config.runBusinessLogic());

        Thread pipelineThread = new Thread(() -> {
            try {
                pipeline.run(); // A thread executa a instância da pipeline
            } catch (Exception e) {
                logger.error("Erro inesperado na thread da pipeline.", e);
            } finally {
                logger.info("Pipeline thread para '{}' foi finalizada.", taskDescription);
                if (taskWrapper[0] != null) {
                    taskWrapper[0].stop();
                }
            }
        });

        PipelineTask task = new PipelineTask(taskDescription, pipelineThread, pipeline, config, () -> {
            this.runningTasks.remove(taskWrapper[0]);
            notifyUpdate();
        });
        taskWrapper[0] = task;

        this.runningTasks.add(task);
        notifyUpdate();

        pipelineThread.start();
        logger.info("Pipeline para '{}' iniciada em segundo plano.", taskDescription);
    }

    public void relaunchPipeline(PipelineTask oldTask) {
        logger.info("Recebida ordem para reiniciar a pipeline: {}", oldTask.getDescription());
        PipelineConfiguration config = oldTask.getOriginalConfig();
        runningTasks.remove(oldTask);
        notifyUpdate();
        launchPipeline(config);
    }

    public void showMonitorFor(PipelineTask task) {
        if (task.getMonitoringWindow() == null || !task.getMonitoringWindow().isDisplayable()) {
            logger.info("Criando/Recriando janela de monitoramento para: {}", task.getDescription());
            Consumer<MonitoringWindow> onDisposeRequest = (closedWindow) -> task.clearMonitoringWindow();
            MonitoringWindow newMonitor = new MonitoringWindow(task, onDisposeRequest);
            task.setMonitoringWindow(newMonitor);
        }
        task.getMonitoringWindow().setVisible(true);
    }

    public void stopAllPipelines() {
        if (runningTasks.isEmpty()) {
            JOptionPane.showMessageDialog(parentComponentForDialogs, "Não há nenhum monitoramento em execução.", "Informação", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(parentComponentForDialogs,
                "Você tem certeza que deseja parar " + runningTasks.size() + " processo(s) de monitoramento?",
                "Confirmar Parada Geral",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            logger.warn("Sinal de parada geral enviado pelo usuário para {} monitores.", runningTasks.size());
            List<PipelineTask> tasksToStop = new ArrayList<>(runningTasks);
            for (PipelineTask task : tasksToStop) {
                task.stop();
            }
        }
    }
}