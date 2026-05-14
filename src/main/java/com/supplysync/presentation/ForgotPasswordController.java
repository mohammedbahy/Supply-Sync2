package com.supplysync.presentation;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class ForgotPasswordController extends BaseScreenController {
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private void resetPassword(ActionEvent event) throws IOException {
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("Validation Error", "All fields are required.");
            return;
        }

        if (!email.toLowerCase().contains("@gmail") && !email.toLowerCase().contains("@icloud")) {
            showAlert("Validation Error", "Email must be a @gmail or @icloud address.");
            return;
        }

        if (password.length() < 6) {
            showAlert("Validation Error", "Password must be at least 6 characters.");
            return;
        }

        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSymbol = password.matches(".*[^a-zA-Z0-9].*");

        if (!hasLetter || !hasDigit || !hasSymbol) {
            showAlert("Validation Error", "Password must contain at least one letter, one number, and one symbol.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert("Validation Error", "Passwords do not match.");
            return;
        }

        boolean success = orderFacade.resetPassword(email, password);
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Password has been reset successfully.");
            openLogin(event);
        } else {
            showAlert("Error", "Email not found.");
        }
    }

    @FXML
    private void openLogin(ActionEvent event) throws IOException {
        ScreenNavigator.open(event, "/com/supplysync/presentation/login-view.fxml", "SupplySync Login");
    }

    private void showAlert(String title, String content) {
        showAlert(Alert.AlertType.ERROR, title, content);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
