package com.supplysync.domain.order;

import com.supplysync.models.Order;
import com.supplysync.models.OrderStatuses;
import com.supplysync.models.User;
import com.supplysync.services.delivery.DeliveryService;

public final class DeliveryTransitionSideEffect implements OrderTransitionSideEffect {
    private final DeliveryService deliveryService;

    public DeliveryTransitionSideEffect(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
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
        if (transition == OrderTransition.SHIP && OrderStatuses.IN_TRANSIT.equals(toStatus)) {
            deliveryService.schedule(order);
        }
    }
}
