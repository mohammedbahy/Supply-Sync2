package com.supplysync.patterns;

import com.supplysync.models.OrderStatuses;

/**
 * Maps persisted status strings to {@link OrderState} instances (legacy APPROVED/SHIPPED supported).
 */
public final class OrderStates {
    private OrderStates() {
    }

    public static OrderState forStatus(String status) {
        if (status == null) {
            return new PendingState();
        }
        String normalized = OrderStatuses.normalize(status);
        switch (normalized) {
            case OrderStatuses.IN_TRANSIT:
                return new InTransitState();
            case OrderStatuses.DELIVERED:
                return new DeliveredState();
            case OrderStatuses.CANCELLED:
                return new CancelledState();
            case OrderStatuses.PENDING:
            default:
                return new PendingState();
        }
    }
}
