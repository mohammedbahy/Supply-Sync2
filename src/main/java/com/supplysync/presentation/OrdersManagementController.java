package com.supplysync.presentation;

import com.supplysync.domain.order.OrderTransition;
import com.supplysync.models.Order;
import com.supplysync.models.OrderStatuses;
import com.supplysync.models.Product;
import com.supplysync.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Set;
import java.util.UUID;

public class OrdersManagementController extends BaseScreenController {
    @FXML private VBox ordersTable;
    @FXML private Label detailsOrderId;
    @FXML private Label detailsOrderDate;
    @FXML private Label detailsStatusLabel;
    @FXML private Label detailsCustomerName;
    @FXML private Label detailsCustomerPhone;
    @FXML private Label detailsCustomerAddress;
    @FXML private VBox detailsItemsContainer;
    @FXML private Label detailsTotalAmount;
    @FXML private Button approveBtn;
    @FXML private Button deliverBtn;
    @FXML private Button cancelBtn;
    @FXML private Label pageTitle;
    @FXML private Label pageSubtitle;

    private Order selectedOrder;

    public void initialize() {
        if (orders() != null) {
            renderOrders();
        }
    }

    @Override
    public void setApplicationContext(com.supplysync.facade.ApplicationContext app) {
        super.setApplicationContext(app);
        renderOrders();
    }

    @Override
    protected void applyLanguage() {
        super.applyLanguage();
        if (pageTitle != null) {
            pageTitle.setText(LanguageManager.get("Orders"));
        }
        if (pageSubtitle != null) {
            pageSubtitle.setText(LanguageManager.get("Manage wholesale purchase orders and shipping status."));
        }
        if (approveBtn != null) {
            approveBtn.setText(LanguageManager.isArabic() ? "اعتماد الطلب" : "Approve order");
        }
        if (deliverBtn != null) {
            deliverBtn.setText(LanguageManager.isArabic() ? "تم التسليم" : "Mark delivered");
        }
        if (cancelBtn != null) {
            cancelBtn.setText(LanguageManager.get("CANCEL ORDER"));
        }
    }

    private void renderOrders() {
        if (ordersTable == null || orders() == null) {
            return;
        }

        javafx.scene.Node header = ordersTable.getChildren().get(0);
        javafx.scene.Node sep = ordersTable.getChildren().get(1);
        ordersTable.getChildren().clear();
        ordersTable.getChildren().addAll(header, sep);

        for (Order order : orders().getAllOrders()) {
            ordersTable.getChildren().add(createOrderRow(order));
        }
    }

    private HBox createOrderRow(Order order) {
        HBox row = new HBox();
        row.getStyleClass().add("table-row");
        row.setOnMouseClicked(e -> showOrderDetails(order));

        Label idLabel = new Label(order.getId());
        idLabel.getStyleClass().addAll("col-id", "body-cell", "active-link");

        Label nameLabel = new Label(order.getCustomerName());
        nameLabel.getStyleClass().addAll("col-customer", "body-cell");

        Label statusLabel = new Label(OrderStatuses.displayLabel(order.getStatus(), LanguageManager.isArabic()));
        statusLabel.getStyleClass().add(getStatusClass(order.getStatus()));

        Label priceLabel = new Label("$" + String.format("%.2f", order.getTotalAmount()));
        priceLabel.getStyleClass().addAll("col-price", "body-cell");

        Label dateLabel = new Label(order.getDate().toString());
        dateLabel.getStyleClass().addAll("col-date", "body-cell");

        Label arrow = new Label("›");
        arrow.getStyleClass().addAll("col-arrow", "body-cell");

        row.getChildren().addAll(idLabel, nameLabel, statusLabel, priceLabel, dateLabel, arrow);
        return row;
    }

    private void showOrderDetails(Order order) {
        this.selectedOrder = order;
        detailsOrderId.setText(order.getId());
        if (detailsStatusLabel != null) {
            detailsStatusLabel.setText(OrderStatuses.displayLabel(order.getStatus(), LanguageManager.isArabic()));
        }
        detailsOrderDate.setText(LanguageManager.isArabic()
                ? "تاريخ الطلب: " + order.getDate() + " — " + order.getEffectivePlacedAt()
                : "Placed: " + order.getEffectivePlacedAt() + " (date " + order.getDate() + ")");
        detailsCustomerName.setText(order.getCustomerName());
        detailsCustomerPhone.setText(order.getCustomerPhone());
        detailsCustomerAddress.setText(order.getCustomerAddress());
        detailsTotalAmount.setText("$" + String.format("%.2f", order.getTotalAmount()));

        detailsItemsContainer.getChildren().clear();
        for (Product p : order.getProducts()) {
            HBox itemRow = new HBox();
            Label pName = new Label(p.getName());
            Region pSpacer = new Region();
            HBox.setHgrow(pSpacer, Priority.ALWAYS);
            Label pPrice = new Label("$" + String.format("%.2f", p.getPrice()));
            itemRow.getChildren().addAll(pName, pSpacer, pPrice);
            detailsItemsContainer.getChildren().add(itemRow);
        }

        refreshActionButtons();
    }

