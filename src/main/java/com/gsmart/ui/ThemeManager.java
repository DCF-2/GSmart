// Localização: src/main/java/com/gsmart/ui/ThemeManager.java
package main.java.com.gsmart.ui;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import main.java.com.gsmart.config.ConfigManager;
import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.util.Properties;

/**
 * Gestor central para a aplicação de temas (Look and Feel) na UI.
 * <p>
 * Esta classe utiliza a biblioteca FlatLaf para gerir e aplicar temas visuais
 * (claro, escuro, padrão do sistema) a toda a aplicação. É também responsável
 * por guardar a preferência de tema do utilizador para que seja restaurada
 * em sessões futuras.
 *
 * @see com.formdev.flatlaf.FlatLaf
 */
public class ThemeManager {

    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";
    public static final String THEME_SYSTEM = "system";
    private static final String THEME_KEY = "ui.theme";

    /**
     * Carrega e aplica o tema que foi guardado na última sessão da aplicação.
     * <p>
     * Utiliza o {@link main.java.com.gsmart.config.ConfigManager} para ler a preferência
     * de tema e, em seguida, chama o método {@link #applyTheme(String)} para o aplicar
     * no arranque da aplicação.
     */
    public static void applySavedTheme() {
        ConfigManager configManager = new ConfigManager();
        Properties props = configManager.loadProperties();
        String theme = props.getProperty(THEME_KEY, THEME_SYSTEM);
        applyTheme(theme);
    }

    /**
     * Aplica um tema específico à aplicação, configurando o Look and Feel do Swing.
     *
     * @param theme A string que identifica o tema a ser aplicado (ex: "light", "dark").
     */
    private static void applyTheme(String theme) {
        try {
            // As chamadas setup() já limpam o estado anterior.
            if (THEME_DARK.equals(theme)) {
                // Para o tema escuro, aplica o tema base. As customizações são lidas pela biblioteca.
                FlatDarculaLaf.setup();
            } else if (THEME_SYSTEM.equals(theme)) {
                FlatIntelliJLaf.setup();
            } else { // Tema Claro
                FlatLightLaf.setup();
            }
        } catch (Exception e) {
            System.err.println("Falha ao aplicar o tema: " + e.getMessage());
        }
    }

    private static void saveThemePreference(String theme) {
        ConfigManager configManager = new ConfigManager();
        Properties props = configManager.loadProperties();
        props.setProperty(THEME_KEY, theme);
        configManager.saveProperties(props);
    }

    /**
     * Altera o tema atual da aplicação, guarda a preferência e atualiza a UI.
     * <p>
     * Este método aplica o novo tema e, em seguida, percorre todas as janelas abertas
     * para forçar a sua atualização, garantindo que as alterações visuais sejam
     * refletidas imediatamente sem a necessidade de reiniciar a aplicação.
     *
     * @param theme A string que identifica o novo tema a ser aplicado.
     */
    public static void changeAndSaveTheme(String theme) {
        applyTheme(theme);
        saveThemePreference(theme);

        for (Window window : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
        }
    }
}