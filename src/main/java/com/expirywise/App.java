package com.expirywise;

import com.expirywise.database.DatabaseManager;
import com.expirywise.util.ThemeManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.prefs.Preferences;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        DatabaseManager.initializeDatabase();

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/login.fxml"));

        Parent root = loader.load();

        Scene scene = new Scene(root, 460, 600);
        ThemeManager.registerScene(scene);

        ThemeManager.applyStageIcon(stage);
        stage.setTitle("ExpiryWise");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}