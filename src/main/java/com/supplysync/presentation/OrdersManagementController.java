package com.supplysync.presentation;

import com.supplysync.models.Order;
import com.supplysync.models.OrderStatuses;
import com.supplysync.models.Product;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
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
        if (orderFacade != null) {
            renderOrders();
        }
    }

    @Override
    public void setOrderFacade(com.supplysync.facade.OrderFacade orderFacade) {
        super.setOrderFacade(orderFacade);
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
            approveBtn.setText(LanguageManager.isArabic() ? "في الطريق" : "Mark in transit");
        }
        if (deliverBtn != null) {
            deliverBtn.setText(LanguageManager.isArabic() ? "تم التسليم" : "Mark delivered");
        }
        if (cancelBtn != null) {
            cancelBtn.setText(LanguageManager.get("CANCEL ORDER"));
        }
    }

    private void renderOrders() {
        if (ordersTable == null || orderFacade == null) {
            return;
        }

        javafx.scene.Node header = ordersTable.getChildren().get(0);
        javafx.scene.Node sep = ordersTable.getChildren().get(1);
        ordersTable.getChildren().clear();
        ordersTable.getChildren().addAll(header, sep);

        List<Order> orders = orderFacade.getAllOrders();
        for (Order order : orders) {
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

        String displayStatus = OrderStatuses.displayLabel(order.getStatus(), LanguageManager.isArabic());
        Label statusLabel = new Label(displayStatus);
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

        String st = order.getStatus();
        boolean pending = OrderStatuses.PENDING.equals(st);
        boolean inTransit = OrderStatuses.IN_TRANSIT.equals(st) || OrderStatuses.APPROVED.equals(st) || "SHIPPED".equals(st);
        boolean cancellable = pending || inTransit;
        boolean delivered = OrderStatuses.DELIVERED.equals(st);
        boolean cancelled = OrderStatuses.CANCELLED.equals(st);

        approveBtn.setVisible(pending);
        deliverBtn.setVisible(inTransit);
        cancelBtn.setVisible(cancellable && !delivered && !cancelled);
    }

    @FXML
    private void handleApproveOrder() {
        if (selectedOrder == null) {
            return;
        }
        if (!OrderStatuses.PENDING.equals(selectedOrder.getStatus())) {
            return;
        }

        selectedOrder.setStatus(OrderStatuses.IN_TRANSIT);
        orderFacade.persistOrder(selectedOrder);

        com.supplysync.models.Message message = new com.supplysync.models.Message();
        message.setId(UUID.randomUUID().toString());
        message.setOrderId(selectedOrder.getId());
        message.setRecipientEmail(selectedOrder.getCustomerName());
        message.setSenderEmail("admin@gmail.com");
        message.setTitle("Order in transit");
        message.setContent("Your order " + selectedOrder.getId() + " is on the way.");
        message.setStatus(OrderStatuses.IN_TRANSIT);
        orderFacade.sendMessage(message);

        showAlert(Alert.AlertType.INFORMATION, LanguageManager.isArabic() ? "تم التحديث" : "Updated",
                LanguageManager.isArabic() ? "تم وضع الطلب في حالة \"في الطريق\"." : "Order marked as in transit.");
        renderOrders();
        showOrderDetails(selectedOrder);
    }

    @FXML
    private void handleMarkDelivered() {
        if (selectedOrder == null) {
            return;
        }
        String st = selectedOrder.getStatus();
        if (!OrderStatuses.IN_TRANSIT.equals(st) && !OrderStatuses.APPROVED.equals(st) && !"SHIPPED".equals(st)) {
            return;
        }

        selectedOrder.setStatus(OrderStatuses.DELIVERED);
        orderFacade.persistOrder(selectedOrder);

        com.supplysync.models.Message message = new com.supplysync.models.Message();
        message.setId(UUID.randomUUID().toString());
        message.setOrderId(selectedOrder.getId());
        message.setRecipientEmail(selectedOrder.getCustomerName());
        message.setSenderEmail("admin@gmail.com");
        message.setTitle("Order delivered");
        message.setContent("Your order " + selectedOrder.getId() + " has been delivered.");
        message.setStatus(OrderStatuses.DELIVERED);
        orderFacade.sendMessage(message);

        showAlert(Alert.AlertType.INFORMATION, LanguageManager.isArabic() ? "تم التسليم" : "Delivered",
                LanguageManager.isArabic() ? "تم تسجيل الطلب كمُسلَّم." : "Order marked as delivered.");
        renderOrders();
        showOrderDetails(selectedOrder);
    }

    @FXML
    private void handleCancelOrder() {
        if (selectedOrder == null) {
            return;
        }
        String st = selectedOrder.getStatus();
        if (OrderStatuses.DELIVERED.equals(st) || OrderStatuses.CANCELLED.equals(st)) {
            return;
        }

        orderFacade.restoreOrderInventory(selectedOrder);
        selectedOrder.setStatus(OrderStatuses.CANCELLED);
        orderFacade.persistOrder(selectedOrder);

        com.supplysync.models.Message message = new com.supplysync.models.Message();
        message.setId(UUID.randomUUID().toString());
        message.setOrderId(selectedOrder.getId());
        message.setRecipientEmail(selectedOrder.getCustomerName());
        message.setSenderEmail("admin@gmail.com");
        message.setTitle("Order Cancelled");
        message.setContent("Your order " + selectedOrder.getId() + " has been cancelled. Stock has been restored.");
        message.setStatus(OrderStatuses.CANCELLED);
        orderFacade.sendMessage(message);

        showAlert(Alert.AlertType.INFORMATION, LanguageManager.isArabic() ? "تم الإلغاء" : "Cancelled",
                LanguageManager.isArabic() ? "تم إلغاء الطلب واستعادة المخزون." : "Order cancelled and inventory restored.");
        renderOrders();
        showOrderDetails(selectedOrder);
    }

    private String getStatusClass(String status) {
        if (status == null) {
            return "tag-pending";
        }
        switch (status) {
            case "PENDING":
                return "tag-pending";
            case "IN_TRANSIT":
            case "APPROVED":
            case "SHIPPED":
                return "tag-in-transit";
            case "DELIVERED":
                return "tag-delivered";
            case "CANCELLED":
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
