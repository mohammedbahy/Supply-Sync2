package com.supplysync.presentation;

import com.supplysync.models.Order;
import com.supplysync.patterns.creational.builder.OrderBuilder;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;

import java.util.UUID;

public class OrderPlacementController extends BaseScreenController {

    @FXML
    private TextField customerNameField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextArea addressArea;

    @FXML
    private void handleConfirmOrder(ActionEvent event) {
        String customerName = customerNameField.getText();
        String address = addressArea.getText();

        if (customerName.isEmpty() || address.isEmpty()) {
            showAlert("Error", "Please fill in all required fields.");
            return;
        }

        // Create order using builder pattern
        Order order = new OrderBuilder()
                .withId(UUID.randomUUID().toString())
                .build();

        // Process order via facade
        if (orderFacade != null) {
            orderFacade.processOrder(order);
            showAlert("Success", "Order confirmed successfully for " + customerName);
        } else {
            showAlert("Error", "Backend service not available.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
