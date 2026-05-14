package com.supplysync.presentation;

import javafx.event.Event;
import javafx.fxml.FXML;
import java.io.IOException;
import com.supplysync.facade.OrderFacade;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.Node;
import javafx.scene.Parent;

public abstract class BaseScreenController {
    protected OrderFacade orderFacade;


    @FXML protected Parent root; // Assuming FXMLs might have a root ID or we wrap them

    public void setOrderFacade(OrderFacade orderFacade) {
        this.orderFacade = orderFacade;
        applyLanguage();
        setupScrolling();
    }

    protected void applyLanguage() {
        // Language switching removed as requested
    }

    private void setupScrolling() {
        // Implementation logic for scrolling can be handled via FXML wrapping in ScrollPane
        // But we will also ensure the mouse wheel works by default (JavaFX handles this if ScrollPane is present)
    }

    private String getCurrentFxmlPath() {
        String name = this.getClass().getSimpleName();
        if (name.endsWith("Controller")) {
            String base = name.substring(0, name.length() - "Controller".length());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < base.length(); i++) {
                char c = base.charAt(i);
                if (Character.isUpperCase(c) && i > 0) {
                    sb.append("-");
                }
                sb.append(Character.toLowerCase(c));
            }
            return "/com/supplysync/presentation/" + sb.toString() + "-view.fxml";
        }
        return null;
    }

    @FXML protected void openDashboard(Event event) throws IOException { ScreenNavigator.open(event, "/com/supplysync/presentation/dashboard-view.fxml", "SupplySync Dashboard"); }
    @FXML protected void openProducts(Event event) throws IOException { ScreenNavigator.open(event, "/com/supplysync/presentation/products-management-view.fxml", "SupplySync Products Management"); }
    @FXML protected void openOrders(Event event) throws IOException { ScreenNavigator.open(event, "/com/supplysync/presentation/orders-management-view.fxml", "SupplySync Orders Management"); }
    @FXML protected void openMarketers(Event event) throws IOException { ScreenNavigator.open(event, "/com/supplysync/presentation/marketers-view.fxml", "SupplySync Marketers"); }
    @FXML protected void openReports(Event event) throws IOException { ScreenNavigator.open(event, "/com/supplysync/presentation/notifications-view.fxml", "SupplySync Reports"); }
    @FXML protected void openSupport(Event event) throws IOException { ScreenNavigator.open(event, "/com/supplysync/presentation/notifications-view.fxml", "SupplySync Support"); }
    @FXML protected void openMarketingDashboard(Event event) throws IOException { ScreenNavigator.open(event, "/com/supplysync/presentation/marketing-dashboard-view.fxml", "SupplySync Marketing Dashboard"); }
    @FXML protected void openProductCatalog(Event event) throws IOException { ScreenNavigator.open(event, "/com/supplysync/presentation/product-catalog-view.fxml", "SupplySync Product Catalog"); }
    @FXML protected void openMyOrders(Event event) throws IOException { ScreenNavigator.open(event, "/com/supplysync/presentation/order-placement-view.fxml", "SupplySync My Orders"); }
    @FXML protected void openMessages(Event event) throws IOException { ScreenNavigator.open(event, "/com/supplysync/presentation/notifications-view.fxml", "SupplySync Messages"); }
    
    @FXML
    protected void logout(Event event) throws IOException {
        if (orderFacade != null) {
            orderFacade.logout();
        }
        ScreenNavigator.open(event, "/com/supplysync/presentation/login-view.fxml", "SupplySync Login");
    }
}
