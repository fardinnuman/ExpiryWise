package com.expirywise.controller;

import com.expirywise.model.Food;
import com.expirywise.service.FoodService;
import com.expirywise.util.ImageUtil;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import java.io.File;
import java.time.LocalDate;

public class FoodDialogController {

    @FXML
    private Label dialogTitle;

    @FXML
    private Label errorLabel;

    @FXML
    private TextField nameField;

    @FXML
    private DatePicker purchaseDatePicker;

    @FXML
    private DatePicker expiryDatePicker;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private TextField quantityField;

    @FXML
    private ComboBox<String> locationComboBox;

    @FXML
    private ImageView imagePreview;

    @FXML
    private Label imagePlaceholderText;

    @FXML
    private Label imageNameLabel;

    @FXML
    private Button removeImageButton;

    private String selectedImagePath;
    private final FoodService foodService = new FoodService();

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

        locationComboBox.getItems().setAll(
                "Fridge",
                "Freezer",
                "Pantry");

        categoryComboBox.getSelectionModel().select("Fruits");
        locationComboBox.getSelectionModel().select("Fridge");

        purchaseDatePicker.setValue(LocalDate.now());

        if (nameField != null) {
            nameField.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ENTER) {
                    handleSave();
                } else if (e.getCode() == KeyCode.ESCAPE) {
                    handleCancel();
                }
            });
        }
    }

    public void setInitialExpiryDate(LocalDate date) {
        if (expiryDatePicker != null && date != null) {
            expiryDatePicker.setValue(date);
        }
    }

    @FXML
    private void handleChooseImage() {
        javafx.stage.Window window = nameField.getScene().getWindow();
        String imagePath = ImageUtil.chooseAndSaveImage(window);

        if (imagePath != null) {
            selectedImagePath = imagePath;
            File imageFile = new File(imagePath);

            try {
                imagePreview.setImage(new Image(imageFile.toURI().toString()));
                imagePreview.setVisible(true);
                if (imagePlaceholderText != null) imagePlaceholderText.setVisible(false);
                if (removeImageButton != null) {
                    removeImageButton.setVisible(true);
                    removeImageButton.setManaged(true);
                }
                imageNameLabel.setText(imageFile.getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleRemoveImage() {
        selectedImagePath = null;
        imagePreview.setImage(null);
        if (imagePlaceholderText != null) imagePlaceholderText.setVisible(true);
        if (removeImageButton != null) {
            removeImageButton.setVisible(false);
            removeImageButton.setManaged(false);
        }
        imageNameLabel.setText("If none chosen, category default image is used.");
    }

    @FXML
    private void handleSave() {
        clearError();

        String name = nameField.getText() != null ? nameField.getText().trim() : "";
        LocalDate expiryDate = expiryDatePicker.getValue();

        if (name.isEmpty()) {
            showError("Please provide a food item name.", nameField);
            return;
        }

        if (expiryDate == null) {
            showError("Please select an expiry date.", expiryDatePicker);
            return;
        }

        String category = categoryComboBox.getValue() != null ? categoryComboBox.getValue() : "Others";
        String quantity = quantityField.getText() != null && !quantityField.getText().trim().isEmpty()
                ? quantityField.getText().trim() : "1";
        String location = locationComboBox.getValue() != null ? locationComboBox.getValue() : "Fridge";
        LocalDate purchaseDate = purchaseDatePicker.getValue();

        Food food = new Food(
                0,
                name,
                category,
                quantity,
                purchaseDate,
                expiryDate,
                location,
                false,
                selectedImagePath);

        foodService.addFood(food);
        closeDialog();
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Node source = nameField;
        if (source != null && source.getScene() != null && source.getScene().getWindow() != null) {
            Stage stage = (Stage) source.getScene().getWindow();
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
        nameField.getStyleClass().remove("field-error");
        expiryDatePicker.getStyleClass().remove("field-error");
    }
}