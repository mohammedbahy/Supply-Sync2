package com.supplysync.presentation;

import com.supplysync.models.Message;
import com.supplysync.models.Order;
import com.supplysync.models.OrderStatuses;
import com.supplysync.models.Product;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Admin "Reports" view: daily summary cards for the current calendar day (orders, statuses, inventory, messages).
 */
public class NotificationsController extends BaseScreenController {
    @FXML
    private VBox reportsContainer;
    @FXML
    private Label pageTitle;
    @FXML
    private Label pageSubtitle;
    @FXML
    private Label reportDateLabel;

    public void initialize() {
        renderDailyReports();
    }

    @Override
    public void setOrderFacade(com.supplysync.facade.OrderFacade orderFacade) {
        super.setOrderFacade(orderFacade);
        renderDailyReports();
    }

    @Override
    protected void applyLanguage() {
        super.applyLanguage();
        if (pageTitle != null) {
            pageTitle.setText(LanguageManager.get("Reports"));
        }
        if (pageSubtitle != null) {
            pageSubtitle.setText(LanguageManager.isArabic()
                    ? "تقرير يومي يلخص نشاط الطلبات والمخزون والرسائل لتاريخ اليوم."
                    : "Daily report summarizing today's orders, inventory, and notifications.");
        }
        renderDailyReports();
    }

