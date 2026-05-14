package com.supplysync.patterns.structural.decorator;

import com.supplysync.models.Order;
import com.supplysync.services.notification.NotificationService;

public class AuditNotificationDecorator extends NotificationDecorator {
    public AuditNotificationDecorator(NotificationService wrapped) {
        super(wrapped);
    }

    @Override
    public void notifyOrderUpdate(Order order) {
        super.notifyOrderUpdate(order);
        // Add audit behavior placeholder.
    }
}
