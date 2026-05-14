package com.supplysync.presentation;

import com.supplysync.models.Marketer;
import com.supplysync.models.MarketerCancelResult;
import com.supplysync.models.Order;
import com.supplysync.models.OrderStatuses;
import com.supplysync.models.Product;
import com.supplysync.models.User;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OrdersController extends BaseScreenController {
    @FXML
    private VBox orderItemsContainer;
    @FXML
    private VBox myOrdersContainer;
    @FXML
    private TextField customerNameField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField countryField;
    @FXML
    private TextArea addressArea;
    @FXML
    private Label subtotalLabel;
    @FXML
    private Label totalLabel;
    @FXML
    private TextField orderSearchField;
    @FXML
    private Label userNameLabel;

    @Override
    public void setOrderFacade(com.supplysync.facade.OrderFacade orderFacade) {
        super.setOrderFacade(orderFacade);
        User u = orderFacade.getCurrentUser();
        if (u != null) {
            userNameLabel.setText(u.getName());
            String savedName = u.getPrefCustomerName() != null && !u.getPrefCustomerName().isBlank()
                    ? u.getPrefCustomerName().trim()
                    : u.getName();
            customerNameField.setText(savedName);
            if (u.getPrefCustomerPhone() != null && !u.getPrefCustomerPhone().isBlank()) {
                phoneField.setText(u.getPrefCustomerPhone());
            }
            if (u.getPrefCustomerCountry() != null && !u.getPrefCustomerCountry().isBlank()) {
                countryField.setText(u.getPrefCustomerCountry());
            }
            if (u.getPrefShippingAddress() != null && !u.getPrefShippingAddress().isBlank()) {
                addressArea.setText(u.getPrefShippingAddress());
            }
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
            final String orderId = o.getId();

            VBox tile = new VBox(8);
            tile.setAlignment(Pos.TOP_LEFT);
            tile.setStyle("-fx-padding: 12; -fx-background-color: #fafafa; -fx-background-radius: 8; -fx-border-color: #e5e7eb; -fx-border-radius: 8;");

            Label title = new Label(orderId + "  •  $" + String.format("%.2f", o.getTotalAmount()));
            title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            String nm = o.getCustomerName() != null ? o.getCustomerName() : "";
            String ph = o.getCustomerPhone() != null ? o.getCustomerPhone() : "";
            String ctry = o.getCustomerCountry() != null ? o.getCustomerCountry() : "";
            Label contact = new Label(nm + " · " + ph + " · " + ctry);
            contact.setStyle("-fx-text-fill: #4b5563; -fx-font-size: 12px;");
            contact.setWrapText(true);

            Label itemsLine = new Label(buildLineItemSummary(o));
            itemsLine.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px;");
            itemsLine.setWrapText(true);

            Label st = new Label(OrderStatuses.displayLabel(o.getStatus(), LanguageManager.isArabic()));
            st.setStyle("-fx-font-size: 11px; -fx-text-fill: #2563eb;");

            Label when = new Label(o.getEffectivePlacedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            when.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button cancelBtn = new Button(LanguageManager.isArabic() ? "إلغاء الطلب" : "Cancel order");
            cancelBtn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c;");
            cancelBtn.setTooltip(new Tooltip(LanguageManager.isArabic()
                    ? "إلغاء الطلب رقم " + orderId
                    : "Cancel order " + orderId));
            boolean pending = OrderStatuses.PENDING.equals(o.getStatus());
            cancelBtn.setDisable(!pending);
            cancelBtn.setOnAction(e -> handleCancelMyOrder(orderId));

            HBox footer = new HBox(10);
            footer.setAlignment(Pos.CENTER_LEFT);
            footer.getChildren().addAll(st, when, spacer, cancelBtn);

            tile.getChildren().addAll(title, contact, itemsLine, footer);
            myOrdersContainer.getChildren().add(tile);
        }
    }

    private static String buildLineItemSummary(Order o) {
        String prefix = LanguageManager.isArabic() ? "الأصناف: " : "Items: ";
        List<Product> products = o.getProducts();
        if (products == null || products.isEmpty()) {
            return prefix + (LanguageManager.isArabic() ? "—" : "—");
        }
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Product p : products) {
            counts.merge(p.getName(), 1, Integer::sum);
        }
        StringBuilder sb = new StringBuilder(prefix);
        int n = 0;
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            if (n++ > 0) {
                sb.append(" · ");
            }
            sb.append(e.getKey());
            if (e.getValue() > 1) {
                sb.append(" ×").append(e.getValue());
            }
            if (sb.length() > 140) {
                sb.append("…");
                break;
            }
        }
        return sb.toString();
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
        if (orderItemsContainer == null || orderFacade == null) {
            return;
        }
        orderItemsContainer.getChildren().clear();

        List<Product> selectedProducts = orderFacade.getCart();
        double subtotal = 0;

        for (Product p : selectedProducts) {
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
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

        if (subtotalLabel != null) {
            subtotalLabel.setText("$" + String.format("%.2f", subtotal));
        }
        if (totalLabel != null) {
            totalLabel.setText("$" + String.format("%.2f", subtotal));
        }
    }

    private static int countDigits(String raw) {
        if (raw == null) {
            return 0;
        }
        int n = 0;
        for (int i = 0; i < raw.length(); i++) {
            if (Character.isDigit(raw.charAt(i))) {
                n++;
            }
        }
        return n;
    }

    @FXML
    private void handleSubmitOrder() {
        if (orderFacade.getCart().isEmpty()) {
            showAlert(LanguageManager.get("Validation Error"), LanguageManager.isArabic() ? "السلة فارغة، يرجى إضافة منتجات أولاً." : "Cart is empty, please add products first.");
            return;
        }

        String name = customerNameField.getText() != null ? customerNameField.getText().trim() : "";
        if (name.isEmpty()) {
            showAlert(LanguageManager.get("Validation Error"), LanguageManager.get("Customer name is required"));
            return;
        }

        String phone = phoneField.getText() != null ? phoneField.getText().trim() : "";
        if (phone.isEmpty()) {
            showAlert(LanguageManager.get("Validation Error"), LanguageManager.isArabic() ? "رقم الهاتف مطلوب." : "Phone number is required.");
            return;
        }
        if (countDigits(phone) < 11) {
            showAlert(LanguageManager.get("Validation Error"), LanguageManager.get("Phone must be at least 11 digits"));
            return;
        }

        String country = countryField.getText() != null ? countryField.getText().trim() : "";
        if (country.isEmpty()) {
            showAlert(LanguageManager.get("Validation Error"), LanguageManager.get("Country is required"));
            return;
        }

        if (addressArea.getText() == null || addressArea.getText().trim().isEmpty()) {
            showAlert(LanguageManager.get("Validation Error"), LanguageManager.isArabic() ? "العنوان مطلوب." : "Address is required.");
            return;
        }

        Order order = new Order();
        order.setId("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setCustomerName(name);
        order.setCustomerPhone(phone);
        order.setCustomerCountry(country);
        order.setCustomerAddress(addressArea.getText().trim());
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
        orderFacade.persistCheckoutContactForCurrentUser(name, phone, country, addressArea.getText().trim());

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
