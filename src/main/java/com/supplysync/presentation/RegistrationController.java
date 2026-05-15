package com.supplysync.presentation;

import com.supplysync.models.User;
import com.supplysync.presentation.auth.PasswordRevealSupport;
import com.supplysync.presentation.auth.UserRegistrationValidator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class RegistrationController extends BaseScreenController {
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField companyField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField passwordPlainField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private TextField confirmPlainField;
    @FXML
    private CheckBox showPasswordCheck;
    @FXML
    private CheckBox agreeCheckBox;

    @FXML
    public void initialize() {
        if (showPasswordCheck != null && passwordField != null && passwordPlainField != null
                && confirmPasswordField != null && confirmPlainField != null) {
            PasswordRevealSupport.bindDual(showPasswordCheck, passwordField, passwordPlainField,
                    confirmPasswordField, confirmPlainField);
            showPasswordCheck.setText(LanguageManager.get("Show passwords"));
        }
    }

    @FXML
    private void createAccount(ActionEvent event) throws IOException {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String email = emailField.getText();
        String company = companyField.getText();
        String password = PasswordRevealSupport.effectivePassword(showPasswordCheck, passwordField, passwordPlainField);
        String confirmPassword = PasswordRevealSupport.effectivePassword(showPasswordCheck, confirmPasswordField, confirmPlainField);

        java.util.List<String> errors = UserRegistrationValidator.validateNewMarketer(
                firstName, lastName, email, company, password, confirmPassword, agreeCheckBox.isSelected());
        if (!errors.isEmpty()) {
            showAlert("Validation Error", String.join("\n", errors));
            return;
        }

        User newUser = new User();
        newUser.setId(java.util.UUID.randomUUID().toString());
        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setName(firstName + " " + lastName);
        newUser.setRole("MARKETER"); // Default role for new registrations

        try {
            orderFacade.register(newUser);
            showAlert(Alert.AlertType.INFORMATION, "Registration Successful", "Your account has been created. Please sign in.");
            openLogin(event);
        } catch (Exception e) {
            showAlert("Registration Failed", e.getMessage());
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
