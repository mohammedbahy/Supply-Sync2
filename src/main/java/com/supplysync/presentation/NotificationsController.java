package com.supplysync.presentation;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.List;
import com.supplysync.models.Message;

public class NotificationsController extends BaseScreenController {
    @FXML private VBox todayContainer;
    @FXML private VBox yesterdayContainer;
    @FXML private Label pageTitle;
    @FXML private Button markAllReadBtn;

    private static final List<Notification> notifications = new ArrayList<>();

    static {
        notifications.add(new Notification("New Order", "Order ORD-9921 has been submitted.", "TODAY", false));
        notifications.add(new Notification("Low Stock", "Samsung TV 55 Inch is low on stock (5 units left).", "TODAY", false));
        notifications.add(new Notification("System Update", "New products have been added to the catalog.", "YESTERDAY", true));
    }

    public void initialize() {
        renderNotifications();
    }

    @Override
    public void setOrderFacade(com.supplysync.facade.OrderFacade orderFacade) {
        super.setOrderFacade(orderFacade);
        renderNotifications();
    }

    @Override
    protected void applyLanguage() {
        super.applyLanguage();
        if (pageTitle != null) pageTitle.setText(LanguageManager.get("System Notifications"));
        if (markAllReadBtn != null) markAllReadBtn.setText(LanguageManager.get("Mark all as read"));
    }

    private void renderNotifications() {
        if (todayContainer == null) return;
        todayContainer.getChildren().clear();
        yesterdayContainer.getChildren().clear();

        // Get real messages from orders
        List<Message> messages = new ArrayList<>();
        if (orderFacade != null) {
            messages.addAll(orderFacade.getAllMessages());
        }

        // Add mock notifications
        for (Notification n : notifications) {
            VBox card = createNotificationCard(n);
            if ("TODAY".equals(n.day)) {
                todayContainer.getChildren().add(card);
            } else {
                yesterdayContainer.getChildren().add(card);
            }
        }

        // Add real messages
        for (Message msg : messages) {
            VBox card = createMessageCard(msg);
            if ("TODAY".equals(msg.getStatus())) {
                todayContainer.getChildren().add(card);
            } else {
                yesterdayContainer.getChildren().add(card);
            }
        }
    }

    private VBox createNotificationCard(Notification n) {
        VBox card = new VBox(5);
        card.setStyle("-fx-padding: 15; -fx-background-color: " + (n.isRead ? "#f9fafb" : "#ffffff") + 
                      "; -fx-border-color: #e5e7eb; -fx-border-radius: 8; -fx-background-radius: 8;");
        
        Label title = new Label(n.title);
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label content = new Label(n.content);
        content.setWrapText(true);
        
        card.getChildren().addAll(title, content);
        card.setOnMouseClicked(e -> {
            n.isRead = true;
            renderNotifications();
        });
        
        return card;
    }

    private VBox createMessageCard(Message msg) {
        VBox card = new VBox(5);
        card.setStyle("-fx-padding: 15; -fx-background-color: " + (msg.isRead() ? "#f9fafb" : "#ffffff") + 
                      "; -fx-border-color: #4CAF50; -fx-border-radius: 8; -fx-background-radius: 8;");
        
        Label title = new Label(msg.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #4CAF50;");
        
        Label content = new Label(msg.getContent());
        content.setWrapText(true);
        
        Label timestamp = new Label("Order: " + msg.getOrderId() + " | " + msg.getCreatedAt().toString());
        timestamp.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
        
        card.getChildren().addAll(title, content, timestamp);
        card.setOnMouseClicked(e -> {
            msg.setRead(true);
            renderNotifications();
        });
        
        return card;
    }

    @FXML
    private void handleMarkAllRead() {
        for (Notification n : notifications) n.isRead = true;
        if (orderFacade != null) {
            for (Message msg : orderFacade.getAllMessages()) {
                msg.setRead(true);
            }
        }
        renderNotifications();
    }

    private static class Notification {
        String title, content, day;
        boolean isRead;
        Notification(String t, String c, String d, boolean r) {
            title = t; content = c; day = d; isRead = r;
        }
    }
}
