package com.supplysync.presentation;

import com.supplysync.facade.OrderFacade;
import com.supplysync.models.AdminDashboardStats;
import com.supplysync.models.Product;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DashboardController extends BaseScreenController {
    @FXML
    private Label totalOrdersLabel;
    @FXML
    private Label pendingOrdersLabel;
    @FXML
    private Label totalProductsLabel;
    @FXML
    private Label revenueLabel;
    @FXML
    private Label dashboardTitle;
    @FXML
    private Label dashboardSubtitle;
    @FXML
    private Label metricTrendProducts;
    @FXML
    private Label metricTrendOrders;
    @FXML
    private Label metricTrendPending;
    @FXML
    private Label metricTrendRevenue;
    @FXML
    private HBox statsChartBox;
    @FXML
    private Label chartLegendLabel;
    @FXML
    private Label inTransitPctLabel;
    @FXML
    private ProgressBar inTransitProgress;
    @FXML
    private Label stockAvailPctLabel;
    @FXML
    private ProgressBar stockAvailProgress;
    @FXML
    private Label cancelledPctLabel;
    @FXML
    private ProgressBar cancelledProgress;
    @FXML
    private Label dashboardInfoLabel;
    @FXML
    private VBox recentInventoryVBox;

    public void initialize() {
        applyMetricPlaceholders();
        updateStats();
    }

    /** Safe defaults (no `$` in FXML — `$` starts an FXML expression and breaks loading). */
    private void applyMetricPlaceholders() {
        if (totalProductsLabel != null) {
            totalProductsLabel.setText("0");
        }
        if (totalOrdersLabel != null) {
            totalOrdersLabel.setText("0");
        }
        if (pendingOrdersLabel != null) {
            pendingOrdersLabel.setText("0");
        }
        if (revenueLabel != null) {
            revenueLabel.setText("$0.00");
        }
        if (inTransitPctLabel != null) {
            inTransitPctLabel.setText("0%");
        }
        if (inTransitProgress != null) {
            inTransitProgress.setProgress(0);
        }
        if (stockAvailPctLabel != null) {
            stockAvailPctLabel.setText("0%");
        }
        if (stockAvailProgress != null) {
            stockAvailProgress.setProgress(0);
        }
        if (cancelledPctLabel != null) {
            cancelledPctLabel.setText("0%");
        }
        if (cancelledProgress != null) {
            cancelledProgress.setProgress(0);
        }
        if (dashboardInfoLabel != null) {
            dashboardInfoLabel.setText("");
        }
    }

    @Override
    public void setOrderFacade(OrderFacade orderFacade) {
        super.setOrderFacade(orderFacade);
        updateStats();
    }

    @Override
    protected void applyLanguage() {
        super.applyLanguage();
        if (dashboardTitle != null) {
            dashboardTitle.setText(LanguageManager.get("Dashboard Overview"));
        }
        if (dashboardSubtitle != null) {
            dashboardSubtitle.setText(LanguageManager.isArabic()
                    ? "بيانات مباشرة من قاعدة البيانات."
                    : "Live figures loaded from the database.");
        }
        updateStats();
    }

    private void updateStats() {
        if (orderFacade == null) {
            return;
        }
        AdminDashboardStats s = orderFacade.getAdminDashboardStats();

        if (totalProductsLabel != null) {
            totalProductsLabel.setText(String.valueOf(s.skuCount));
        }
        if (totalOrdersLabel != null) {
            totalOrdersLabel.setText(String.valueOf(s.orderCount));
        }
        if (pendingOrdersLabel != null) {
            pendingOrdersLabel.setText(String.valueOf(s.pendingOrders));
        }
        if (revenueLabel != null) {
            revenueLabel.setText("$" + String.format("%.2f", s.totalRevenue));
        }

        clearTrend(metricTrendProducts);
        clearTrend(metricTrendOrders);
        clearTrend(metricTrendPending);
        clearTrend(metricTrendRevenue);

        if (inTransitPctLabel != null) {
            inTransitPctLabel.setText(formatPct(s.activePipelineFraction));
        }
        if (inTransitProgress != null) {
            inTransitProgress.setProgress(clamp01(s.activePipelineFraction));
        }
        if (stockAvailPctLabel != null) {
            stockAvailPctLabel.setText(formatPct(s.stockAvailabilityFraction));
        }
        if (stockAvailProgress != null) {
            stockAvailProgress.setProgress(clamp01(s.stockAvailabilityFraction));
        }
        if (cancelledPctLabel != null) {
            cancelledPctLabel.setText(formatPct(s.cancelledFraction));
        }
        if (cancelledProgress != null) {
            cancelledProgress.setProgress(clamp01(s.cancelledFraction));
        }

        if (dashboardInfoLabel != null) {
            if (LanguageManager.isArabic()) {
                dashboardInfoLabel.setText(String.format(
                        "إجمالي وحدات المخزون: %d عبر %d صنف. %d طلب بحاجة للمراجعة.",
                        s.totalStockUnits, s.skuCount, s.pendingOrders));
            } else {
                dashboardInfoLabel.setText(String.format(
                        "%d stock units across %d SKUs. %d order(s) pending review.",
                        s.totalStockUnits, s.skuCount, s.pendingOrders));
            }
        }

        buildStatsChart(s);
        renderRecentInventory();
    }

    private static void clearTrend(Label label) {
        if (label != null) {
            label.setText("");
            label.setManaged(false);
            label.setVisible(false);
        }
    }

    private static String formatPct(double fraction) {
        return String.format("%.0f%%", clamp01(fraction) * 100.0);
    }

    private static double clamp01(double v) {
        if (v < 0) {
            return 0;
        }
        if (v > 1) {
            return 1;
        }
        return v;
    }

    private void buildStatsChart(AdminDashboardStats s) {
        if (statsChartBox == null) {
            return;
        }
        statsChartBox.getChildren().clear();
        int[] heights = s.chartBarHeights();
        String[] names = LanguageManager.isArabic()
                ? new String[]{"معلق", "في الطريق", "مسلّم", "ملغى", "أصناف", "تنبيهات"}
                : new String[]{"Pending", "In transit", "Delivered", "Cancelled", "SKUs", "Alerts"};

        for (int i = 0; i < 6; i++) {
            VBox col = new VBox(6);
            col.setAlignment(Pos.BOTTOM_CENTER);
            col.setPrefWidth(72);

            Region bar = new Region();
            bar.getStyleClass().add("bar");
            int h = heights[i];
            bar.setStyle("-fx-pref-height:" + h + "px; -fx-min-height:" + h + "px; -fx-max-width:56px;");

            Label cap = new Label(names[i]);
            cap.setStyle("-fx-font-size:10px; -fx-text-fill:#6b7280; -fx-wrap-text:true; -fx-text-alignment:center;");
            cap.setMaxWidth(70);

            col.getChildren().addAll(bar, cap);
            statsChartBox.getChildren().add(col);
        }

        if (chartLegendLabel != null) {
            chartLegendLabel.setText(LanguageManager.isArabic()
                    ? "أعمدة: طلبات حسب الحالة + عدد الأصناف + تنبيهات المخزون المنخفض/النفاد."
                    : "Bars: order counts by status, SKU count, and low/out-of-stock alerts.");
        }
    }

    private void renderRecentInventory() {
        if (recentInventoryVBox == null || orderFacade == null) {
            return;
        }
        recentInventoryVBox.getChildren().clear();

        List<Product> products = new ArrayList<>(orderFacade.getCatalog());
        products.sort(Comparator.comparingInt(p -> {
            try {
                return -Integer.parseInt(p.getId().trim());
            } catch (NumberFormatException e) {
                return 0;
            }
        }));

        int limit = 10;
        for (Product p : products.subList(0, Math.min(limit, products.size()))) {
            recentInventoryVBox.getChildren().add(buildInventoryRow(p));
        }
    }

    private HBox buildInventoryRow(Product p) {
        HBox row = new HBox();
        row.getStyleClass().add("table-row");
        row.setSpacing(10);

        Label name = new Label(p.getName());
        name.getStyleClass().add("inv-table-cell");

        Label sku = new Label(p.getId());
        sku.getStyleClass().add("inv-table-cell");

        Label cat = new Label(LanguageManager.get(p.getCategory()));
        cat.getStyleClass().add("inv-table-cell");

        String statusKey = stockStatusKey(p.getQuantity());
        Label st = new Label(LanguageManager.get(statusKey));
        st.getStyleClass().add(statusStyleClass(statusKey));

        Label price = new Label("$" + String.format("%.2f", p.getPrice()));
        price.getStyleClass().addAll("inv-table-cell", "price-col");

        row.getChildren().addAll(name, sku, cat, st, price);
        return row;
    }

    private static String stockStatusKey(int qty) {
        if (qty == 0) {
            return "OUT OF STOCK";
        }
        if (qty < 100) {
            return "LOW STOCK";
        }
        return "IN STOCK";
    }

    private static String statusStyleClass(String statusKey) {
        switch (statusKey) {
            case "OUT OF STOCK":
                return "status-red";
            case "LOW STOCK":
                return "status-yellow";
            default:
                return "status-green";
        }
    }

    @FXML
    private void openProductsFromDashboard(Event event) throws IOException {
        openProducts(event);
    }

    @FXML
    private void showHelp() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(LanguageManager.get("Support"));
        alert.setHeaderText(LanguageManager.get("Dashboard Overview"));
        alert.setContentText(LanguageManager.isArabic()
                ? "الأرقام والرسوم البيانية تُحمّل من قاعدة البيانات (المنتجات والطلبات)."
                : "Figures and charts load from the database (products and orders).");
        alert.showAndWait();
    }

    @FXML
    private void handleExportReport() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(LanguageManager.get("Reports"));
        dialog.setHeaderText(LanguageManager.isArabic() ? "كتابة تقرير جديد" : "Write New Report");
        dialog.setContentText(LanguageManager.isArabic() ? "أدخل محتوى التقرير:" : "Enter report content:");

        dialog.showAndWait().ifPresent(content -> {
            System.out.println("Report sent: " + content);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText(LanguageManager.isArabic() ? "تم إرسال التقرير بنجاح" : "Report sent successfully");
            alert.showAndWait();
        });
    }
}