    private void renderDailyReports() {
        if (reportsContainer == null) {
            return;
        }
        reportsContainer.getChildren().clear();

        LocalDate today = LocalDate.now();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")
                .withLocale(LanguageManager.isArabic() ? Locale.forLanguageTag("ar") : Locale.ENGLISH);
        if (reportDateLabel != null) {
            reportDateLabel.setText(LanguageManager.isArabic()
                    ? "تاريخ التقرير: " + today.format(df)
                    : "Report date: " + today.format(df));
        }

        if (orderFacade == null) {
            reportsContainer.getChildren().add(reportCard(
                    LanguageManager.isArabic() ? "لا بيانات" : "No data",
                    LanguageManager.isArabic() ? "لم يتم تحميل الخدمة." : "Service not available."));
            return;
        }

        List<Order> todayOrders = orderFacade.getAllOrders().stream()
                .filter(o -> isOrderPlacedOn(o, today))
                .collect(Collectors.toList());

        double revenueToday = todayOrders.stream()
                .filter(o -> !OrderStatuses.CANCELLED.equals(o.getStatus()))
                .mapToDouble(Order::getTotalAmount)
                .sum();

        String ordersTitle = LanguageManager.isArabic() ? "طلبات اليوم" : "Today's orders";
        StringBuilder ordersBody = new StringBuilder();
        if (todayOrders.isEmpty()) {
            ordersBody.append(LanguageManager.isArabic()
                    ? "لا توجد طلبات وُضعت في سجل اليوم."
                    : "No orders were placed today.");
        } else {
            ordersBody.append(LanguageManager.isArabic()
                    ? String.format("عدد الطلبات: %d — إجمالي قيمة الطلبات غير الملغاة: $%.2f%n%n", todayOrders.size(), revenueToday)
                    : String.format("Orders placed today: %d — non-cancelled total: $%.2f%n%n", todayOrders.size(), revenueToday));
            for (Order o : todayOrders) {
                String cust = o.getCustomerName() != null ? o.getCustomerName() : "-";
                ordersBody.append(String.format("• %s — %s — %s — $%.2f%n",
                        o.getId(),
                        cust,
                        OrderStatuses.displayLabel(o.getStatus(), LanguageManager.isArabic()),
                        o.getTotalAmount()));
            }
        }
        reportsContainer.getChildren().add(reportCard(ordersTitle, ordersBody.toString().trim()));

        long pending = todayOrders.stream().filter(o -> OrderStatuses.PENDING.equals(o.getStatus())).count();
        long transit = todayOrders.stream().filter(o ->
                OrderStatuses.IN_TRANSIT.equals(o.getStatus())
                        || OrderStatuses.APPROVED.equals(o.getStatus())
                        || "SHIPPED".equals(o.getStatus())).count();
        long delivered = todayOrders.stream().filter(o -> OrderStatuses.DELIVERED.equals(o.getStatus())).count();
        long cancelled = todayOrders.stream().filter(o -> OrderStatuses.CANCELLED.equals(o.getStatus())).count();

        String statusTitle = LanguageManager.isArabic()
                ? "ملخص حالات طلبات اليوم"
                : "Today's order status breakdown";
        String statusBody = LanguageManager.isArabic()
                ? String.format("قيد المعالجة: %d%nفي الطريق: %d%nتم التسليم: %d%nملغاة: %d", pending, transit, delivered, cancelled)
                : String.format("Processing: %d%nIn transit: %d%nDelivered: %d%nCancelled: %d", pending, transit, delivered, cancelled);
        reportsContainer.getChildren().add(reportCard(statusTitle, statusBody));

        List<Product> catalog = orderFacade.getCatalog();
        long low = catalog.stream().filter(p -> p.getQuantity() > 0 && p.getQuantity() < 100).count();
        long out = catalog.stream().filter(p -> p.getQuantity() == 0).count();
        int totalSkus = catalog.size();
        int totalUnits = catalog.stream().mapToInt(Product::getQuantity).sum();

        String invTitle = LanguageManager.isArabic()
                ? "المخزون (لقطة حالية)"
                : "Inventory (current snapshot)";
        String invBody = LanguageManager.isArabic()
                ? String.format("عدد الأصناف: %d — إجمالي الوحدات في المخزون: %d — أصناف منخفضة المخزون (<100 وحدة): %d — أصناف نافدة: %d",
                totalSkus, totalUnits, low, out)
                : String.format("SKU count: %d — total stock units: %d — low stock SKUs (<100 units): %d — out of stock SKUs: %d",
                totalSkus, totalUnits, low, out);
        reportsContainer.getChildren().add(reportCard(invTitle, invBody));

        List<Message> msgs = orderFacade.getAllMessages().stream()
                .filter(m -> m.getCreatedAt() != null && m.getCreatedAt().toLocalDate().equals(today))
                .collect(Collectors.toList());
        String msgTitle = LanguageManager.isArabic() ? "رسائل وإشعارات اليوم" : "Today's messages";
        StringBuilder msgBody = new StringBuilder();
        if (msgs.isEmpty()) {
            msgBody.append(LanguageManager.isArabic() ? "لا توجد رسائل مسجّلة اليوم." : "No messages recorded today.");
        } else {
            msgBody.append(LanguageManager.isArabic()
                    ? ("عدد الرسائل: " + msgs.size() + "\n\n")
                    : ("Messages today: " + msgs.size() + "\n\n"));
            for (Message m : msgs) {
                String content = m.getContent() != null ? m.getContent() : "";
                if (content.length() > 160) {
                    content = content.substring(0, 157) + "...";
                }
                msgBody.append("• ").append(m.getTitle() != null ? m.getTitle() : "").append(" — ").append(content).append('\n');
            }
        }
        reportsContainer.getChildren().add(reportCard(msgTitle, msgBody.toString().trim()));
    }

    private static boolean isOrderPlacedOn(Order o, LocalDate day) {
        if (o == null || day == null) {
            return false;
        }
        if (o.getPlacedAt() != null && o.getPlacedAt().toLocalDate().equals(day)) {
            return true;
        }
        return o.getDate() != null && o.getDate().equals(day);
    }

    private static VBox reportCard(String title, String body) {
        VBox card = new VBox(8);
        card.setStyle("-fx-padding: 16; -fx-background-color: #ffffff; -fx-border-color: #e5e7eb; "
                + "-fx-border-radius: 10; -fx-background-radius: 10;");
        Label t = new Label(title);
        t.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");
        Label b = new Label(body);
        b.setWrapText(true);
        b.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");
        card.getChildren().addAll(t, b);
        return card;
    }
}
