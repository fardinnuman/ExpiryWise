package com.expirywise.controller;

import com.expirywise.model.Food;
import com.expirywise.service.FoodService;
import com.expirywise.util.ThemeManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class SettingsController {

    @FXML
    private CheckBox darkModeCheckBox;

    @FXML
    private ComboBox<String> thresholdComboBox;

    private final FoodService foodService = new FoodService();

    @FXML
    public void initialize() {
        if (darkModeCheckBox != null) {
            darkModeCheckBox.setSelected(ThemeManager.isDarkMode());
            darkModeCheckBox.setOnAction(event -> {
                ThemeManager.setDarkMode(darkModeCheckBox.isSelected());
            });
        }

        if (thresholdComboBox != null) {
            thresholdComboBox.getItems().setAll(
                    "2 Days in advance",
                    "3 Days in advance (Default)",
                    "5 Days in advance",
                    "7 Days in advance");

            int currentDays = ThemeManager.getExpiryThresholdDays();
            switch (currentDays) {
                case 2 -> thresholdComboBox.getSelectionModel().select("2 Days in advance");
                case 5 -> thresholdComboBox.getSelectionModel().select("5 Days in advance");
                case 7 -> thresholdComboBox.getSelectionModel().select("7 Days in advance");
                default -> thresholdComboBox.getSelectionModel().select("3 Days in advance (Default)");
            }

            thresholdComboBox.setOnAction(event -> {
                String val = thresholdComboBox.getValue();
                if (val != null) {
                    if (val.startsWith("2"))
                        ThemeManager.setExpiryThresholdDays(2);
                    else if (val.startsWith("5"))
                        ThemeManager.setExpiryThresholdDays(5);
                    else if (val.startsWith("7"))
                        ThemeManager.setExpiryThresholdDays(7);
                    else
                        ThemeManager.setExpiryThresholdDays(3);
                }
            });
        }
    }

    @FXML
    public void handleExportCsv() {
        Window window = darkModeCheckBox.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Food Inventory to CSV");
        fileChooser.setInitialFileName("ExpiryWise_Inventory_" + LocalDate.now() + ".csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));

        File file = fileChooser.showSaveDialog(window);
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8)) {
                writer.println("ID,Item Name,Category,Quantity,Location,Purchase Date,Expiry Date,Status,Favorite");
                List<Food> foods = foodService.getAllFoods();
                for (Food f : foods) {
                    writer.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\",%s,%s,\"%s\",%b%n",
                            f.getId(),
                            escapeCsv(f.getName()),
                            escapeCsv(f.getCategory()),
                            escapeCsv(f.getQuantity()),
                            escapeCsv(f.getLocation()),
                            f.getPurchaseDate() != null ? f.getPurchaseDate().toString() : "",
                            f.getExpiryDate() != null ? f.getExpiryDate().toString() : "",
                            foodService.getExpiryStatus(f),
                            f.isFav());
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                ThemeManager.applyTheme(alert.getDialogPane());
                alert.setTitle("Export Completed");
                alert.setHeaderText("Inventory Exported Successfully");
                alert.setContentText("Exported " + foods.size() + " items to: " + file.getName());
                alert.showAndWait();

            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                ThemeManager.applyTheme(alert.getDialogPane());
                alert.setTitle("Export Failed");
                alert.setHeaderText("Failed to write CSV file");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }

    @FXML
    public void handleLoadSampleData() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        ThemeManager.applyTheme(confirm.getDialogPane());
        confirm.setTitle("Load Sample Pantry Data");
        confirm.setHeaderText("Add demo groceries to your inventory?");
        confirm.setContentText("This will add realistic items across safe, expiring soon, and expired statuses.");

        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            LocalDate today = LocalDate.now();

            Food[] samples = new Food[] {
                    new Food(0, "Yogurt", "Dairy", "500g", today.minusDays(5), today.plusDays(10), "Fridge", true,
                            null),
                    new Food(0, "Avocado", "Fruits", "4 pcs", today.minusDays(3), today.plusDays(2), "Pantry", true,
                            null),
                    new Food(0, "Milk", "Dairy", "1 Liter", today.minusDays(6), today, "Fridge", false, null),
                    new Food(0, "Salmon", "Fish", "400g", today.minusDays(1), today.plusDays(1), "Fridge", false, null),
                    new Food(0, "Bread", "Dairy", "1 loaf", today.minusDays(7), today.minusDays(1), "Pantry", false,
                            null),
                    new Food(0, "Spinach", "Vegetables", "250g", today.minusDays(2), today.plusDays(4), "Fridge", false,
                            null),
                    new Food(0, "Eggs", "Dairy", "12 pcs", today.minusDays(4), today.plusDays(14), "Fridge", true,
                            null),
                    new Food(0, "Chicken", "Meat", "800g", today.minusDays(2), today.plusDays(1), "Fridge", false,
                            null),
                    new Food(0, "Orange Juice", "Drinks", "1 Liter", today.minusDays(8), today.minusDays(2), "Fridge",
                            false, null),
                    new Food(0, "Strawberry", "Fruits", "1 box", today.minusDays(4), today.plusDays(3), "Fridge", true,
                            null),
                    new Food(0, "Frozen Peas", "Frozen Food", "1 kg", today.minusDays(20), today.plusDays(90),
                            "Freezer", false, null)
            };

            for (Food food : samples) {
                foodService.addFood(food);
            }

            Alert done = new Alert(Alert.AlertType.INFORMATION);
            ThemeManager.applyTheme(done.getDialogPane());
            done.setTitle("Sample Data Added");
            done.setHeaderText("11 Sample Items Populated");
            done.setContentText("Sample groceries with varied expiry dates are now loaded in your inventory.");
            done.showAndWait();
        }
    }

    @FXML
    public void handleClearAllData() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        ThemeManager.applyTheme(alert.getDialogPane());
        alert.setTitle("Clear All Pantry Data");
        alert.setHeaderText("Are you absolutely sure?");
        alert.setContentText("This will permanently delete all items from your database.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            List<Food> foods = foodService.getAllFoods();
            for (Food f : foods) {
                foodService.deleteFood(f.getId());
            }

            Alert info = new Alert(Alert.AlertType.INFORMATION);
            ThemeManager.applyTheme(info.getDialogPane());
            info.setTitle("Inventory Cleared");
            info.setHeaderText("Database Reset");
            info.setContentText("All food inventory items have been deleted.");
            info.showAndWait();
        }
    }

    private String escapeCsv(String val) {
        if (val == null)
            return "";
        return val.replace("\"", "\"\"");
    }
}