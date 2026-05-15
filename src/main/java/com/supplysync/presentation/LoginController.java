package com.supplysync.presentation;

import com.supplysync.models.User;
import com.supplysync.presentation.auth.PasswordRevealSupport;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.Optional;

public class LoginController extends BaseScreenController {
    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField passwordPlainField;

    @FXML
    private CheckBox showPasswordCheck;

    @FXML
    public void initialize() {
        if (showPasswordCheck != null && passwordField != null && passwordPlainField != null) {
            PasswordRevealSupport.bindSingle(showPasswordCheck, passwordField, passwordPlainField);
            showPasswordCheck.setText(LanguageManager.get("Show password"));
        }
    }

    @FXML
    private void signIn(ActionEvent event) throws IOException {
        String email = emailField.getText();
        String password = PasswordRevealSupport.effectivePassword(showPasswordCheck, passwordField, passwordPlainField);

        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            showAlert("Validation Error", "Please enter both email and password.");
            return;
        }

        if (auth() == null) {
            showAlert("Login Failed", "Application is not initialized.");
            return;
        }
        Optional<User> user = auth().login(email.trim(), password);
        if (user.isPresent()) {
            User loggedInUser = user.get();
            try {
                if ("ADMIN".equalsIgnoreCase(loggedInUser.getRole())) {
                    ScreenNavigator.open(event, "/com/supplysync/presentation/dashboard-view.fxml", "SupplySync Admin Dashboard");
                } else {
                    ScreenNavigator.open(event, "/com/supplysync/presentation/marketing-dashboard-view.fxml", "SupplySync Marketing Dashboard");
                }
            } catch (Exception e) {
                showAlert("Navigation Error", "Could not open dashboard: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showAlert("Login Failed", "Invalid email or password.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void openRegistration(ActionEvent event) throws IOException {
        ScreenNavigator.open(event, "/com/supplysync/presentation/registration-view.fxml", "SupplySync Registration");
    }

    @FXML
    private void openForgotPassword(ActionEvent event) throws IOException {
        ScreenNavigator.open(event, "/com/supplysync/presentation/forgot-password-view.fxml", "SupplySync Forgot Password");
    }
}
