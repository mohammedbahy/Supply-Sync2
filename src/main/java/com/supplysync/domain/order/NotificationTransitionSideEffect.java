package com.supplysync.domain.order;

import com.supplysync.models.Order;
import com.supplysync.models.User;
import com.supplysync.services.notification.NotificationService;

public final class NotificationTransitionSideEffect implements OrderTransitionSideEffect {
    private final NotificationService notificationService;

    public NotificationTransitionSideEffect(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void beforeTransition(Order order,
                                 OrderTransition transition,
                                 String fromStatus,
                                 String toStatus,
                                 User actor) {
    }

    @Override
    public void afterTransition(Order order,
                                OrderTransition transition,
                                String fromStatus,
                                String toStatus,
                                User actor) {
        notificationService.notifyOrderUpdate(order);
    }
}