    private void refreshActionButtons() {
        if (selectedOrder == null || orders() == null) {
            return;
        }
        Set<OrderTransition> allowed = orders().getAllowedTransitions(selectedOrder);

        boolean canApprove = allowed.contains(OrderTransition.APPROVE);
        boolean canShip = allowed.contains(OrderTransition.SHIP);
        boolean canDeliver = allowed.contains(OrderTransition.DELIVER);
        boolean canCancel = allowed.contains(OrderTransition.CANCEL);

        approveBtn.setVisible(canApprove || canShip);
        approveBtn.setDisable(!canApprove && !canShip);
        if (canApprove) {
            approveBtn.setText(LanguageManager.isArabic() ? "اعتماد الطلب" : "Approve order");
            approveBtn.setOnAction(e -> runTransition(OrderTransition.APPROVE));
        } else if (canShip) {
            approveBtn.setText(LanguageManager.isArabic() ? "شحن الطلب" : "Ship order");
            approveBtn.setOnAction(e -> runTransition(OrderTransition.SHIP));
        }

        deliverBtn.setVisible(canDeliver);
        deliverBtn.setDisable(!canDeliver);
        deliverBtn.setOnAction(e -> runTransition(OrderTransition.DELIVER));

        cancelBtn.setVisible(canCancel);
        cancelBtn.setDisable(!canCancel);
        cancelBtn.setOnAction(e -> runTransition(OrderTransition.CANCEL));
    }

    private void runTransition(OrderTransition transition) {
        if (selectedOrder == null) {
            return;
        }
        try {
            Order updated = orders().executeTransition(selectedOrder.getId(), transition);
            selectedOrder = updated;
            sendWorkflowMessage(updated, transition);
            showAlert(Alert.AlertType.INFORMATION,
                    LanguageManager.isArabic() ? "تم التحديث" : "Updated",
                    OrderStatuses.displayLabel(updated.getStatus(), LanguageManager.isArabic()));
            renderOrders();
            showOrderDetails(updated);
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR,
                    LanguageManager.isArabic() ? "خطأ" : "Error",
                    ex.getMessage());
        }
    }

    private void sendWorkflowMessage(Order order, OrderTransition transition) {
        if (notifications() == null) {
            return;
        }
        com.supplysync.models.Message message = new com.supplysync.models.Message();
        message.setId(UUID.randomUUID().toString());
        message.setOrderId(order.getId());
        message.setRecipientEmail(order.getCustomerName());
        message.setSenderEmail("admin@gmail.com");
        switch (transition) {
            case APPROVE:
                message.setTitle("Order approved");
                message.setContent("Your order " + order.getId() + " was approved.");
                break;
            case SHIP:
                message.setTitle("Order shipped");
                message.setContent("Your order " + order.getId() + " is on the way.");
                break;
            case DELIVER:
                message.setTitle("Order delivered");
                message.setContent("Your order " + order.getId() + " has been delivered.");
                break;
            case CANCEL:
                message.setTitle("Order cancelled");
                message.setContent("Your order " + order.getId() + " was cancelled.");
                break;
            default:
                message.setTitle("Order updated");
                message.setContent("Order " + order.getId() + " status: " + order.getStatus());
        }
        message.setStatus(order.getStatus());
        notifications().sendMessage(message);
    }

    @FXML
    private void handleApproveOrder() {
        if (selectedOrder == null) {
            return;
        }
        Set<OrderTransition> allowed = orders().getAllowedTransitions(selectedOrder);
        if (allowed.contains(OrderTransition.APPROVE)) {
            runTransition(OrderTransition.APPROVE);
        } else if (allowed.contains(OrderTransition.SHIP)) {
            runTransition(OrderTransition.SHIP);
        }
    }

    @FXML
    private void handleMarkDelivered() {
        runTransition(OrderTransition.DELIVER);
    }

    @FXML
    private void handleCancelOrder() {
        runTransition(OrderTransition.CANCEL);
    }

    private String getStatusClass(String status) {
        if (status == null) {
            return "tag-pending";
        }
        switch (OrderStatuses.normalizeWorkflow(status)) {
            case OrderStatuses.AWAITING_APPROVAL:
            case OrderStatuses.PENDING:
                return "tag-pending";
            case OrderStatuses.APPROVED:
            case OrderStatuses.PARTIALLY_SHIPPED:
            case OrderStatuses.ON_HOLD:
                return "tag-pending";
            case OrderStatuses.IN_TRANSIT:
                return "tag-in-transit";
            case OrderStatuses.DELIVERED:
                return "tag-delivered";
            case OrderStatuses.CANCELLED:
            case OrderStatuses.RETURNED:
                return "tag-cancelled";
            default:
                return "tag-pending";
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
