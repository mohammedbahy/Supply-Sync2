package com.supplysync.presentation;

import com.supplysync.models.Order;
import com.supplysync.models.OrderStatuses;
import com.supplysync.models.Product;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MarketingDashboardController extends BaseScreenController {
    @FXML
    private Label totalOrdersLabel;
    @FXML
    private Label pendingCommissionsLabel;
    @FXML
    private Label successfulDeliveriesLabel;
    @FXML
    private Label totalEarningsLabel;
    @FXML
    private VBox recentOrdersContainer;
    @FXML
    private VBox topSellingContainer;
    @FXML
    private VBox weeklyTargetPanel;
    @FXML
    private Label userNameLabel;
    @FXML
    private HBox draftHintBox;
    @FXML
    private Label draftHintLabel;

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
        renderTopSelling(allOrders);

        if (draftHintBox != null) {
            boolean has = orderFacade.hasOrderDraft();
            draftHintBox.setVisible(has);
            draftHintBox.setManaged(has);
            if (draftHintLabel != null && has) {
                draftHintLabel.setText(LanguageManager.get("Saved draft hint"));
            }
        }
    }

    private void renderRecentOrders(List<Order> orders) {
        recentOrdersContainer.getChildren().clear();
        List<Order> sorted = orders.stream()
                .sorted(Comparator.comparing(Order::getEffectivePlacedAt).reversed())
                .limit(5)
                .collect(Collectors.toList());

        for (Order o : sorted) {
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("table-row");
            row.getChildren().addAll(
                    new Label(o.getId()),
                    new Label(o.getCustomerName() != null ? o.getCustomerName() : ""),
                    new Label(o.getDate() != null ? o.getDate().toString() : ""),
                    new Label("$" + String.format("%.2f", o.getTotalAmount())),
                    new Label(OrderStatuses.displayLabel(o.getStatus(), LanguageManager.isArabic()))
            );
            recentOrdersContainer.getChildren().add(row);
        }
    }

    /**
     * Top products by units sold across this marketer's orders (excludes cancelled).
     */
    private void renderTopSelling(List<Order> myOrders) {
        topSellingContainer.getChildren().clear();
        Map<String, Integer> unitsByProductId = new HashMap<>();
        Map<String, String> nameById = new HashMap<>();
        for (Order o : myOrders) {
            if (OrderStatuses.CANCELLED.equals(o.getStatus())) {
                continue;
            }
            for (Product p : o.getProducts()) {
                unitsByProductId.merge(p.getId(), 1, Integer::sum);
                nameById.putIfAbsent(p.getId(), p.getName());
            }
        }
        List<Map.Entry<String, Integer>> ranked = unitsByProductId.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(4)
                .collect(Collectors.toList());

        if (ranked.isEmpty()) {
            Label empty = new Label(LanguageManager.get("No sales yet"));
            empty.setStyle("-fx-text-fill:#6b7280; -fx-font-size:12px;");
            topSellingContainer.getChildren().add(empty);
            return;
        }

        Map<String, Product> catalogById = orderFacade.getCatalog().stream()
                .collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));

        for (Map.Entry<String, Integer> e : ranked) {
            String pid = e.getKey();
            int sold = e.getValue();
            String name = nameById.getOrDefault(pid, pid);
            Product cat = catalogById.get(pid);
            double unit = cat != null ? cat.getPrice() : 0;
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            Label n = new Label(name);
            n.setStyle("-fx-font-weight:600;");
            Label meta = new Label(LanguageManager.get("Units sold") + ": " + sold + " · $" + String.format("%.2f", unit * sold));
            meta.setStyle("-fx-text-fill:#6b7280; -fx-font-size:11px;");
            row.getChildren().addAll(n, meta);
            topSellingContainer.getChildren().add(row);
        }
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
