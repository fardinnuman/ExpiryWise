package com.expirywise.controller;

import com.expirywise.model.Food;
import com.expirywise.model.ShoppingItem;
import com.expirywise.service.FoodService;
import com.expirywise.service.ShoppingListService;
import com.expirywise.util.ThemeManager;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class DashboardController {

    @FXML
    private Label totalItemsLabel;

    @FXML
    private Label safeLabel;

    @FXML
    private Label useSoonLabel;

    @FXML
    private Label expiresTodayLabel;

    @FXML
    private Label expiredLabel;

    @FXML
    private TextField searchField;

    @FXML
    private Button clearSearchButton;

    @FXML
    private ComboBox<String> categoryFilter;

    @FXML
    private ComboBox<String> statusFilter;

    @FXML
    private ComboBox<String> sortBy;

    @FXML
    private Label inventorySectionLabel;

    @FXML
    private Label filterCountLabel;

    @FXML
    private Button resetFiltersButton;

    @FXML
    private ScrollPane dashboardScrollPane;

    @FXML
    private GridPane foodGrid;

    @FXML
    private VBox emptyStateContainer;

    @FXML
    private Label emptyStateIcon;

    @FXML
    private Label emptyStateTitle;

    @FXML
    private Label emptyStateSubtitle;

    @FXML
    private Button emptyStateActionBtn;

    private final FoodService foodService = new FoodService();
    private final ShoppingListService shoppingListService = new ShoppingListService();

    private List<Food> allFoodsCache = new ArrayList<>();
    private int currentGridColumns = 3;

    @FXML
    public void initialize() {
        initFilterControls();
        initResponsiveGrid();
        loadDashboard();
    }

    private void initFilterControls() {
        if (categoryFilter != null) {
            categoryFilter.getItems().setAll(
                    "All Categories",
                    "Fruits",
                    "Vegetables",
                    "Dairy",
                    "Meat",
                    "Fish",
                    "Drinks",
                    "Snacks",
                    "Frozen Food",
                    "Others");
            categoryFilter.getSelectionModel().selectFirst();
            categoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> refreshFoodCards());
        }

        if (statusFilter != null) {
            statusFilter.getItems().setAll(
                    "All Status",
                    "Safe",
                    "Use Soon",
                    "Expires Today",
                    "Expired");
            statusFilter.getSelectionModel().selectFirst();
            statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> refreshFoodCards());
        }

        if (sortBy != null) {
            sortBy.getItems().setAll(
                    "Sort by Expiry",
                    "Sort by Name",
                    "Sort by Category",
                    "Favorites First");
            sortBy.getSelectionModel().selectFirst();
            sortBy.valueProperty().addListener((obs, oldVal, newVal) -> refreshFoodCards());
        }

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (clearSearchButton != null) {
                    boolean hasText = newVal != null && !newVal.trim().isEmpty();
                    clearSearchButton.setVisible(hasText);
                    clearSearchButton.setManaged(hasText);
                }
                refreshFoodCards();
            });
        }
    }

    private void initResponsiveGrid() {
        if (dashboardScrollPane != null) {
            dashboardScrollPane.widthProperty().addListener((obs, oldVal, newVal) -> {
                double width = newVal.doubleValue();
                int newCols;
                if (width < 620) {
                    newCols = 1;
                } else if (width < 930) {
                    newCols = 2;
                } else if (width < 1280) {
                    newCols = 3;
                } else {
                    newCols = 4;
                }

                if (newCols != currentGridColumns) {
                    currentGridColumns = newCols;
                    refreshFoodCards();
                }
            });
        }
    }

    public void loadDashboard() {
        allFoodsCache = foodService.getAllFoods();
        updateStatistics(allFoodsCache);
        refreshFoodCards();
    }

    private void updateStatistics(List<Food> foods) {
        int safe = 0;
        int useSoon = 0;
        int expiresToday = 0;
        int expired = 0;

        for (Food food : foods) {
            String status = foodService.getExpiryStatus(food);
            switch (status) {
                case "Safe" -> safe++;
                case "Use Soon" -> useSoon++;
                case "Expires Today" -> expiresToday++;
                case "Expired" -> expired++;
            }
        }

        if (totalItemsLabel != null)
            totalItemsLabel.setText(String.valueOf(foods.size()));
        if (safeLabel != null)
            safeLabel.setText(String.valueOf(safe));
        if (useSoonLabel != null)
            useSoonLabel.setText(String.valueOf(useSoon));
        if (expiresTodayLabel != null)
            expiresTodayLabel.setText(String.valueOf(expiresToday));
        if (expiredLabel != null)
            expiredLabel.setText(String.valueOf(expired));
    }

    private void refreshFoodCards() {
        List<Food> displayedFoods = new ArrayList<>(allFoodsCache);

        String query = searchField != null && searchField.getText() != null
                ? searchField.getText().trim().toLowerCase()
                : "";
        String selectedCategory = categoryFilter != null ? categoryFilter.getValue() : "All Categories";
        String selectedStatus = statusFilter != null ? statusFilter.getValue() : "All Status";
        String selectedSort = sortBy != null ? sortBy.getValue() : "Sort by Expiry";

        boolean isFiltering = !query.isEmpty()
                || (selectedCategory != null && !selectedCategory.equals("All Categories"))
                || (selectedStatus != null && !selectedStatus.equals("All Status"));

        if (resetFiltersButton != null) {
            resetFiltersButton.setVisible(isFiltering);
            resetFiltersButton.setManaged(isFiltering);
        }

        if (!query.isEmpty()) {
            displayedFoods.removeIf(f -> (f.getName() == null || !f.getName().toLowerCase().contains(query)) &&
                    (f.getCategory() == null || !f.getCategory().toLowerCase().contains(query)) &&
                    (f.getLocation() == null || !f.getLocation().toLowerCase().contains(query)));
        }

        if (selectedCategory != null && !selectedCategory.equals("All Categories")) {
            displayedFoods
                    .removeIf(f -> f.getCategory() == null || !f.getCategory().equalsIgnoreCase(selectedCategory));
        }

        if (selectedStatus != null && !selectedStatus.equals("All Status")) {
            displayedFoods.removeIf(f -> !foodService.getExpiryStatus(f).equalsIgnoreCase(selectedStatus));
        }

        if ("Sort by Name".equals(selectedSort)) {
            displayedFoods.sort(Comparator.comparing(f -> f.getName() != null ? f.getName().toLowerCase() : ""));
        } else if ("Sort by Category".equals(selectedSort)) {
            displayedFoods
                    .sort(Comparator.comparing(f -> f.getCategory() != null ? f.getCategory().toLowerCase() : ""));
        } else if ("Favorites First".equals(selectedSort)) {
            displayedFoods.sort((a, b) -> Boolean.compare(b.isFav(), a.isFav()));
        } else {
            displayedFoods
                    .sort(Comparator.comparing(Food::getExpiryDate, Comparator.nullsLast(Comparator.naturalOrder())));
        }

        if (inventorySectionLabel != null) {
            inventorySectionLabel.setText("Pantry Inventory");
        }
        if (filterCountLabel != null) {
            if (isFiltering) {
                filterCountLabel.setText("Showing " + displayedFoods.size() + " of " + allFoodsCache.size());
            } else {
                filterCountLabel.setText("(" + displayedFoods.size() + " items)");
            }
        }

        if (displayedFoods.isEmpty()) {
            foodGrid.getChildren().clear();
            foodGrid.setVisible(false);
            foodGrid.setManaged(false);

            if (emptyStateContainer != null) {
                emptyStateContainer.setVisible(true);
                emptyStateContainer.setManaged(true);

                if (allFoodsCache.isEmpty()) {
                    emptyStateIcon.setText("🥫");
                    emptyStateTitle.setText("Your Pantry is Empty");
                    emptyStateSubtitle.setText(
                            "Start by adding groceries and food items to track their shelf-life and avoid waste.");
                    emptyStateActionBtn.setText("+ Add First Food Item");
                    emptyStateActionBtn.setOnAction(e -> handleAddFood());
                } else {
                    emptyStateIcon.setText("🔍");
                    emptyStateTitle.setText("No Matching Food Items");
                    emptyStateSubtitle.setText(
                            "No items matched your current search or status filters. Try clearing your filters.");
                    emptyStateActionBtn.setText("Clear Filters");
                    emptyStateActionBtn.setOnAction(e -> handleResetFilters());
                }
            }
            return;
        }

        if (emptyStateContainer != null) {
            emptyStateContainer.setVisible(false);
            emptyStateContainer.setManaged(false);
        }

        foodGrid.setVisible(true);
        foodGrid.setManaged(true);
        loadFoodCards(displayedFoods);
    }

    private void loadFoodCards(List<Food> foods) {
        foodGrid.getChildren().clear();

        int cols = Math.max(1, currentGridColumns);
        int col = 0;
        int row = 0;

        for (Food food : foods) {
            VBox card = createFoodCard(food);
            foodGrid.add(card, col, row);

            col++;
            if (col >= cols) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createFoodCard(Food food) {
        VBox card = new VBox(0);
        card.getStyleClass().add("food-card");

        StackPane imagePane = new StackPane();
        imagePane.getStyleClass().add("food-card-image-pane");
        imagePane.setPrefHeight(140);
        imagePane.setMinHeight(140);
        imagePane.setMaxHeight(140);

        Image img = resolveFoodImage(food);
        ImageView imageView = new ImageView(img);
        imageView.fitWidthProperty().bind(imagePane.widthProperty());
        imageView.setFitHeight(140);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(imagePane.widthProperty());
        clip.heightProperty().bind(imagePane.heightProperty());
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        imagePane.setClip(clip);

        imagePane.getChildren().add(imageView);

        String status = foodService.getExpiryStatus(food);
        Label statusBadge = new Label(getStatusWithIcon(status));
        statusBadge.getStyleClass().addAll("status-badge", getStatusStyleClass(status));
        StackPane.setAlignment(statusBadge, Pos.TOP_LEFT);
        StackPane.setMargin(statusBadge, new javafx.geometry.Insets(10, 0, 0, 10));

        Button favButton = new Button(food.isFav() ? "♥" : "♡");
        favButton.getStyleClass().add("card-fav-button");
        if (food.isFav()) {
            favButton.getStyleClass().add("card-fav-active");
        }
        favButton.setOnAction(e -> {
            food.setFav(!food.isFav());
            foodService.updateFood(food);
            favButton.setText(food.isFav() ? "♥" : "♡");
            if (food.isFav()) {
                favButton.getStyleClass().add("card-fav-active");
            } else {
                favButton.getStyleClass().remove("card-fav-active");
            }
        });
        StackPane.setAlignment(favButton, Pos.TOP_RIGHT);
        StackPane.setMargin(favButton, new javafx.geometry.Insets(8, 8, 0, 0));

        imagePane.getChildren().addAll(statusBadge, favButton);

        VBox body = new VBox(8);
        body.getStyleClass().add("food-card-body");
        body.setPadding(new javafx.geometry.Insets(12, 14, 14, 14));

        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(food.getName());
        nameLabel.getStyleClass().add("food-name");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        Label qtyLabel = new Label(
                food.getQuantity() != null && !food.getQuantity().isBlank() ? food.getQuantity() : "1");
        qtyLabel.getStyleClass().add("food-qty-chip");

        titleRow.getChildren().addAll(nameLabel, qtyLabel);

        HBox tagsRow = new HBox(6);
        tagsRow.setAlignment(Pos.CENTER_LEFT);

        Label catTag = new Label(food.getCategory() != null ? food.getCategory() : "Other");
        catTag.getStyleClass().add("food-tag");

        String locEmoji = switch (food.getLocation() != null ? food.getLocation() : "") {
            case "Fridge" -> "❄️ Fridge";
            case "Freezer" -> "🧊 Freezer";
            case "Pantry" -> "🥫 Pantry";
            default -> "📍 " + (food.getLocation() != null ? food.getLocation() : "Kitchen");
        };
        Label locTag = new Label(locEmoji);
        locTag.getStyleClass().add("food-tag");

        tagsRow.getChildren().addAll(catTag, locTag);

        VBox dateBox = new VBox(4);
        String relativeExpiry = foodService.getExpiryBadgeText(food);
        Label expiryLabel = new Label(relativeExpiry);
        expiryLabel.getStyleClass().addAll("food-expiry-countdown", getExpiryCountdownClass(status));

        Label datesSubtext = new Label(
                "Expires: " + (food.getExpiryDate() != null ? food.getExpiryDate().toString() : "N/A"));
        datesSubtext.getStyleClass().add("food-date-sub");

        ProgressBar freshnessBar = new ProgressBar(foodService.getFreshnessProgress(food));
        freshnessBar.setMaxWidth(Double.MAX_VALUE);
        freshnessBar.getStyleClass().addAll("freshness-progress", getFreshnessProgressClass(status));

        dateBox.getChildren().addAll(expiryLabel, freshnessBar, datesSubtext);

        HBox actionsRow = new HBox(8);
        actionsRow.setAlignment(Pos.CENTER_LEFT);

        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().addAll("btn-card", "btn-card-edit");
        editBtn.setOnAction(e -> handleEditFood(food));
        HBox.setHgrow(editBtn, Priority.ALWAYS);
        editBtn.setMaxWidth(Double.MAX_VALUE);

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().addAll("btn-card", "btn-card-delete");
        deleteBtn.setOnAction(e -> handleDeleteFood(food));

        Button addShoppingBtn = new Button("🛒");
        addShoppingBtn.setTooltip(new Tooltip("Add to Shopping List"));
        addShoppingBtn.getStyleClass().addAll("btn-card", "btn-card-shopping");
        addShoppingBtn.setOnAction(e -> handleAddFoodToShopping(food, addShoppingBtn));

        actionsRow.getChildren().addAll(editBtn, deleteBtn, addShoppingBtn);

        body.getChildren().addAll(titleRow, tagsRow, dateBox, actionsRow);

        card.getChildren().addAll(imagePane, body);
        return card;
    }

    private void handleAddFoodToShopping(Food food, Button btn) {
        String itemName = food.getName();
        if (food.getQuantity() != null && !food.getQuantity().isBlank()) {
            itemName += " (" + food.getQuantity() + ")";
        }

        ShoppingItem item = new ShoppingItem(0, itemName, false);
        shoppingListService.addItem(item);

        btn.setText("✓");
        btn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white;");

        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.millis(1500));
        pause.setOnFinished(e -> {
            btn.setText("🛒");
            btn.setStyle("");
        });
        pause.play();
    }

    private Image resolveFoodImage(Food food) {
        if (food.getImage() != null && !food.getImage().isBlank()) {
            File customFile = new File(food.getImage());
            if (customFile.exists()) {
                try {
                    return new Image(customFile.toURI().toString(), 300, 150, true, true);
                } catch (Exception ignored) {
                }
            }
        }

        String catPath = getCategoryImagePath(food.getCategory());
        var res = getClass().getResource(catPath);
        if (res != null) {
            return new Image(res.toExternalForm(), 300, 150, true, true);
        }

        return new Image(getClass().getResource("/images/category/Others.jpg").toExternalForm(), 300, 150, true, true);
    }

    private String getCategoryImagePath(String category) {
        if (category == null || category.trim().isEmpty()) {
            return "/images/category/Others.jpg";
        }
        return switch (category.trim()) {
            case "Fruits" -> "/images/category/Fruits.jpg";
            case "Vegetables" -> "/images/category/Vegetables.jpg";
            case "Dairy" -> "/images/category/Dairy.jpg";
            case "Meat" -> "/images/category/Meat.jpg";
            case "Fish" -> "/images/category/Fish.jpg";
            case "Drinks" -> "/images/category/Drinks.jpg";
            case "Snacks" -> "/images/category/Snacks.jpg";
            case "Frozen Food" -> "/images/category/Frozen Food.jpg";
            default -> "/images/category/Others.jpg";
        };
    }

    private String getStatusWithIcon(String status) {
        return switch (status) {
            case "Safe" -> "● Safe";
            case "Use Soon" -> "▲ Use Soon";
            case "Expires Today" -> "⚡ Today";
            case "Expired" -> "✕ Expired";
            default -> status;
        };
    }

    private String getStatusStyleClass(String status) {
        return switch (status) {
            case "Safe" -> "status-safe";
            case "Use Soon" -> "status-soon";
            case "Expires Today" -> "status-today";
            case "Expired" -> "status-expired";
            default -> "status-safe";
        };
    }

    private String getExpiryCountdownClass(String status) {
        return switch (status) {
            case "Safe" -> "countdown-safe";
            case "Use Soon" -> "countdown-soon";
            case "Expires Today" -> "countdown-today";
            case "Expired" -> "countdown-expired";
            default -> "";
        };
    }

    private String getFreshnessProgressClass(String status) {
        return switch (status) {
            case "Safe" -> "progress-safe";
            case "Use Soon" -> "progress-soon";
            case "Expires Today" -> "progress-today";
            case "Expired" -> "progress-expired";
            default -> "";
        };
    }

    @FXML
    public void handleClearSearch() {
        if (searchField != null) {
            searchField.clear();
        }
    }

    @FXML
    public void handleResetFilters() {
        if (searchField != null)
            searchField.clear();
        if (categoryFilter != null)
            categoryFilter.getSelectionModel().selectFirst();
        if (statusFilter != null)
            statusFilter.getSelectionModel().selectFirst();
        if (sortBy != null)
            sortBy.getSelectionModel().selectFirst();
    }

    @FXML
    public void handleAddFood() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/food-dialog.fxml"));

            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add Food Item — ExpiryWise");
            dialogStage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(root, 620, 680);
            ThemeManager.registerScene(scene);
            ThemeManager.applyStageIcon(dialogStage);

            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            dialogStage.centerOnScreen();

            dialogStage.showAndWait();

            loadDashboard();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleEditFood(Food food) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/edit-food.fxml"));

            Parent root = loader.load();

            EditFoodController controller = loader.getController();
            controller.setFood(food);

            Stage stage = new Stage();
            stage.setTitle("Edit " + food.getName() + " — ExpiryWise");
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(root, 620, 680);
            ThemeManager.registerScene(scene);
            ThemeManager.applyStageIcon(stage);

            stage.setScene(scene);
            stage.setResizable(false);
            stage.centerOnScreen();

            stage.showAndWait();

            loadDashboard();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteFood(Food food) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        ThemeManager.applyTheme(confirmation.getDialogPane());

        confirmation.setTitle("Delete Food Item");
        confirmation.setHeaderText("Delete \"" + food.getName() + "\"?");
        confirmation.setContentText("This item will be permanently removed from your inventory.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            foodService.deleteFood(food.getId());
            loadDashboard();
        }
    }
}