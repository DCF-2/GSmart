// Localização: src/main/java/com/gsmart/ui/LogViewerAppender.java
package main.java.com.gsmart.Gui.windows;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import main.java.com.gsmart.Gui.windows.LogViewerWindow;

/**
 * Um Appender personalizado do Logback que envia as mensagens de log
 * diretamente para a nossa janela LogViewerWindow.
 */
public class LogViewerAppender extends AppenderBase<ILoggingEvent> {

    private static LogViewerWindow logViewer;

    /**
     * Regista a instância da janela de log para que o appender saiba para onde enviar as mensagens.
     * @param viewer A instância de LogViewerWindow.
     */
    public static void setLogViewer(LogViewerWindow viewer) {
        logViewer = viewer;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        if (logViewer != null && logViewer.isDisplayable()) {
            // Envia a mensagem formatada para a janela de log
            logViewer.appendText(eventObject.getFormattedMessage() + "\n");
        }
    }
}