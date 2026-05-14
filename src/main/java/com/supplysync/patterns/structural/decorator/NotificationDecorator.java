package com.supplysync.patterns.structural.decorator;

import com.supplysync.models.Order;
import com.supplysync.services.notification.NotificationService;

public abstract class NotificationDecorator implements NotificationService {
    protected final NotificationService wrapped;

    protected NotificationDecorator(NotificationService wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void notifyOrderUpdate(Order order) {
        wrapped.notifyOrderUpdate(order);
    }
}
