// Localização: src/main/java/com/gsmart/pipeline/PipelineManager.java
package com.gsmart.pipeline;

import com.gsmart.config.AlertRule;
import com.gsmart.resources.GSmartListener;
import com.gsmart.resources.TaskStatus;
import com.gsmart.config.PipelineConfiguration;
import com.gsmart.Gui.windows.ConnectionErrorDialog;
import com.gsmart.Gui.windows.LogViewerWindow;
import com.gsmart.Gui.windows.MonitoringWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Orquestrador central para o ciclo de vida de múltiplas tarefas de pipeline ({@link PipelineTask}).
 *
 * Esta classe atua como o "maestro" do sistema, responsável por instanciar,
 * gerir e finalizar os processos de monitorização de dados. Ela faz a ponte
 * entre as configurações definidas na {@link com.gsmart.GSmartGui} e a execução
 * real das {@link DataPipeline} em threads separadas.
 *
 * <h3>Principais Responsabilidades:</h3>
 * <ul>
 * <li>Lançar novas tarefas de pipeline com base numa {@link com.gsmart.config.PipelineConfiguration} fornecida pela GUI.</li>
 * <li>Manter e fornecer uma lista atualizada de todas as tarefas em execução.</li>
 * <li>Gerir a comunicação entre a lógica de fundo ({@code DataPipeline}) e a interface gráfica (GUI), utilizando um {@link com.gsmart.resources.GSmartListener}.</li>
 * <li>Controlar a exibição de janelas de monitorização individuais e diálogos de erro de conexão.</li>
 * <li>Fornecer métodos para reiniciar ou parar tarefas de forma segura.</li>
 * </ul>
 *
 * @see com.gsmart.pipeline.PipelineTask
 * @see com.gsmart.pipeline.DataPipeline
 * @see com.gsmart.GSmartGui
 */
public class PipelineManager {

    private static final Logger logger = LoggerFactory.getLogger(PipelineManager.class);
    private final List<PipelineTask> runningTasks = new ArrayList<>();
    private Runnable onTaskListUpdated;
    private LogViewerWindow globalLogViewer;
    private Component parentComponentForDialogs;

    /**
     * Define um callback (Runnable) a ser executado sempre que a lista de tarefas
     * for atualizada (adição ou remoção).
     * Isso é usado para notificar a GUI para redesenhar a lista de tarefas.
     *
     * @param onTaskListUpdated O Runnable a ser executado.
     */
    public void setOnTaskListUpdated(Runnable onTaskListUpdated) { this.onTaskListUpdated = onTaskListUpdated; }


    public void setParentComponent(Component parentComponent) { this.parentComponentForDialogs = parentComponent; }


    public void setGlobalLogViewer(LogViewerWindow logViewer) { this.globalLogViewer = logViewer; }

    private void notifyUpdate() {
        if (onTaskListUpdated != null) SwingUtilities.invokeLater(onTaskListUpdated);
    }

    /**
     * Retorna uma cópia da lista de tarefas atualmente em execução.
     * A lista é copiada para evitar problemas de concorrência (ConcurrentModificationException)
     * ao iterar sobre ela enquanto a original pode ser modificada.
     *
     * @return Uma nova lista contendo as tarefas em execução.
     */
    public List<PipelineTask> getRunningTasks() { return new ArrayList<>(runningTasks); }

    /**
     * Lança um novo pipeline com base em uma configuração fornecida.
     *
     * Este método instancia e configura todos os componentes necessários para uma nova
     * tarefa de monitoramento, incluindo a {@code DataPipeline}, a {@code Thread} de execução
     * e o listener de eventos, encapsulando tudo em um objeto {@code PipelineTask}.
     *
     * @param config O objeto de configuração contendo todos os parâmetros necessários
     * para o pipeline, como a fonte de dados, métricas e URL de destino.
     */
    // Localização: src/com/gsmart/pipeline/PipelineManager.java

    public void launchPipeline(PipelineConfiguration config) {
        String taskDescription = config.dataSource().getSourceName();
        logger.info("Recebida ordem para lançar pipeline: {}", taskDescription);

        final PipelineTask[] taskWrapper = new PipelineTask[1];

        GSmartListener listener = new GSmartListener() {
            @Override public void onInsight(String message, String type) {
                PipelineTask task = taskWrapper[0];
                if (task != null && task.getMonitoringWindow() != null) {
                    task.getMonitoringWindow().onInsight(message, type);
                }
            }
            @Override public void onAlert(String title, String message) {
                PipelineTask task = taskWrapper[0];
                if (task != null) {
                    task.setHasAlert(true);
                    if (task.getMonitoringWindow() != null) {
                        task.getMonitoringWindow().onAlert(title, message);
                    }
                    notifyUpdate();
                }
            }
            @Override public void onStatusUpdate(TaskStatus status) {
                PipelineTask task = taskWrapper[0];
                if (task != null) {
                    task.setStatus(status);
                }
                notifyUpdate();
            }
            @Override
            public void onConnectionLost(String errorMessage) {
                PipelineTask task = taskWrapper[0];
                if (task == null) return;

                if (task.getConnectionErrorDialog() == null || !task.getConnectionErrorDialog().isDisplayable()) {
                    SwingUtilities.invokeLater(() -> {
                        ConnectionErrorDialog newDialog = new ConnectionErrorDialog(
                                (Frame) parentComponentForDialogs, task.getDescription(),
                                task::forceReconnect,
                                task::stop,
                                task::clearConnectionErrorDialog);
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


        DataPipeline pipeline = new DataPipeline(config.dataSource(), config.powerBiUrl(), config.metricConfigs(), listener, config.alertRules(), config.insightRules());

        Thread pipelineThread = new Thread(() -> {
            try {
                pipeline.run();
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

    /**
     * Para uma tarefa antiga e lança uma nova com base na configuração original da tarefa.
     * Muito útil para reiniciar um pipeline que parou ou encontrou um erro.
     *
     * @param oldTask A tarefa existente que precisa ser reiniciada.
     */
    public void relaunchPipeline(PipelineTask oldTask) {
        logger.info("Recebida ordem para reiniciar a pipeline: {}", oldTask.getDescription());
        PipelineConfiguration config = oldTask.getOriginalConfig();
        runningTasks.remove(oldTask);
        notifyUpdate();
        launchPipeline(config);
    }

    /**
     * Exibe a janela de monitoramento para uma tarefa específica.
     *
     * Se uma janela de monitoramento para esta tarefa ainda não existir ou tiver sido
     * fechada, uma nova é criada. Se já existir, a janela existente é trazida
     * para a frente, garantindo que apenas uma instância do monitor seja exibida por tarefa.
     *
     * @param task A tarefa para a qual o monitor deve ser exibido.
     */
    public void showMonitorFor(PipelineTask task) {
        if (task.getMonitoringWindow() == null || !task.getMonitoringWindow().isDisplayable()) {
            logger.info("Criando/Recriando janela de monitoramento para: {}", task.getDescription());
            Consumer<MonitoringWindow> onDisposeRequest = (closedWindow) -> task.clearMonitoringWindow();
            MonitoringWindow newMonitor = new MonitoringWindow(task, onDisposeRequest);
            task.setMonitoringWindow(newMonitor);
        }
        task.getMonitoringWindow().setVisible(true);
    }

    /**
     * Tenta parar todas as tarefas de monitoramento que estão em execução.
     *
     * Exibe um diálogo de confirmação ao utilizador antes de prosseguir. Se a ação for
     * confirmada, o metodo {@code stop()} de cada tarefa ativa é invocado para
     - * garantir uma finalização segura.
     + * garantir uma finalização segura.
     */
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