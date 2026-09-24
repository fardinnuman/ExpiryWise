package com.expirywise.controller;

import com.expirywise.model.Food;
import com.expirywise.service.FoodService;
import com.expirywise.util.ImageUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import java.io.File;
import java.time.LocalDate;

public class EditFoodController {

    @FXML
    private Label errorLabel;

    @FXML
    private TextField foodNameField;

    @FXML
    private DatePicker purchaseDatePicker;

    @FXML
    private DatePicker expiryDatePicker;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private TextField quantityField;

    @FXML
    private ComboBox<String> storageComboBox;

    @FXML
    private ImageView imagePreview;

    @FXML
    private Label imagePlaceholderText;

    @FXML
    private Label imageNameLabel;

    @FXML
    private Button chooseImageButton;

    @FXML
    private Button removeImageButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button saveButton;

    private final FoodService foodService = new FoodService();
    private Food food;
    private String customImagePath;

    @FXML
    public void initialize() {
        categoryComboBox.getItems().setAll(
                "Fruits",
                "Vegetables",
                "Dairy",
                "Meat",
                "Fish",
                "Drinks",
                "Snacks",
                "Frozen Food",
                "Others");

        storageComboBox.getItems().setAll(
                "Fridge",
                "Freezer",
                "Pantry");

        if (foodNameField != null) {
            foodNameField.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ENTER) {
                    handleSave();
                } else if (e.getCode() == KeyCode.ESCAPE) {
                    handleCancel();
                }
            });
        }
    }

    public void setFood(Food food) {
        this.food = food;
        this.customImagePath = food.getImage();

        foodNameField.setText(food.getName() != null ? food.getName() : "");
        quantityField.setText(food.getQuantity() != null ? food.getQuantity() : "1");

        categoryComboBox.setValue(food.getCategory() != null ? food.getCategory() : "Others");
        storageComboBox.setValue(food.getLocation() != null ? food.getLocation() : "Fridge");

        if (food.getPurchaseDate() != null) {
            purchaseDatePicker.setValue(food.getPurchaseDate());
        }

        if (food.getExpiryDate() != null) {
            expiryDatePicker.setValue(food.getExpiryDate());
        }

        updateImageDisplay();
    }

    private void updateImageDisplay() {
        if (customImagePath != null && !customImagePath.isBlank()) {
            File imgFile = new File(customImagePath);
            if (imgFile.exists()) {
                try {
                    imagePreview.setImage(new Image(imgFile.toURI().toString()));
                    imagePreview.setVisible(true);
                    if (imagePlaceholderText != null) imagePlaceholderText.setVisible(false);
                    if (imageNameLabel != null) imageNameLabel.setText(imgFile.getName());
                    if (removeImageButton != null) {
                        removeImageButton.setVisible(true);
                        removeImageButton.setManaged(true);
                    }
                    return;
                } catch (Exception ignored) {
                }
            }
        }

        String category = categoryComboBox.getValue();
        String catPath = getCategoryImagePath(category);
        var res = getClass().getResource(catPath);
        if (res != null) {
            imagePreview.setImage(new Image(res.toExternalForm()));
            imagePreview.setVisible(true);
            if (imagePlaceholderText != null) imagePlaceholderText.setVisible(false);
        } else {
            imagePreview.setImage(null);
            if (imagePlaceholderText != null) imagePlaceholderText.setVisible(true);
        }

        if (imageNameLabel != null) {
            imageNameLabel.setText("Using category default image.");
        }
        if (removeImageButton != null) {
            removeImageButton.setVisible(false);
            removeImageButton.setManaged(false);
        }
    }

    @FXML
    private void handleChooseImage() {
        javafx.stage.Window window = foodNameField.getScene().getWindow();
        String imagePath = ImageUtil.chooseAndSaveImage(window);

        if (imagePath != null) {
            customImagePath = imagePath;
            updateImageDisplay();
        }
    }

    @FXML
    private void handleRemoveImage() {
        customImagePath = null;
        updateImageDisplay();
    }

    @FXML
    private void handleSave() {
        clearError();

        String name = foodNameField.getText() != null ? foodNameField.getText().trim() : "";
        LocalDate expiryDate = expiryDatePicker.getValue();

        if (name.isEmpty()) {
            showError("Food name cannot be empty.", foodNameField);
            return;
        }

        if (expiryDate == null) {
            showError("Please select an expiry date.", expiryDatePicker);
            return;
        }

        food.setName(name);
        food.setQuantity(quantityField.getText() != null && !quantityField.getText().trim().isEmpty()
                ? quantityField.getText().trim() : "1");
        food.setCategory(categoryComboBox.getValue() != null ? categoryComboBox.getValue() : "Others");
        food.setLocation(storageComboBox.getValue() != null ? storageComboBox.getValue() : "Fridge");
        food.setPurchaseDate(purchaseDatePicker.getValue());
        food.setExpiryDate(expiryDate);
        food.setImage(customImagePath);

        foodService.updateFood(food);
        closeWindow();
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        if (cancelButton != null && cancelButton.getScene() != null) {
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
        }
    }

    private void showError(String message, Control field) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
        if (field != null) {
            field.getStyleClass().add("field-error");
            field.requestFocus();
        }
    }

    private void clearError() {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
        foodNameField.getStyleClass().remove("field-error");
        expiryDatePicker.getStyleClass().remove("field-error");
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
}