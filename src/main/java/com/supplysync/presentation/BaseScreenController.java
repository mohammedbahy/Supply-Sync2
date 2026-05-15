package com.supplysync.presentation;

import javafx.event.Event;
import javafx.fxml.FXML;
import java.io.IOException;
import com.supplysync.facade.ApplicationContext;
import com.supplysync.facade.AuthFacade;
import com.supplysync.facade.CatalogFacade;
import com.supplysync.facade.DraftFacade;
import com.supplysync.facade.NotificationFacade;
import com.supplysync.facade.OrderFacade;
import com.supplysync.facade.DashboardFacade;
import javafx.scene.Parent;

public abstract class BaseScreenController {
    protected ApplicationContext app;

    @FXML protected Parent root;

    public void setApplicationContext(ApplicationContext app) {
        this.app = app;
        applyLanguage();
        setupScrolling();
    }

    protected AuthFacade auth() {
        return app != null ? app.auth() : null;
    }

    protected CatalogFacade catalog() {
        return app != null ? app.catalog() : null;
    }

    protected OrderFacade orders() {
        return app != null ? app.orders() : null;
    }

    protected DashboardFacade dashboard() {
        return app != null ? app.dashboard() : null;
    }

    protected NotificationFacade notifications() {
        return app != null ? app.notifications() : null;
    }

    protected DraftFacade drafts() {
        return app != null ? app.drafts() : null;
    }

    protected void applyLanguage() {
        // Language switching removed as requested
    }

    private void setupScrolling() {
        // ScrollPane behavior is configured in FXML where needed.
    }

    @FXML protected void openDashboard(Event event) throws IOException { ScreenNavigator.open(event, "/com/supplysync/presentation/dashboard-view.fxml", "SupplySync Dashboard"); }
    @FXML protected void openProducts(Event event) throws IOException { ScreenNavigator.open(event, "/com/supplysync/presentation/products-management-view.fxml", "SupplySync Products Management"); }
    @FXML protected void openOrders(Event event) throws IOException { ScreenNavigator.open(event, "/com/supplysync/presentation/orders-management-view.fxml", "SupplySync Orders Management"); }
    @FXML protected void openMarketers(Event event) throws IOException { ScreenNavigator.open(event, "/com/supplysync/presentation/marketers-view.fxml", "SupplySync Marketers"); }
    @FXML protected void openReports(Event event) throws IOException { ScreenNavigator.open(event, "/com/supplysync/presentation/notifications-view.fxml", "SupplySync Reports"); }
    @FXML protected void openMarketingDashboard(Event event) throws IOException { ScreenNavigator.open(event, "/com/supplysync/presentation/marketing-dashboard-view.fxml", "SupplySync Marketing Dashboard"); }
    @FXML protected void openProductCatalog(Event event) throws IOException { ScreenNavigator.open(event, "/com/supplysync/presentation/product-catalog-view.fxml", "SupplySync Product Catalog"); }
    @FXML protected void openMyOrders(Event event) throws IOException { ScreenNavigator.open(event, "/com/supplysync/presentation/order-placement-view.fxml", "SupplySync My Orders"); }
    @FXML protected void openMessages(Event event) throws IOException { ScreenNavigator.open(event, "/com/supplysync/presentation/notifications-view.fxml", "SupplySync Messages"); }

    @FXML
    protected void logout(Event event) throws IOException {
        if (app != null) {
            app.logout();
        }
        ScreenNavigator.open(event, "/com/supplysync/presentation/login-view.fxml", "SupplySync Login");
    }
}
