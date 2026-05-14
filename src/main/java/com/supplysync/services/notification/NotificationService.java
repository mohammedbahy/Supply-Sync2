package com.supplysync.services.notification;

import com.supplysync.models.Order;

public interface NotificationService {
    void notifyOrderUpdate(Order order);
}
