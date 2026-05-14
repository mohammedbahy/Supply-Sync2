package com.supplysync.presentation;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import java.io.IOException;

import com.supplysync.models.User;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.util.Optional;

public class LoginController extends BaseScreenController {
    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void signIn(ActionEvent event) throws IOException {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            showAlert("Validation Error", "Please enter both email and password.");
            return;
        }

        Optional<User> user = orderFacade.login(email.trim(), password);
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
