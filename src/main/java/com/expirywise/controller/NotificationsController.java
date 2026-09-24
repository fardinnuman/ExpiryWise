package com.expirywise.controller;

import com.expirywise.model.Food;
import com.expirywise.model.ShoppingItem;
import com.expirywise.service.FoodService;
import com.expirywise.service.ShoppingListService;
import com.expirywise.util.ThemeManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NotificationsController {

    @FXML
    private Label alertCountBadge;

    @FXML
    private Button filterAllBtn;

    @FXML
    private Button filterCriticalBtn;

    @FXML
    private Button filterSoonBtn;

    @FXML
    private VBox notificationsContainer;

    @FXML
    private VBox emptyStateContainer;

    private final FoodService foodService = new FoodService();
    private final ShoppingListService shoppingListService = new ShoppingListService();

    private enum FilterMode { ALL, CRITICAL, SOON }
    private FilterMode currentFilter = FilterMode.ALL;

    @FXML
    public void initialize() {
        loadNotifications();
    }

    public void loadNotifications() {
        notificationsContainer.getChildren().clear();

        List<Food> foods = foodService.getAllFoods();
        List<Food> alertFoods = new ArrayList<>();

        for (Food food : foods) {
            String status = foodService.getExpiryStatus(food);
            if ("Expired".equals(status) || "Expires Today".equals(status) || "Use Soon".equals(status)) {
                alertFoods.add(food);
            }
        }

        alertFoods.sort((a, b) -> {
            int priorityA = getUrgencyPriority(foodService.getExpiryStatus(a));
            int priorityB = getUrgencyPriority(foodService.getExpiryStatus(b));
            if (priorityA != priorityB) {
                return Integer.compare(priorityA, priorityB);
            }
            return a.getExpiryDate().compareTo(b.getExpiryDate());
        });

        if (alertCountBadge != null) {
            alertCountBadge.setText(alertFoods.size() + " Alerts");
            if (alertFoods.isEmpty()) {
                alertCountBadge.setStyle("-fx-background-color: #e8f7ee; -fx-text-fill: #10b981;");
            } else {
                alertCountBadge.setStyle("");
            }
        }

        List<Food> displayedList = new ArrayList<>();
        for (Food food : alertFoods) {
            String status = foodService.getExpiryStatus(food);
            if (currentFilter == FilterMode.CRITICAL && !"Expired".equals(status) && !"Expires Today".equals(status)) {
                continue;
            }
            if (currentFilter == FilterMode.SOON && !"Use Soon".equals(status)) {
                continue;
            }
            displayedList.add(food);
        }

        if (displayedList.isEmpty()) {
            if (emptyStateContainer != null) {
                emptyStateContainer.setVisible(true);
                emptyStateContainer.setManaged(true);
            }
            notificationsContainer.setVisible(false);
            notificationsContainer.setManaged(false);
            return;
        }

        if (emptyStateContainer != null) {
            emptyStateContainer.setVisible(false);
            emptyStateContainer.setManaged(false);
        }
        notificationsContainer.setVisible(true);
        notificationsContainer.setManaged(true);

        for (Food food : displayedList) {
            addNotificationCard(food);
        }
    }

    private int getUrgencyPriority(String status) {
        return switch (status) {
            case "Expired" -> 1;
            case "Expires Today" -> 2;
            case "Use Soon" -> 3;
            default -> 4;
        };
    }

    @FXML
    public void handleFilterAll() {
        currentFilter = FilterMode.ALL;
        updateFilterTabs(filterAllBtn);
        loadNotifications();
    }

    @FXML
    public void handleFilterCritical() {
        currentFilter = FilterMode.CRITICAL;
        updateFilterTabs(filterCriticalBtn);
        loadNotifications();
    }

    @FXML
    public void handleFilterSoon() {
        currentFilter = FilterMode.SOON;
        updateFilterTabs(filterSoonBtn);
        loadNotifications();
    }

    private void updateFilterTabs(Button activeBtn) {
        Button[] buttons = { filterAllBtn, filterCriticalBtn, filterSoonBtn };
        for (Button btn : buttons) {
            if (btn != null) btn.getStyleClass().remove("filter-tab-active");
        }
        if (activeBtn != null) activeBtn.getStyleClass().add("filter-tab-active");
    }

    private void addNotificationCard(Food food) {
        String status = foodService.getExpiryStatus(food);

        HBox card = new HBox(14);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().addAll("notification-card", getNotificationCardClass(status));

        Label iconLabel = new Label(getStatusIcon(status));
        iconLabel.getStyleClass().addAll("notification-icon", getNotificationIconClass(status));

        VBox infoBox = new VBox(3);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(food.getName() + " " + getStatusHeadline(status));
        title.getStyleClass().add("notification-title");
        titleRow.getChildren().add(title);

        Label badge = new Label(status);
        badge.getStyleClass().addAll("notification-badge", getStatusBadgeClass(status));
        titleRow.getChildren().add(badge);

        String relativeTime = foodService.getExpiryBadgeText(food);
        Label desc = new Label(food.getCategory() + " • Stored in " + food.getLocation()
                + " • Qty: " + food.getQuantity() + " • " + relativeTime + " (" + food.getExpiryDate() + ")");
        desc.getStyleClass().add("notification-description");

        infoBox.getChildren().addAll(titleRow, desc);

        HBox actionBox = new HBox(8);
        actionBox.setAlignment(Pos.CENTER_RIGHT);

        Button addToShoppingBtn = new Button("🛒 Add to List");
        addToShoppingBtn.getStyleClass().addAll("btn-card", "btn-card-shopping");
        addToShoppingBtn.setOnAction(e -> {
            shoppingListService.addItem(new ShoppingItem(0, food.getName(), false));
            addToShoppingBtn.setText("✓ Added");
            addToShoppingBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white;");
        });

        Button deleteBtn = new Button("Discard");
        deleteBtn.getStyleClass().addAll("btn-card", "btn-card-delete");
        deleteBtn.setOnAction(e -> {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            ThemeManager.applyTheme(confirmation.getDialogPane());
            confirmation.setTitle("Discard Food Item");
            confirmation.setHeaderText("Remove \"" + food.getName() + "\" from inventory?");
            confirmation.setContentText("This item will be deleted from your food inventory.");

            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                foodService.deleteFood(food.getId());
                loadNotifications();
            }
        });

        actionBox.getChildren().addAll(addToShoppingBtn, deleteBtn);

        card.getChildren().addAll(iconLabel, infoBox, actionBox);
        notificationsContainer.getChildren().add(card);
    }

    private String getStatusHeadline(String status) {
        return switch (status) {
            case "Expired" -> "has expired";
            case "Expires Today" -> "expires today!";
            case "Use Soon" -> "is expiring soon";
            default -> "status update";
        };
    }

    private String getStatusIcon(String status) {
        return switch (status) {
            case "Expired" -> "✕";
            case "Expires Today" -> "⚡";
            case "Use Soon" -> "▲";
            default -> "●";
        };
    }

    private String getNotificationCardClass(String status) {
        return switch (status) {
            case "Expired" -> "notification-card-expired";
            case "Expires Today" -> "notification-card-today";
            case "Use Soon" -> "notification-card-soon";
            default -> "";
        };
    }

    private String getNotificationIconClass(String status) {
        return switch (status) {
            case "Expired" -> "icon-expired";
            case "Expires Today" -> "icon-today";
            case "Use Soon" -> "icon-soon";
            default -> "";
        };
    }

    private String getStatusBadgeClass(String status) {
        return switch (status) {
            case "Expired" -> "status-expired";
            case "Expires Today" -> "status-today";
            case "Use Soon" -> "status-soon";
            default -> "status-safe";
        };
    }
}