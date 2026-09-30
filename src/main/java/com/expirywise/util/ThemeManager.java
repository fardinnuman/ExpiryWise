package com.expirywise.util;

import javafx.scene.Scene;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class ThemeManager {

    private static final String PREF_DARK_MODE = "darkMode";
    private static final String PREF_EXPIRY_THRESHOLD = "expiryThresholdDays";
    private static final Preferences preferences = Preferences.userNodeForPackage(ThemeManager.class);

    private static final List<Scene> registeredScenes = new ArrayList<>();
    private static final List<Runnable> themeChangeListeners = new ArrayList<>();

    private static String lightStylesheet;
    private static String darkStylesheet;

    static {
        try {
            var lightRes = ThemeManager.class.getResource("/css/style.css");
            if (lightRes != null) {
                lightStylesheet = lightRes.toExternalForm();
            }
            var darkRes = ThemeManager.class.getResource("/css/dark.css");
            if (darkRes != null) {
                darkStylesheet = darkRes.toExternalForm();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isDarkMode() {
        return preferences.getBoolean(PREF_DARK_MODE, false);
    }

    public static void setDarkMode(boolean darkMode) {
        preferences.putBoolean(PREF_DARK_MODE, darkMode);
        applyToAllRegistered();
        for (Runnable listener : new ArrayList<>(themeChangeListeners)) {
            try {
                listener.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static int getExpiryThresholdDays() {
        return preferences.getInt(PREF_EXPIRY_THRESHOLD, 3);
    }

    public static void setExpiryThresholdDays(int days) {
        preferences.putInt(PREF_EXPIRY_THRESHOLD, days);
        for (Runnable listener : new ArrayList<>(themeChangeListeners)) {
            try {
                listener.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void addThemeChangeListener(Runnable listener) {
        if (!themeChangeListeners.contains(listener)) {
            themeChangeListeners.add(listener);
        }
    }

    public static void removeThemeChangeListener(Runnable listener) {
        themeChangeListeners.remove(listener);
    }

    public static void registerScene(Scene scene) {
        if (scene != null && !registeredScenes.contains(scene)) {
            registeredScenes.add(scene);
            applyTheme(scene);
            scene.windowProperty().addListener((obs, oldWin, newWin) -> {
                if (newWin == null) {
                    registeredScenes.remove(scene);
                }
            });
        }
    }

    public static void applyTheme(Scene scene) {
        if (scene == null) {
            return;
        }

        scene.getStylesheets().remove(darkStylesheet);
        if (lightStylesheet != null && !scene.getStylesheets().contains(lightStylesheet)) {
            scene.getStylesheets().add(0, lightStylesheet);
        }

        if (isDarkMode() && darkStylesheet != null) {
            if (!scene.getStylesheets().contains(darkStylesheet)) {
                scene.getStylesheets().add(darkStylesheet);
            }
        }
    }

    public static void applyTheme(DialogPane dialogPane) {
        if (dialogPane == null) {
            return;
        }

        dialogPane.getStylesheets().remove(darkStylesheet);
        if (lightStylesheet != null && !dialogPane.getStylesheets().contains(lightStylesheet)) {
            dialogPane.getStylesheets().add(0, lightStylesheet);
        }

        if (isDarkMode() && darkStylesheet != null) {
            if (!dialogPane.getStylesheets().contains(darkStylesheet)) {
                dialogPane.getStylesheets().add(darkStylesheet);
            }
            if (!dialogPane.getStyleClass().contains("dark-alert")) {
                dialogPane.getStyleClass().add("dark-alert");
            }
        } else {
            dialogPane.getStyleClass().remove("dark-alert");
        }
    }

    private static void applyToAllRegistered() {
        registeredScenes.removeIf(scene -> scene.getWindow() == null);
        for (Scene scene : registeredScenes) {
            applyTheme(scene);
        }
    }

    public static void applyStageIcon(Stage stage) {
        try {
            InputStream iconStream = ThemeManager.class.getResourceAsStream("/images/icon/ExpiryWise.jpg");
            if (iconStream != null) {
                stage.getIcons().add(new Image(iconStream));
            }
        } catch (Exception ignored) {
        }
    }
}