package com.supplysync.presentation;

import com.supplysync.dashboard.DashboardDataPort;
import com.supplysync.dashboard.DashboardUiHelper;
import com.supplysync.facade.ApplicationContext;
import com.supplysync.models.AdminDashboardStats;
import com.supplysync.models.Order;
import com.supplysync.patterns.behavioral.observer.OrderObserver;
import javafx.animation.KeyFrame;
import javafx.application.Platform;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;

/**
 * Admin dashboard: depends on {@link DashboardDataPort} (DIP); UI updates via {@link DashboardUiHelper} (SRP).
 */
public class DashboardController extends BaseScreenController implements OrderObserver {
    private static final Duration DASHBOARD_POLL_INTERVAL = Duration.seconds(60);

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

    private DashboardDataPort dashboardPort;
    private Timeline dashboardPoll;
    private boolean scenePollHookInstalled;

    @FXML
    public void initialize() {
        applyMetricPlaceholders();
        installScenePollHook();
        updateStatsFromPort();
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

    private void installScenePollHook() {
        if (root == null || scenePollHookInstalled) {
            return;
        }
        scenePollHookInstalled = true;
        root.sceneProperty().addListener((o, oldScene, newScene) -> {
            if (newScene == null) {
                stopDashboardPolling();
            } else {
                restartDashboardPolling();
            }
        });
        if (root.getScene() != null) {
            restartDashboardPolling();
        }
    }

    private void restartDashboardPolling() {
        stopDashboardPolling();
        if (dashboardPort == null || root == null || root.getScene() == null) {
            return;
        }
        dashboardPoll = new Timeline(new KeyFrame(DASHBOARD_POLL_INTERVAL, e -> updateStatsFromPort()));
        dashboardPoll.setCycleCount(Timeline.INDEFINITE);
        dashboardPoll.play();
    }

    private void stopDashboardPolling() {
        if (dashboardPoll != null) {
            dashboardPoll.stop();
            dashboardPoll = null;
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext app) {
        if (orders() != null) {
            orders().removeOrderObserver(this);
        }
        super.setApplicationContext(app);
        this.dashboardPort = app == null ? null : app.dashboardData();
        if (orders() != null) {
            orders().addOrderObserver(this);
        }
        restartDashboardPolling();
        updateStatsFromPort();
    }

    @Override
    public void onOrderUpdated(Order order) {
        Platform.runLater(this::updateStatsFromPort);
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
        updateStatsFromPort();
    }

    private void updateStatsFromPort() {
        if (dashboardPort == null) {
            if (dashboard() != null) {
                dashboardPort = dashboard();
            } else {
                return;
            }
        }
        AdminDashboardStats s = dashboardPort.loadStatistics();
        DashboardUiHelper.applyMetricCards(s, totalProductsLabel, totalOrdersLabel, pendingOrdersLabel, revenueLabel);
        clearTrend(metricTrendProducts);
        clearTrend(metricTrendOrders);
        clearTrend(metricTrendPending);
        clearTrend(metricTrendRevenue);
        DashboardUiHelper.applyVelocity(s,
                inTransitPctLabel, inTransitProgress,
                stockAvailPctLabel, stockAvailProgress,
                cancelledPctLabel, cancelledProgress);
        DashboardUiHelper.applyInfoBanner(s, dashboardInfoLabel);
        DashboardUiHelper.rebuildStatisticsChart(statsChartBox, chartLegendLabel, s);
        DashboardUiHelper.fillRecentInventory(recentInventoryVBox, dashboardPort.loadProductCatalogSnapshot(), 10);
    }

    private static void clearTrend(Label label) {
        if (label != null) {
            label.setText("");
            label.setManaged(false);
            label.setVisible(false);
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
