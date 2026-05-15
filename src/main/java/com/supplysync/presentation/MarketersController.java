package com.supplysync.presentation;

import com.supplysync.models.Marketer;
import com.supplysync.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import java.util.List;
import java.util.Optional;

public class MarketersController extends BaseScreenController {
    @FXML private VBox marketersTable;
    @FXML private Label pageTitle;
    @FXML private Label pageSubtitle;
    @FXML private Button editMarketerBtn;
    @FXML private Button addMarketerBtn;

    public void initialize() {
        if (auth() != null) {
            renderMarketers();
        }
    }

    @Override
    public void setApplicationContext(com.supplysync.facade.ApplicationContext app) {
        super.setApplicationContext(app);
        renderMarketers();
    }

    @Override
    protected void applyLanguage() {
        super.applyLanguage();
        if (pageTitle != null) pageTitle.setText(LanguageManager.get("Customers"));
        if (pageSubtitle != null) pageSubtitle.setText(LanguageManager.get("View all registered customers and their information."));
        if (editMarketerBtn != null) editMarketerBtn.setText(LanguageManager.get("Manage"));
        if (addMarketerBtn != null) addMarketerBtn.setText(LanguageManager.get("Add Marketer"));
    }

    private void renderMarketers() {
        if (marketersTable == null || auth() == null) return;
        
        if (marketersTable.getChildren().size() > 2) {
            javafx.scene.Node header = marketersTable.getChildren().get(0);
            javafx.scene.Node sep = marketersTable.getChildren().get(1);
            marketersTable.getChildren().clear();
            marketersTable.getChildren().addAll(header, sep);
        }

        // Get all registered users (customers)
        List<User> users = auth().getAllUsers();
        for (User user : users) {
            // Skip admin users, show only marketers/customers
            if ("MARKETER".equals(user.getRole())) {
                HBox row = createCustomerRow(user);
                marketersTable.getChildren().add(row);
            }
        }
    }

    private HBox createCustomerRow(User user) {
        HBox row = new HBox();
        row.getStyleClass().add("table-row");
        row.setSpacing(15);

        Label emailLabel = new Label(user.getEmail());
        emailLabel.getStyleClass().addAll("col-id", "body-cell");
        emailLabel.setPrefWidth(200);
        
        Label nameLabel = new Label(user.getName());
        nameLabel.getStyleClass().addAll("col-name", "body-cell");
        nameLabel.setPrefWidth(200);
        
        Label roleLabel = new Label(user.getRole());
        roleLabel.getStyleClass().addAll("col-category", "body-cell");
        roleLabel.setPrefWidth(100);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button viewBtn = new Button("👁");
        viewBtn.setOnAction(e -> showCustomerDetails(user));
        
        Button deleteBtn = new Button("🗑");
        deleteBtn.setOnAction(e -> {
            // In a real app, you'd delete from storage
            renderMarketers();
        });

        row.getChildren().addAll(emailLabel, nameLabel, roleLabel, spacer, viewBtn, deleteBtn);
        return row;
    }
    
    private void showCustomerDetails(User user) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Customer Details");
        alert.setHeaderText(user.getName());
        String content = "Email: " + user.getEmail() + "\nRole: " + user.getRole() + "\nStatus: Active";
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleAddMarketer() {
        showAlert("Add Marketer", "New customers are automatically added when they register through the registration page.");
    }

    @FXML
    private void handleEditMarketer() {
        renderMarketers();
        showAlert("Customer Management", "View registered customers and manage their accounts.");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
