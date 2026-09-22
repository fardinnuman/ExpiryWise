package com.expirywise.controller;

import com.expirywise.dao.UserDAO;
import com.expirywise.util.ThemeManager;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;

public class LoginController {

    @FXML
    private VBox nameFieldBox;

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private VBox confirmPasswordBox;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button actionButton;

    @FXML
    private Button demoButton;

    @FXML
    private Button toggleModeButton;

    @FXML
    private Label subtitleLabel;

    @FXML
    private Label errorLabel;

    @FXML
    private Label successLabel;

    private boolean isRegisterMode = false;
    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        if (emailField != null) {
            emailField.setText("demo@expirywise.app");
            emailField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    passwordField.requestFocus();
                }
            });
        }

        if (passwordField != null) {
            passwordField.setText("password123");
            passwordField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    if (isRegisterMode) {
                        confirmPasswordField.requestFocus();
                    } else {
                        handlePrimaryAction();
                    }
                }
            });
        }

        if (nameField != null) {
            nameField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    emailField.requestFocus();
                }
            });
        }

        if (confirmPasswordField != null) {
            confirmPasswordField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    handlePrimaryAction();
                }
            });
        }
    }

    @FXML
    private void handlePrimaryAction() {
        clearMessages();
        if (isRegisterMode) {
            handleRegister();
        } else {
            handleSignIn();
        }
    }

    @FXML
    private void handleToggleMode() {
        clearMessages();
        isRegisterMode = !isRegisterMode;

        if (isRegisterMode) {
            nameFieldBox.setVisible(true);
            nameFieldBox.setManaged(true);
            confirmPasswordBox.setVisible(true);
            confirmPasswordBox.setManaged(true);

            actionButton.setText("Create Account");
            toggleModeButton.setText("Already have an account? Sign In");
            demoButton.setVisible(false);
            demoButton.setManaged(false);

            subtitleLabel.setText("Create an account to manage your kitchen");
            emailField.setText("");
            passwordField.setText("");
            if (nameField != null) {
                nameField.setText("");
                nameField.requestFocus();
            }
            if (confirmPasswordField != null) {
                confirmPasswordField.setText("");
            }
        } else {
            nameFieldBox.setVisible(false);
            nameFieldBox.setManaged(false);
            confirmPasswordBox.setVisible(false);
            confirmPasswordBox.setManaged(false);

            actionButton.setText("Sign In");
            toggleModeButton.setText("Don't have an account? Create one");
            demoButton.setVisible(true);
            demoButton.setManaged(true);

            subtitleLabel.setText("Smart Food Expiry & Inventory Management");
            emailField.setText("demo@expirywise.app");
            passwordField.setText("password123");
            emailField.requestFocus();
        }

        if (actionButton != null && actionButton.getScene() != null && actionButton.getScene().getWindow() != null) {
            Stage stage = (Stage) actionButton.getScene().getWindow();
            stage.sizeToScene();
        }
    }

    private void handleSignIn() {
        String email = emailField != null ? emailField.getText().trim() : "";
        String password = passwordField != null ? passwordField.getText() : "";

        if (email.isEmpty()) {
            showError("Please enter your email address.");
            if (emailField != null)
                emailField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            showError("Please enter your password.");
            if (passwordField != null)
                passwordField.requestFocus();
            return;
        }

        boolean authenticated = userDAO.authenticate(email, password);
        if (authenticated) {
            proceedToMain();
        } else {
            showError("Invalid email or password. Please check your credentials or create an account.");
        }
    }

    private void handleRegister() {
        String name = nameField != null ? nameField.getText().trim() : "";
        String email = emailField != null ? emailField.getText().trim() : "";
        String password = passwordField != null ? passwordField.getText() : "";
        String confirmPassword = confirmPasswordField != null ? confirmPasswordField.getText() : "";

        if (name.isEmpty()) {
            showError("Please enter your name.");
            if (nameField != null)
                nameField.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            showError("Please enter your email address.");
            if (emailField != null)
                emailField.requestFocus();
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            showError("Please enter a valid email address.");
            if (emailField != null)
                emailField.requestFocus();
            return;
        }

        if (userDAO.emailExists(email)) {
            showError("An account with this email already exists. Please sign in.");
            return;
        }

        if (password.isEmpty()) {
            showError("Please enter a password.");
            if (passwordField != null)
                passwordField.requestFocus();
            return;
        }

        if (password.length() < 4) {
            showError("Password must be at least 4 characters.");
            if (passwordField != null)
                passwordField.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match.");
            if (confirmPasswordField != null)
                confirmPasswordField.requestFocus();
            return;
        }

        boolean registered = userDAO.registerUser(name, email, password);
        if (registered) {
            handleToggleMode();
            emailField.setText(email);
            passwordField.setText(password);
            showSuccess("Account created successfully! Click Sign In to continue.");
        } else {
            showError("Registration failed. Please try again.");
        }
    }

    @FXML
    private void handleDemoSignIn() {
        if (emailField != null)
            emailField.setText("demo@expirywise.app");
        if (passwordField != null)
            passwordField.setText("demo2026");
        proceedToMain();
    }

    private void proceedToMain() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/main.fxml"));

            Parent root = loader.load();

            Stage stage = (Stage) actionButton.getScene().getWindow();

            Scene scene = new Scene(root, 1220, 780);
            ThemeManager.registerScene(scene);

            stage.setMinWidth(1080);
            stage.setMinHeight(700);
            stage.setScene(scene);
            stage.setTitle("ExpiryWise");
            stage.setResizable(true);
            stage.centerOnScreen();

            FadeTransition ft = new FadeTransition(Duration.millis(250), root);
            ft.setFromValue(0.3);
            ft.setToValue(1.0);
            ft.play();

        } catch (IOException e) {
            showError("Failed to open application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
        if (successLabel != null) {
            successLabel.setVisible(false);
            successLabel.setManaged(false);
        }
    }

    private void showSuccess(String message) {
        if (successLabel != null) {
            successLabel.setText(message);
            successLabel.setVisible(true);
            successLabel.setManaged(true);
        }
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }

    private void clearMessages() {
        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
        if (successLabel != null) {
            successLabel.setText("");
            successLabel.setVisible(false);
            successLabel.setManaged(false);
        }
    }
}