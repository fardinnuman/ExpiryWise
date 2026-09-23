package com.expirywise.controller;

import com.expirywise.model.Food;
import com.expirywise.service.FoodService;
import com.expirywise.util.ThemeManager;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MainController {

    @FXML
    private VBox contentArea;

    @FXML
    private Button btnDashboard;

    @FXML
    private Button btnAnalytics;

    @FXML
    private Button btnCalendar;

    @FXML
    private Button btnShoppingList;

    @FXML
    private Button btnNotifications;

    @FXML
    private Button btnSettings;

    @FXML
    private Label notificationBadge;

    @FXML
    private Label sidebarPantryStats;

    private final FoodService foodService = new FoodService();
    private Button currentActiveBtn;

    @FXML
    private void initialize() {
        contentArea.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                ThemeManager.registerScene(newScene);
            }
        });

        ThemeManager.addThemeChangeListener(this::refreshNotificationBadge);

        showDashboard();
        refreshNotificationBadge();
    }

    @FXML
    public void showDashboard() {
        loadPage("dashboard.fxml", btnDashboard);
    }

    @FXML
    public void showAnalytics() {
        loadPage("analytics.fxml", btnAnalytics);
    }

    @FXML
    public void showCalendar() {
        loadPage("calendar.fxml", btnCalendar);
    }

    @FXML
    public void showShoppingList() {
        loadPage("shopping-list.fxml", btnShoppingList);
    }

    @FXML
    public void showNotifications() {
        loadPage("notifications.fxml", btnNotifications);
    }

    @FXML
    public void showSettings() {
        loadPage("settings.fxml", btnSettings);
    }

    @FXML
    public void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        ThemeManager.applyTheme(alert.getDialogPane());

        alert.setTitle("Sign Out");
        alert.setHeaderText("Sign out of ExpiryWise?");
        alert.setContentText("You will be returned to the sign-in screen.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/login.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) contentArea.getScene().getWindow();
                Scene scene = new Scene(root, 460, 600);
                ThemeManager.registerScene(scene);

                stage.setMinWidth(460);
                stage.setMinHeight(600);
                stage.setWidth(460);
                stage.setHeight(600);
                stage.setScene(scene);
                stage.setTitle("ExpiryWise");
                stage.setResizable(false);
                stage.centerOnScreen();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void refreshNotificationBadge() {
        Platform.runLater(() -> {
            try {
                List<Food> foods = foodService.getAllFoods();
                int urgentCount = 0;
                for (Food food : foods) {
                    String status = foodService.getExpiryStatus(food);
                    if ("Expired".equals(status) || "Expires Today".equals(status) || "Use Soon".equals(status)) {
                        urgentCount++;
                    }
                }

                if (notificationBadge != null) {
                    if (urgentCount > 0) {
                        notificationBadge.setText(String.valueOf(urgentCount));
                        notificationBadge.setVisible(true);
                        notificationBadge.setManaged(true);
                    } else {
                        notificationBadge.setVisible(false);
                        notificationBadge.setManaged(false);
                    }
                }

                if (sidebarPantryStats != null) {
                    sidebarPantryStats.setText(foods.size() + " items tracked");
                }
            } catch (Exception ignored) {
            }
        });
    }

    private void loadPage(String page, Button targetBtn) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/" + page));

            Parent view = loader.load();
            VBox.setVgrow(view, Priority.ALWAYS);

            contentArea.getChildren().setAll(view);

            FadeTransition ft = new FadeTransition(Duration.millis(160), view);
            ft.setFromValue(0.4);
            ft.setToValue(1.0);
            ft.play();

            setActiveNav(targetBtn);
            refreshNotificationBadge();

        } catch (IOException | NullPointerException e) {
            System.err.println("Failed to load view: " + page);
            e.printStackTrace();
        }
    }

    private void setActiveNav(Button targetBtn) {
        if (targetBtn == null)
            return;

        List<Button> buttons = Arrays.asList(
                btnDashboard, btnAnalytics, btnCalendar,
                btnShoppingList, btnNotifications, btnSettings);

        for (Button btn : buttons) {
            if (btn != null) {
                btn.getStyleClass().remove("sidebar-item-active");
            }
        }

        targetBtn.getStyleClass().add("sidebar-item-active");
        currentActiveBtn = targetBtn;
    }
}