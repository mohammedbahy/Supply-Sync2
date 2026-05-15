package com.supplysync.domain.order;

import com.supplysync.models.Order;
import com.supplysync.models.OrderStatuses;

/**
 * Restores workflow status from persistence. Not for controllers or facades.
 */
public final class OrderStatusHydrator {
    private OrderStatusHydrator() {
    }

    public static void hydrate(Order order, String persistedStatus) {
        order.internalSetWorkflowStatus(OrderStatuses.normalizeWorkflow(persistedStatus));
    }
}
