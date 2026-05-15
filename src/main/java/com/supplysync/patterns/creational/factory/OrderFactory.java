package com.supplysync.patterns.creational.factory;

import com.supplysync.domain.order.OrderStatusHydrator;
import com.supplysync.models.Marketer;
import com.supplysync.models.Order;
import com.supplysync.models.OrderStatuses;
import com.supplysync.models.User;
import com.supplysync.patterns.creational.builder.OrderBuilder;

/**
 * Creates orders ready for workflow submission (AWAITING_APPROVAL).
 */
public final class OrderFactory {
    private OrderFactory() {
    }

    public static Order createAwaitingApproval(OrderBuilder builder, User marketer) {
        Order order = builder.build();
        if (marketer != null && order.getMarketer() != null) {
            order.getMarketer().setName(marketer.getName());
        } else if (marketer != null) {
            order.setMarketer(new Marketer(marketer.getId(), marketer.getName()));
        }
        OrderStatusHydrator.hydrate(order, OrderStatuses.AWAITING_APPROVAL);
        return order;
    }
}
