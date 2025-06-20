// Localização: src/main/java/com/gsmart/PipelineManager.java
package com.gsmart;

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

    public void setOnTaskListUpdated(Runnable onTaskListUpdated) {
        this.onTaskListUpdated = onTaskListUpdated;
    }

    public void setParentComponent(Component parentComponent) {
        this.parentComponentForDialogs = parentComponent;
    }

    public void setGlobalLogViewer(LogViewerWindow logViewer) {
        this.globalLogViewer = logViewer;
    }

    private void notifyUpdate() {
        if (onTaskListUpdated != null) {
            SwingUtilities.invokeLater(onTaskListUpdated);
        }
    }

    public List<PipelineTask> getRunningTasks() {
        return new ArrayList<>(runningTasks);
    }

    public void launchPipeline(PipelineConfiguration config) {
        String taskDescription = config.dataSource().getSourceName();
        logger.info("Recebida ordem para lançar pipeline: {}", taskDescription);

        final PipelineTask[] taskWrapper = new PipelineTask[1];

        GSmartListener listener = new GSmartListener() {
            @Override
            public void onInsight(String message, String type) {
                PipelineTask task = taskWrapper[0];
                if (task != null && task.getMonitoringWindow() != null) {
                    task.getMonitoringWindow().onInsight(message, type);
                }
            }

            @Override
            public void onAlert(String title, String message) {
                PipelineTask task = taskWrapper[0];
                if (task != null && task.getMonitoringWindow() != null) {
                    task.getMonitoringWindow().onAlert(title, message);
                } else {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(parentComponentForDialogs, message, title, JOptionPane.WARNING_MESSAGE));
                }
            }
        };

        DataPipeline pipeline = new DataPipeline(
                config.dataSource(), config.powerBiUrl(), config.acumuloKey(),
                config.metricConfigs(), config.logicConfig(), listener
        );

        Thread pipelineThread = new Thread(() -> {
            try {
                pipeline.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                logger.info("Pipeline thread para '{}' foi finalizada.", taskDescription);
                if (taskWrapper[0] != null) {
                    taskWrapper[0].stop();
                }
            }
        });

        PipelineTask task = new PipelineTask(
                taskDescription, pipelineThread,
                () -> {
                    this.runningTasks.remove(taskWrapper[0]);
                    notifyUpdate();
                }
        );
        taskWrapper[0] = task;

        this.runningTasks.add(task);
        notifyUpdate();

        pipelineThread.start();
        logger.info("Pipeline para '{}' iniciada em segundo plano.", taskDescription);
    }

    public void showMonitorFor(PipelineTask task) {
        if (task.getMonitoringWindow() == null || !task.getMonitoringWindow().isDisplayable()) {
            logger.info("Criando nova janela de monitoramento para a tarefa: {}", task.getDescription());

            Runnable onStopRequest = task::stop;

            Consumer<MonitoringWindow> onDisposeRequest = (closedWindow) -> {
                task.clearMonitoringWindow();
                logger.info("Janela de monitoramento para '{}' foi fechada.", task.getDescription());
            };

            MonitoringWindow newMonitor = new MonitoringWindow(
                    "Monitor: " + task.getDescription(),
                    this.globalLogViewer,
                    onStopRequest,
                    onDisposeRequest
            );

            task.setMonitoringWindow(newMonitor);
        }

        task.showMonitor();
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