package com.supplysync.presentation;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import com.supplysync.models.Order;
import com.supplysync.models.Product;
import com.supplysync.models.User;
import com.supplysync.models.Marketer;
import com.supplysync.models.MarketerCancelResult;
import com.supplysync.models.OrderStatuses;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class OrdersController extends BaseScreenController {
    @FXML private VBox orderItemsContainer;
    @FXML private VBox myOrdersContainer;
    @FXML private TextField customerNameField;
    @FXML private TextField phoneField;
    @FXML private TextArea addressArea;
    @FXML private Label subtotalLabel;
    @FXML private Label totalLabel;
    @FXML private TextField orderSearchField;
    @FXML private Label userNameLabel;

    @Override
    public void setOrderFacade(com.supplysync.facade.OrderFacade orderFacade) {
        super.setOrderFacade(orderFacade);
        if (orderFacade.getCurrentUser() != null) {
            customerNameField.setText(orderFacade.getCurrentUser().getName());
            userNameLabel.setText(orderFacade.getCurrentUser().getName());
        }
        renderOrderItems();
        renderMyOrders();
    }

    private void renderMyOrders() {
        if (myOrdersContainer == null || orderFacade == null) {
            return;
        }
        myOrdersContainer.getChildren().clear();
        for (Order o : orderFacade.getMyOrders()) {
            HBox row = new HBox(12);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            row.setStyle("-fx-padding: 8; -fx-background-color: #fafafa; -fx-background-radius: 6; -fx-border-color: #eee; -fx-border-radius: 6;");

            Label id = new Label(o.getId());
            id.setStyle("-fx-font-weight: bold;");
            Label st = new Label(OrderStatuses.displayLabel(o.getStatus(), LanguageManager.isArabic()));
            Label when = new Label(o.getEffectivePlacedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button cancelBtn = new Button(LanguageManager.isArabic() ? "إلغاء الطلب" : "Cancel order");
            cancelBtn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c;");
            boolean pending = OrderStatuses.PENDING.equals(o.getStatus());
            cancelBtn.setDisable(!pending);
            cancelBtn.setOnAction(e -> handleCancelMyOrder(o.getId()));

            row.getChildren().addAll(id, st, when, spacer, cancelBtn);
            myOrdersContainer.getChildren().add(row);
        }
    }

    private void handleCancelMyOrder(String orderId) {
        MarketerCancelResult r = orderFacade.cancelOrderAsMarketer(orderId);
        if (r == MarketerCancelResult.SUCCESS) {
            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setContentText(LanguageManager.isArabic() ? "تم إلغاء الطلب واستعادة الكميات في المخزون." : "Order cancelled and stock was restored.");
            ok.showAndWait();
            renderMyOrders();
            return;
        }
        Alert warn = new Alert(Alert.AlertType.WARNING);
        warn.setHeaderText(null);
        if (r == MarketerCancelResult.TOO_LATE) {
            warn.setTitle(LanguageManager.isArabic() ? "لا يمكن الإلغاء" : "Cannot cancel");
            warn.setContentText(LanguageManager.isArabic()
                    ? "لا يمكن إلغاء الطلب بعد مرور 24 ساعة من وقت الطلب."
                    : "You cannot cancel this order more than 24 hours after it was placed.");
        } else if (r == MarketerCancelResult.INVALID_STATUS) {
            warn.setTitle(LanguageManager.isArabic() ? "لا يمكن الإلغاء" : "Cannot cancel");
            warn.setContentText(LanguageManager.isArabic()
                    ? "يمكن إلغاء الطلبات قيد المعالجة فقط."
                    : "Only orders that are still processing can be cancelled.");
        } else {
            warn.setAlertType(Alert.AlertType.ERROR);
            warn.setContentText(LanguageManager.isArabic() ? "تعذر تنفيذ الإلغاء." : "Unable to cancel this order.");
        }
        warn.showAndWait();
    }

    private void renderOrderItems() {
        if (orderItemsContainer == null || orderFacade == null) return;
        orderItemsContainer.getChildren().clear();
        
        List<Product> selectedProducts = orderFacade.getCart();
        double subtotal = 0;
        
        for (Product p : selectedProducts) {
            HBox row = new HBox(15);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            row.setStyle("-fx-padding: 10; -fx-background-color: white; -fx-background-radius: 5; -fx-border-color: #eee; -fx-border-radius: 5;");
            
            VBox info = new VBox(5);
            Label name = new Label(p.getName());
            name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            Label meta = new Label(LanguageManager.get(p.getCategory()) + " | ID: " + p.getId());
            meta.setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");
            info.getChildren().addAll(name, meta);
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            Label price = new Label("$" + String.format("%.2f", p.getPrice()));
            price.setStyle("-fx-font-weight: bold; -fx-text-fill: #2563eb;");
            
            Button removeBtn = new Button(LanguageManager.isArabic() ? "حذف" : "Remove");
            removeBtn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-font-size: 12px;");
            removeBtn.setOnAction(e -> {
                orderFacade.removeFromCart(p);
                renderOrderItems();
            });
            
            row.getChildren().addAll(info, spacer, price, removeBtn);
            orderItemsContainer.getChildren().add(row);
            subtotal += p.getPrice();
        }
        
        if (subtotalLabel != null) subtotalLabel.setText("$" + String.format("%.2f", subtotal));
        if (totalLabel != null) totalLabel.setText("$" + String.format("%.2f", subtotal));
    }

    @FXML
    private void handleSubmitOrder() {
        if (orderFacade.getCart().isEmpty()) {
            showAlert(LanguageManager.get("Validation Error"), LanguageManager.isArabic() ? "السلة فارغة، يرجى إضافة منتجات أولاً." : "Cart is empty, please add products first.");
            return;
        }

        String phone = phoneField.getText();
        if (phone == null || phone.isEmpty()) {
            showAlert(LanguageManager.get("Validation Error"), LanguageManager.isArabic() ? "رقم الهاتف مطلوب." : "Phone number is required.");
            return;
        }
        if (addressArea.getText().isEmpty()) {
            showAlert(LanguageManager.get("Validation Error"), LanguageManager.isArabic() ? "العنوان مطلوب." : "Address is required.");
            return;
        }

        Order order = new Order();
        order.setId("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setCustomerName(customerNameField.getText());
        order.setCustomerPhone(phone);
        order.setCustomerAddress(addressArea.getText());
        String totalText = totalLabel.getText().replace("$", "").trim();
        double total = Double.parseDouble(totalText);
        order.setTotalAmount(total);
        order.setCommission(total * 0.05);
        order.getProducts().addAll(orderFacade.getCart());

        LocalDateTime now = LocalDateTime.now();
        order.setPlacedAt(now);
        order.setDate(now.toLocalDate());
        User u = orderFacade.getCurrentUser();
        if (u != null) {
            order.setMarketer(new Marketer(u.getId(), u.getName()));
        }

        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        orderFacade.processOrder(order);

        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setTitle(LanguageManager.get("Success"));
        success.setHeaderText(null);
        success.setContentText(LanguageManager.isArabic() ?
            "تم تأكيد طلبك بنجاح!\nرقم الطلب: " + order.getId() + "\nالتاريخ: " + timestamp :
            "Order confirmed successfully!\nOrder ID: " + order.getId() + "\nDate: " + timestamp);
        success.showAndWait();

        renderOrderItems();
        renderMyOrders();
    }

    @FXML
    private void handleSaveDraft() {
        showAlert(Alert.AlertType.INFORMATION, LanguageManager.get("Draft Saved"), LanguageManager.isArabic() ? "تم حفظ الطلب كمسودة." : "Order has been saved as draft.");
    }

    @FXML
    private void handleSearchOrder() {
        showAlert(Alert.AlertType.INFORMATION, LanguageManager.get("Search"), LanguageManager.get("Search") + ": " + orderSearchField.getText());
    }

    @FXML
    private void showHelp() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(LanguageManager.get("Support"));
        alert.setHeaderText(LanguageManager.get("Orders"));
        alert.setContentText(LanguageManager.isArabic() ? 
            "هذه الصفحة مخصصة لإنشاء طلبات شراء جديدة ومراجعة بيانات العميل والمنتجات." :
            "This page is for creating new purchase orders and reviewing customer and product data.");
        alert.showAndWait();
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
