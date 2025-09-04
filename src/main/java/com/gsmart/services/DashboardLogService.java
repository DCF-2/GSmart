// Localização: java/com/gsmart/services/DashboardLogService.java
package main.java.com.gsmart.services;

import main.java.com.gsmart.Gui.panels.DashboardPanel;
import java.awt.Color;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Serviço centralizado (Singleton) para gerir as mensagens de log
 * que são exibidas no painel do Dashboard.
 */
public class DashboardLogService {

    private static DashboardLogService instance;
    private DashboardPanel dashboardPanel;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    private DashboardLogService() {
        // Construtor privado para garantir o padrão Singleton
    }

    /**
     * Obtém a instância única do serviço.
     * @return A instância do DashboardLogService.
     */
    public static DashboardLogService getInstance() {
        if (instance == null) {
            instance = new DashboardLogService();
        }
        return instance;
    }

    /**
     * Regista o painel do Dashboard no serviço para que ele possa receber as mensagens.
     * Deve ser chamado uma única vez durante a inicialização da UI.
     * @param panel O DashboardPanel da aplicação.
     */
    public void registerDashboardPanel(DashboardPanel panel) {
        this.dashboardPanel = panel;
    }

    // --- Métodos de Log Públicos ---

    public void logInfo(String message) {
        log(message, Color.BLUE);
    }

    public void logAlert(String message) {
        log(message, Color.RED);
    }

    public void logAlarm(String message) {
        log(message, new Color(255, 140, 0)); // Laranja
    }

    public void logSuccess(String message) {
        log(message, new Color(0, 150, 0)); // Verde escuro
    }

    /**
     * Método principal que formata e envia a mensagem para o Dashboard.
     * @param message A mensagem original.
     * @param color A cor a ser usada.
     */
    private void log(String message, Color color) {
        if (dashboardPanel != null) {
            String timestamp = LocalTime.now().format(timeFormatter);
            String formattedMessage = String.format("[%s] %s", timestamp, message);
            dashboardPanel.addLogMessage(formattedMessage, color);
        }
    }
}