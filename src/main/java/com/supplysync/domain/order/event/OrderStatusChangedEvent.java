package com.supplysync.domain.order.event;

import com.supplysync.domain.order.OrderTransition;
import com.supplysync.models.Order;
import com.supplysync.models.User;

import java.time.LocalDateTime;

public final class OrderStatusChangedEvent {
    private final Order order;
    private final String fromStatus;
    private final String toStatus;
    private final OrderTransition transition;
    private final User actor;
    private final LocalDateTime occurredAt;

    public OrderStatusChangedEvent(Order order,
                                   String fromStatus,
                                   String toStatus,
                                   OrderTransition transition,
                                   User actor) {
        this.order = order;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.transition = transition;
        this.actor = actor;
        this.occurredAt = LocalDateTime.now();
    }

    public Order getOrder() {
        return order;
    }

    public String getFromStatus() {
        return fromStatus;
    }

    public String getToStatus() {
        return toStatus;
    }

    public OrderTransition getTransition() {
        return transition;
    }

    public User getActor() {
        return actor;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
