package com.expirywise.controller;

import com.expirywise.model.Food;
import com.expirywise.model.ShoppingItem;
import com.expirywise.service.FoodService;
import com.expirywise.service.ShoppingListService;
import com.expirywise.util.ThemeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CalendarController {

    @FXML
    private Label monthYearLabel;

    @FXML
    private Label monthOverviewChip;

    @FXML
    private Button previousMonthButton;

    @FXML
    private Button nextMonthButton;

    @FXML
    private GridPane calendarGrid;

    @FXML
    private Label selectedDateTitle;

    @FXML
    private Label selectedDateSub;

    @FXML
    private Button addForDateBtn;

    @FXML
    private VBox dayItemsContainer;

    private YearMonth currentMonth;
    private LocalDate selectedDate;
    private final FoodService foodService = new FoodService();
    private final ShoppingListService shoppingListService = new ShoppingListService();

    private final DateTimeFormatter monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM yyyy");
    private final DateTimeFormatter fullDateFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy");

    @FXML
    public void initialize() {
        currentMonth = YearMonth.now();
        selectedDate = LocalDate.now();
        renderCalendar();
        displayDayDetails(selectedDate);
    }

    @FXML
    private void handlePreviousMonth() {
        currentMonth = currentMonth.minusMonths(1);
        renderCalendar();
    }

    @FXML
    private void handleNextMonth() {
        currentMonth = currentMonth.plusMonths(1);
        renderCalendar();
    }

    @FXML
    private void handleToday() {
        currentMonth = YearMonth.now();
        selectedDate = LocalDate.now();
        renderCalendar();
        displayDayDetails(selectedDate);
    }

    @FXML
    private void handleAddForSelectedDate() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/food-dialog.fxml"));
            Parent root = loader.load();

            FoodDialogController controller = loader.getController();
            if (selectedDate != null) {
                controller.setInitialExpiryDate(selectedDate);
            }

            Stage stage = new Stage();
            stage.setTitle("Add Item for " + selectedDate + " — ExpiryWise");
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(root, 620, 680);
            ThemeManager.registerScene(scene);
            ThemeManager.applyStageIcon(stage);

            stage.setScene(scene);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.showAndWait();

            renderCalendar();
            displayDayDetails(selectedDate);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renderCalendar() {
        monthYearLabel.setText(currentMonth.format(monthTitleFormatter));
        calendarGrid.getChildren().clear();

        List<Food> foods = foodService.getAllFoods();

        Map<LocalDate, List<Food>> foodsByDate = new HashMap<>();
        int monthItemCount = 0;

        for (Food food : foods) {
            if (food.getExpiryDate() != null) {
                foodsByDate.computeIfAbsent(food.getExpiryDate(), k -> new ArrayList<>()).add(food);
                if (YearMonth.from(food.getExpiryDate()).equals(currentMonth)) {
                    monthItemCount++;
                }
            }
        }

        if (monthOverviewChip != null) {
            monthOverviewChip.setText(monthItemCount + " items expiring this month");
        }

        LocalDate firstDay = currentMonth.atDay(1);
        int startColumn = firstDay.getDayOfWeek().getValue() % 7;
        int daysInMonth = currentMonth.lengthOfMonth();

        LocalDate today = LocalDate.now();

        for (int day = 1; day <= daysInMonth; day++) {
            int index = startColumn + day - 1;
            int row = index / 7;
            int column = index % 7;

            LocalDate date = currentMonth.atDay(day);
            List<Food> dayFoods = foodsByDate.getOrDefault(date, Collections.emptyList());

            VBox dayCell = createDayCell(date, dayFoods, today);
            calendarGrid.add(dayCell, column, row);
        }
    }

    private VBox createDayCell(LocalDate date, List<Food> dayFoods, LocalDate today) {
        VBox cell = new VBox(2);
        cell.setAlignment(Pos.TOP_LEFT);
        cell.getStyleClass().add("calendar-day-cell");

        if (date.equals(today)) {
            cell.getStyleClass().add("calendar-day-today");
        }

        if (date.equals(selectedDate)) {
            cell.getStyleClass().add("calendar-day-selected");
        }

        HBox topRow = new HBox(4);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label numLabel = new Label(String.valueOf(date.getDayOfMonth()));
        numLabel.getStyleClass().add("calendar-day-number");
        HBox.setHgrow(numLabel, Priority.ALWAYS);

        topRow.getChildren().add(numLabel);

        if (!dayFoods.isEmpty()) {
            String dominantStatus = getDominantStatus(dayFoods);
            Label countPill = new Label(dayFoods.size() == 1 ? "1 item" : dayFoods.size() + " items");
            countPill.getStyleClass().addAll("calendar-count-pill", getStatusDotClass(dominantStatus));
            topRow.getChildren().add(countPill);
        }

        cell.getChildren().add(topRow);

        if (!dayFoods.isEmpty()) {
            VBox previewList = new VBox(2);
            int maxShow = 2;
            for (int i = 0; i < Math.min(maxShow, dayFoods.size()); i++) {
                Food f = dayFoods.get(i);
                String status = foodService.getExpiryStatus(f);

                Label chip = new Label("• " + truncate(f.getName(), 11));
                chip.getStyleClass().addAll("calendar-food-chip", getChipStyleClass(status));
                previewList.getChildren().add(chip);
            }

            if (dayFoods.size() > maxShow) {
                Label more = new Label("+" + (dayFoods.size() - maxShow) + " more");
                more.getStyleClass().add("calendar-more-chip");
                previewList.getChildren().add(more);
            }

            cell.getChildren().add(previewList);
        }

        cell.setOnMouseClicked(event -> {
            selectedDate = date;
            renderCalendar();
            displayDayDetails(date);
        });

        return cell;
    }

    private void displayDayDetails(LocalDate date) {
        if (selectedDateTitle != null) {
            selectedDateTitle.setText(date.format(fullDateFormatter));
        }
        if (selectedDateSub != null) {
            if (date.equals(LocalDate.now())) {
                selectedDateSub.setText("Expires Today — Urgent Attention");
            } else if (date.isBefore(LocalDate.now())) {
                selectedDateSub.setText("Past Expiry Record");
            } else {
                long days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), date);
                selectedDateSub.setText(days == 1 ? "Expires tomorrow" : "Expires in " + days + " days");
            }
        }

        dayItemsContainer.getChildren().clear();

        List<Food> foods = foodService.getAllFoods();
        List<Food> matching = new ArrayList<>();
        for (Food f : foods) {
            if (f.getExpiryDate() != null && f.getExpiryDate().equals(date)) {
                matching.add(f);
            }
        }

        if (matching.isEmpty()) {
            VBox emptyBox = new VBox(6);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new javafx.geometry.Insets(24, 16, 24, 16));

            Label icon = new Label("✨");
            icon.setStyle("-fx-font-size: 28px;");

            Label title = new Label("All Clear for This Day");
            title.getStyleClass().add("empty-side-title");

            Label desc = new Label("No groceries or food items expire on " + date.getMonth().name().substring(0, 3) + " " + date.getDayOfMonth() + ".");
            desc.getStyleClass().add("empty-side-desc");
            desc.setWrapText(true);

            emptyBox.getChildren().addAll(icon, title, desc);
            dayItemsContainer.getChildren().add(emptyBox);
            return;
        }

        for (Food food : matching) {
            HBox itemCard = createSideItemCard(food);
            dayItemsContainer.getChildren().add(itemCard);
        }
    }

    private HBox createSideItemCard(Food food) {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("side-item-card");

        Label categoryIcon = new Label(getCategoryEmoji(food.getCategory()));
        categoryIcon.setStyle("-fx-font-size: 20px;");

        VBox infoBox = new VBox(2);
        Label name = new Label(food.getName());
        name.getStyleClass().add("side-item-name");

        Label details = new Label(
                (food.getCategory() != null ? food.getCategory() : "Other")
                + " • Qty: " + (food.getQuantity() != null ? food.getQuantity() : "1")
                + " • " + (food.getLocation() != null ? food.getLocation() : "Fridge"));
        details.getStyleClass().add("side-item-details");
        infoBox.getChildren().addAll(name, details);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        String status = foodService.getExpiryStatus(food);
        Label badge = new Label(status);
        badge.getStyleClass().addAll("status-badge", getStatusBadgeClass(status));

        HBox actions = new HBox(6);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button shopBtn = new Button("🛒");
        shopBtn.setTooltip(new Tooltip("Add to Shopping List"));
        shopBtn.getStyleClass().addAll("btn-card", "btn-card-shopping");
        shopBtn.setOnAction(e -> {
            shoppingListService.addItem(new ShoppingItem(0, food.getName(), false));
            shopBtn.setText("✓");
        });

        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().addAll("btn-card", "btn-card-edit");
        editBtn.setOnAction(e -> handleEditFood(food));

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().addAll("btn-card", "btn-card-delete");
        deleteBtn.setOnAction(e -> handleDeleteFood(food));

        actions.getChildren().addAll(shopBtn, editBtn, deleteBtn);

        card.getChildren().addAll(categoryIcon, infoBox, badge, actions);
        return card;
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

            renderCalendar();
            displayDayDetails(selectedDate);

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
            renderCalendar();
            displayDayDetails(selectedDate);
        }
    }

    private String getCategoryEmoji(String category) {
        if (category == null) return "🍽️";
        return switch (category.trim()) {
            case "Fruits" -> "🍎";
            case "Vegetables" -> "🥦";
            case "Dairy" -> "🥛";
            case "Meat" -> "🥩";
            case "Fish" -> "🐟";
            case "Drinks" -> "🧃";
            case "Snacks" -> "🥨";
            case "Frozen Food" -> "🧊";
            default -> "🥫";
        };
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen - 1) + "…";
    }

    private String getDominantStatus(List<Food> foods) {
        for (Food f : foods) {
            String s = foodService.getExpiryStatus(f);
            if ("Expired".equals(s)) return "Expired";
        }
        for (Food f : foods) {
            String s = foodService.getExpiryStatus(f);
            if ("Expires Today".equals(s)) return "Expires Today";
        }
        for (Food f : foods) {
            String s = foodService.getExpiryStatus(f);
            if ("Use Soon".equals(s)) return "Use Soon";
        }
        return "Safe";
    }

    private String getStatusDotClass(String status) {
        return switch (status) {
            case "Expired" -> "dot-expired";
            case "Expires Today" -> "dot-today";
            case "Use Soon" -> "dot-soon";
            default -> "dot-safe";
        };
    }

    private String getChipStyleClass(String status) {
        return switch (status) {
            case "Expired" -> "chip-expired";
            case "Expires Today" -> "chip-today";
            case "Use Soon" -> "chip-soon";
            default -> "chip-safe";
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