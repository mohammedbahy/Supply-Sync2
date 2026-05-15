package com.supplysync.presentation;

import com.supplysync.models.Order;
import com.supplysync.models.OrderStatuses;
import com.supplysync.models.Product;
import com.supplysync.workflow.OrderEventBus;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

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
    @FXML private VBox workflowActionsBox;
    @FXML private Label pageTitle;
    @FXML private Label pageSubtitle;

    private Order selectedOrder;
    private final OrderEventBus.OrderChangeListener refreshListener = orderId -> {
        renderOrders();
        if (selectedOrder != null && orderId != null && orderId.equals(selectedOrder.getId())) {
            orderFacade.findOrderById(orderId).ifPresent(this::showOrderDetails);
        }
    };

    @FXML
    public void initialize() {
        if (orderFacade != null) {
            renderOrders();
        }
    }

    @Override
    public void setOrderFacade(com.supplysync.facade.OrderFacade orderFacade) {
        super.setOrderFacade(orderFacade);
        OrderEventBus.getInstance().subscribe(refreshListener);
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
    }

    private void renderOrders() {
        if (ordersTable == null || orderFacade == null || ordersTable.getChildren().size() < 2) {
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

        Label idLabel = new Label(order.getId() != null ? order.getId() : "—");
        idLabel.getStyleClass().addAll("col-id", "body-cell", "active-link");

        Label nameLabel = new Label(order.getCustomerName() != null ? order.getCustomerName() : "—");
        nameLabel.getStyleClass().addAll("col-customer", "body-cell");

        String displayStatus = OrderStatuses.displayLabel(order.getStatus(), LanguageManager.isArabic());
        Label statusLabel = new Label(displayStatus);
        statusLabel.getStyleClass().add(getStatusClass(order.getStatus()));

        Label priceLabel = new Label("$" + String.format("%.2f", order.getTotalAmount()));
        priceLabel.getStyleClass().addAll("col-price", "body-cell");

        Label dateLabel = new Label(formatOrderDate(order));
        dateLabel.getStyleClass().addAll("col-date", "body-cell");

        Label arrow = new Label("›");
        arrow.getStyleClass().addAll("col-arrow", "body-cell");

        row.getChildren().addAll(idLabel, nameLabel, statusLabel, priceLabel, dateLabel, arrow);
        return row;
    }

    private void showOrderDetails(Order order) {
        if (order == null) {
            return;
        }
        try {
            this.selectedOrder = order;
            if (detailsOrderId != null) {
                detailsOrderId.setText(order.getId() != null ? order.getId() : "—");
            }
            if (detailsStatusLabel != null) {
                detailsStatusLabel.setText(OrderStatuses.displayLabel(order.getStatus(), LanguageManager.isArabic()));
            }
            if (detailsOrderDate != null) {
                detailsOrderDate.setText(LanguageManager.isArabic()
                        ? "تاريخ الطلب: " + formatOrderDate(order) + " — " + order.getEffectivePlacedAt()
                        : "Placed: " + order.getEffectivePlacedAt() + " (date " + formatOrderDate(order) + ")");
            }
            if (detailsCustomerName != null) {
                detailsCustomerName.setText(nullToDash(order.getCustomerName()));
            }
            if (detailsCustomerPhone != null) {
                detailsCustomerPhone.setText(nullToDash(order.getCustomerPhone()));
            }
            if (detailsCustomerAddress != null) {
                detailsCustomerAddress.setText(nullToDash(order.getCustomerAddress()));
            }
            if (detailsTotalAmount != null) {
                detailsTotalAmount.setText("$" + String.format("%.2f", order.getTotalAmount()));
            }

            if (detailsItemsContainer != null) {
                detailsItemsContainer.getChildren().clear();
                if (order.getProducts() != null) {
                    for (Product p : order.getProducts()) {
                        HBox itemRow = new HBox();
                        Label pName = new Label(p.getName() != null ? p.getName() : "—");
                        Region pSpacer = new Region();
                        HBox.setHgrow(pSpacer, Priority.ALWAYS);
                        Label pPrice = new Label("$" + String.format("%.2f", p.getPrice()));
                        itemRow.getChildren().addAll(pName, pSpacer, pPrice);
                        detailsItemsContainer.getChildren().add(itemRow);
                    }
                }
            }

            if (workflowActionsBox != null && orderFacade != null && order.getId() != null) {
                OrderWorkflowUiHelper.rebuildWorkflowButtons(
                        workflowActionsBox,
                        order,
                        orderFacade,
                        () -> {
                            renderOrders();
                            orderFacade.findOrderById(order.getId()).ifPresent(o -> {
                                selectedOrder = o;
                                showOrderDetails(o);
                            });
                        }
                );
            }
        } catch (Exception ex) {
            showError(
                    LanguageManager.isArabic() ? "خطأ في عرض التفاصيل" : "Could not show order details",
                    ex.getMessage() != null ? ex.getMessage() : ex.toString()
            );
        }
    }

    private static String formatOrderDate(Order order) {
        if (order.getDate() != null) {
            return order.getDate().toString();
        }
        return order.getEffectivePlacedAt() != null
                ? order.getEffectivePlacedAt().toLocalDate().toString()
                : "—";
    }

    private static String nullToDash(String value) {
        return value != null && !value.isBlank() ? value : "—";
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message != null ? message : "");
        alert.showAndWait();
    }

    private String getStatusClass(String status) {
        if (status == null) {
            return "tag-pending";
        }
        switch (OrderStatuses.normalize(status)) {
            case OrderStatuses.PENDING:
                return "tag-pending";
            case OrderStatuses.IN_TRANSIT:
                return "tag-in-transit";
            case OrderStatuses.DELIVERED:
                return "tag-delivered";
            case OrderStatuses.CANCELLED:
                return "tag-cancelled";
            case OrderStatuses.ON_HOLD:
                return "tag-on-hold";
            case OrderStatuses.PARTIALLY_SHIPPED:
                return "tag-partial";
            default:
                return "tag-pending";
        }
    }
}
