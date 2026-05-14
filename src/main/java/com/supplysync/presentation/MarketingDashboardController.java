package com.supplysync.presentation;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import com.supplysync.models.Order;
import com.supplysync.models.OrderStatuses;
import com.supplysync.models.Product;
import java.util.List;
import java.util.stream.Collectors;

public class MarketingDashboardController extends BaseScreenController {
    @FXML private Label totalOrdersLabel;
    @FXML private Label pendingCommissionsLabel;
    @FXML private Label successfulDeliveriesLabel;
    @FXML private Label totalEarningsLabel;
    @FXML private VBox recentOrdersContainer;
    @FXML private VBox topSellingContainer;
    @FXML private TextField searchField;
    @FXML private VBox weeklyTargetPanel;
    @FXML private Label userNameLabel;

    public void initialize() {
        if (weeklyTargetPanel != null) {
            weeklyTargetPanel.setVisible(false);
            weeklyTargetPanel.setManaged(false);
        }
    }

    @Override
    public void setOrderFacade(com.supplysync.facade.OrderFacade orderFacade) {
        super.setOrderFacade(orderFacade);
        if (orderFacade.getCurrentUser() != null) {
            userNameLabel.setText(orderFacade.getCurrentUser().getName());
        }
        updateDashboard();
    }

    private void updateDashboard() {
        List<Order> allOrders = orderFacade.getMyOrders();

        totalOrdersLabel.setText(String.valueOf(allOrders.size()));

        double pendingComm = allOrders.stream()
            .filter(o -> OrderStatuses.PENDING.equals(o.getStatus()))
            .mapToDouble(Order::getCommission)
            .sum();
        pendingCommissionsLabel.setText("$" + String.format("%.2f", pendingComm));
        
        long successful = allOrders.stream()
            .filter(o -> OrderStatuses.DELIVERED.equals(o.getStatus()))
            .count();
        successfulDeliveriesLabel.setText(String.valueOf(successful));

        double earnings = allOrders.stream()
            .filter(o -> OrderStatuses.DELIVERED.equals(o.getStatus()))
            .mapToDouble(Order::getTotalAmount)
            .sum();
        totalEarningsLabel.setText("$" + String.format("%.2f", earnings));

        renderRecentOrders(allOrders);
        renderTopSelling();
    }

    private void renderRecentOrders(List<Order> orders) {
        recentOrdersContainer.getChildren().clear();
        List<Order> sorted = orders.stream()
            .sorted((o1, o2) -> o2.getDate().compareTo(o1.getDate()))
            .limit(5)
            .collect(Collectors.toList());
            
        for (Order o : sorted) {
            javafx.scene.layout.HBox row = new javafx.scene.layout.HBox();
            row.getStyleClass().add("table-row");
            row.getChildren().addAll(
                new Label(o.getId()),
                new Label(o.getCustomerName()),
                new Label(o.getDate().toString()),
                new Label("$" + o.getTotalAmount()),
                new Label(OrderStatuses.displayLabel(o.getStatus(), LanguageManager.isArabic()))
            );
            recentOrdersContainer.getChildren().add(row);
        }
    }

    private void renderTopSelling() {
        topSellingContainer.getChildren().clear();
        List<Product> products = orderFacade.getCatalog().stream()
            .sorted((p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()))
            .limit(4)
            .collect(Collectors.toList());
            
        for (Product p : products) {
            javafx.scene.layout.HBox row = new javafx.scene.layout.HBox();
            row.getChildren().addAll(new Label(p.getName()), new Label("$" + p.getPrice()));
            topSellingContainer.getChildren().add(row);
        }
    }

    @FXML
    private void handleSearch(javafx.event.Event event) {
        String query = searchField != null ? searchField.getText() : "";
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Search Operations");
        alert.setHeaderText("Available Operations to Search:");
        alert.setContentText("1. Order ID\n2. Customer Name\n3. Product Name\n4. Category\n\nCurrent filter: " + query);
        alert.showAndWait();
    }

    @FXML
    private void showHelp() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Help");
        alert.setHeaderText("Dashboard Instructions");
        alert.setContentText("This dashboard shows your sales performance, earnings, and recent orders.");
        alert.showAndWait();
    }
}
