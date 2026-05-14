package com.supplysync.dashboard;

import com.supplysync.models.AdminDashboardStats;
import com.supplysync.models.Product;
import com.supplysync.presentation.LanguageManager;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Pure presentation helpers for the admin dashboard (SRP: no data loading, only formatting / node building).
 */
public final class DashboardUiHelper {
    private DashboardUiHelper() {}

    public static void applyMetricCards(
            AdminDashboardStats s,
            Label totalProductsLabel,
            Label totalOrdersLabel,
            Label pendingOrdersLabel,
            Label revenueLabel) {
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
    }

    public static void applyVelocity(
            AdminDashboardStats s,
            Label inTransitPctLabel,
            ProgressBar inTransitProgress,
            Label stockAvailPctLabel,
            ProgressBar stockAvailProgress,
            Label cancelledPctLabel,
            ProgressBar cancelledProgress) {
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
    }

    public static void applyInfoBanner(AdminDashboardStats s, Label dashboardInfoLabel) {
        if (dashboardInfoLabel == null) {
            return;
        }
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

    public static void rebuildStatisticsChart(HBox statsChartBox, Label chartLegendLabel, AdminDashboardStats s) {
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

    public static void fillRecentInventory(VBox container, List<Product> catalogSnapshot, int limit) {
        if (container == null) {
            return;
        }
        container.getChildren().clear();
        List<Product> products = new ArrayList<>(catalogSnapshot);
        products.sort(Comparator.comparingInt(p -> {
            try {
                return -Integer.parseInt(p.getId().trim());
            } catch (NumberFormatException e) {
                return 0;
            }
        }));
        int n = Math.min(limit, products.size());
        for (int i = 0; i < n; i++) {
            container.getChildren().add(buildInventoryRow(products.get(i)));
        }
    }

    public static HBox buildInventoryRow(Product p) {
        HBox row = new HBox(10);
        row.getStyleClass().add("table-row");

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

    public static String formatPct(double fraction) {
        return String.format("%.0f%%", clamp01(fraction) * 100.0);
    }

    public static double clamp01(double v) {
        if (v < 0) {
            return 0;
        }
        if (v > 1) {
            return 1;
        }
        return v;
    }
}
