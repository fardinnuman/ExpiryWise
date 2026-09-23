package com.expirywise.controller;

import com.expirywise.model.Food;
import com.expirywise.model.ShoppingItem;
import com.expirywise.service.FoodService;
import com.expirywise.service.ShoppingListService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.List;

public class ShoppingListController {

    @FXML
    private TextField itemField;

    @FXML
    private Button addButton;

    @FXML
    private Button clearCompletedButton;

    @FXML
    private Label progressLabel;

    @FXML
    private ProgressBar shoppingProgressBar;

    @FXML
    private Button filterAllBtn;

    @FXML
    private Button filterActiveBtn;

    @FXML
    private Button filterCompletedBtn;

    @FXML
    private VBox shoppingItemsContainer;

    @FXML
    private VBox emptyStateContainer;

    private final ShoppingListService shoppingListService = new ShoppingListService();
    private final FoodService foodService = new FoodService();

    private enum FilterMode {
        ALL, ACTIVE, COMPLETED
    }

    private FilterMode currentFilter = FilterMode.ALL;

    @FXML
    public void initialize() {
        if (itemField != null) {
            itemField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    handleAddItem();
                }
            });
        }

        loadShoppingItems();
    }

    public void loadShoppingItems() {
        shoppingItemsContainer.getChildren().clear();

        List<ShoppingItem> allItems = shoppingListService.getAllItems();
        int total = allItems.size();
        int completed = 0;
        for (ShoppingItem item : allItems) {
            if (item.isCompleted())
                completed++;
        }

        updateProgress(total, completed);

        List<ShoppingItem> filteredList = new ArrayList<>();
        for (ShoppingItem item : allItems) {
            if (currentFilter == FilterMode.ACTIVE && item.isCompleted())
                continue;
            if (currentFilter == FilterMode.COMPLETED && !item.isCompleted())
                continue;
            filteredList.add(item);
        }

        if (filteredList.isEmpty()) {
            if (emptyStateContainer != null) {
                emptyStateContainer.setVisible(true);
                emptyStateContainer.setManaged(true);
            }
            shoppingItemsContainer.setVisible(false);
            shoppingItemsContainer.setManaged(false);
            return;
        }

        if (emptyStateContainer != null) {
            emptyStateContainer.setVisible(false);
            emptyStateContainer.setManaged(false);
        }
        shoppingItemsContainer.setVisible(true);
        shoppingItemsContainer.setManaged(true);

        for (ShoppingItem item : filteredList) {
            createShoppingItemRow(item);
        }
    }

    private void updateProgress(int total, int completed) {
        if (progressLabel != null) {
            if (total == 0) {
                progressLabel.setText("No items in list");
            } else {
                int pct = (int) Math.round(((double) completed / total) * 100);
                progressLabel.setText(completed + " of " + total + " completed (" + pct + "%)");
            }
        }
        if (shoppingProgressBar != null) {
            shoppingProgressBar.setProgress(total > 0 ? (double) completed / total : 0.0);
        }
    }

    @FXML
    private void handleAddItem() {
        String itemName = itemField.getText() != null ? itemField.getText().trim() : "";
        if (itemName.isEmpty()) {
            return;
        }

        ShoppingItem item = new ShoppingItem(0, itemName, false);
        shoppingListService.addItem(item);
        itemField.clear();

        loadShoppingItems();
    }

    @FXML
    private void handleClearCompleted() {
        shoppingListService.deleteCompletedItems();
        loadShoppingItems();
    }

    @FXML
    private void handleAutoAddExpiring() {
        List<Food> foods = foodService.getAllFoods();
        List<ShoppingItem> existing = shoppingListService.getAllItems();
        int added = 0;

        for (Food food : foods) {
            String status = foodService.getExpiryStatus(food);
            if ("Expired".equals(status) || "Expires Today".equals(status) || "Use Soon".equals(status)) {
                String name = food.getName();
                boolean alreadyInList = existing.stream().anyMatch(i -> i.getItemName().equalsIgnoreCase(name));
                if (!alreadyInList) {
                    ShoppingItem newItem = new ShoppingItem(0, name, false);
                    shoppingListService.addItem(newItem);
                    existing.add(newItem);
                    added++;
                }
            }
        }

        loadShoppingItems();
    }

    @FXML
    public void handleFilterAll() {
        currentFilter = FilterMode.ALL;
        updateFilterTabs(filterAllBtn);
        loadShoppingItems();
    }

    @FXML
    public void handleFilterActive() {
        currentFilter = FilterMode.ACTIVE;
        updateFilterTabs(filterActiveBtn);
        loadShoppingItems();
    }

    @FXML
    public void handleFilterCompleted() {
        currentFilter = FilterMode.COMPLETED;
        updateFilterTabs(filterCompletedBtn);
        loadShoppingItems();
    }

    private void updateFilterTabs(Button activeBtn) {
        Button[] buttons = { filterAllBtn, filterActiveBtn, filterCompletedBtn };
        for (Button btn : buttons) {
            if (btn != null)
                btn.getStyleClass().remove("filter-tab-active");
        }
        if (activeBtn != null)
            activeBtn.getStyleClass().add("filter-tab-active");
    }

    private void createShoppingItemRow(ShoppingItem item) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("shopping-item-card");

        CheckBox checkBox = new CheckBox(item.getItemName());
        checkBox.setSelected(item.isCompleted());
        checkBox.getStyleClass().add("shopping-checkbox");
        if (item.isCompleted()) {
            checkBox.getStyleClass().add("shopping-item-completed");
            row.getStyleClass().add("shopping-row-completed");
        }
        HBox.setHgrow(checkBox, Priority.ALWAYS);

        Button deleteBtn = new Button("✕");
        deleteBtn.getStyleClass().add("btn-delete-item");
        deleteBtn.setOnAction(e -> {
            shoppingListService.deleteItem(item.getId());
            loadShoppingItems();
        });

        checkBox.setOnAction(e -> {
            item.setCompleted(checkBox.isSelected());
            shoppingListService.updateItem(item);
            if (checkBox.isSelected()) {
                checkBox.getStyleClass().add("shopping-item-completed");
                row.getStyleClass().add("shopping-row-completed");
            } else {
                checkBox.getStyleClass().remove("shopping-item-completed");
                row.getStyleClass().remove("shopping-row-completed");
            }

            List<ShoppingItem> allItems = shoppingListService.getAllItems();
            int completed = (int) allItems.stream().filter(ShoppingItem::isCompleted).count();
            updateProgress(allItems.size(), completed);

            if (currentFilter != FilterMode.ALL) {
                loadShoppingItems();
            }
        });

        row.getChildren().addAll(checkBox, deleteBtn);
        shoppingItemsContainer.getChildren().add(row);
    }
}
