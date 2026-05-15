package com.supplysync.workflow;

import com.supplysync.models.Order;
import com.supplysync.models.OrderStatuses;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Defines valid transitions for wholesale order lifecycle (Port Said operations).
 */
public final class OrderStateMachine {
    private OrderStateMachine() {
    }

    public static List<OrderTransition> allowedTransitions(Order order) {
        if (order == null || order.getStatus() == null) {
            return List.of();
        }
        String status = OrderStatuses.normalize(order.getStatus());
        Set<OrderTransition> set = EnumSet.noneOf(OrderTransition.class);
        switch (status) {
            case OrderStatuses.PENDING:
                set.add(OrderTransition.APPROVE);
                set.add(OrderTransition.PLACE_ON_HOLD);
                set.add(OrderTransition.CANCEL);
                break;
            case OrderStatuses.ON_HOLD:
                set.add(OrderTransition.RELEASE_HOLD);
                set.add(OrderTransition.CANCEL);
                break;
            case OrderStatuses.IN_TRANSIT:
                set.add(OrderTransition.MARK_DELIVERED);
                set.add(OrderTransition.SHIP_PARTIAL);
                set.add(OrderTransition.PLACE_ON_HOLD);
                set.add(OrderTransition.CANCEL);
                break;
            case OrderStatuses.PARTIALLY_SHIPPED:
                set.add(OrderTransition.MARK_DELIVERED);
                set.add(OrderTransition.PLACE_ON_HOLD);
                set.add(OrderTransition.CANCEL);
                break;
            case OrderStatuses.DELIVERED:
            case OrderStatuses.CANCELLED:
            default:
                break;
        }
        return new ArrayList<>(set);
    }

    public static String targetStatus(String fromStatus, OrderTransition transition) {
        String from = OrderStatuses.normalize(fromStatus);
        switch (transition) {
            case APPROVE:
                if (OrderStatuses.PENDING.equals(from)) {
                    return OrderStatuses.IN_TRANSIT;
                }
                break;
            case MARK_DELIVERED:
                if (OrderStatuses.IN_TRANSIT.equals(from) || OrderStatuses.PARTIALLY_SHIPPED.equals(from)) {
                    return OrderStatuses.DELIVERED;
                }
                break;
            case CANCEL:
                return OrderStatuses.CANCELLED;
            case PLACE_ON_HOLD:
                if (OrderStatuses.PENDING.equals(from) || OrderStatuses.IN_TRANSIT.equals(from)) {
                    return OrderStatuses.ON_HOLD;
                }
                break;
            case RELEASE_HOLD:
                if (OrderStatuses.ON_HOLD.equals(from)) {
                    return OrderStatuses.PENDING;
                }
                break;
            case SHIP_PARTIAL:
                if (OrderStatuses.IN_TRANSIT.equals(from)) {
                    return OrderStatuses.PARTIALLY_SHIPPED;
                }
                break;
            default:
                break;
        }
        throw new IllegalStateException("Transition " + transition + " not allowed from " + from);
    }

    public static boolean isAllowed(Order order, OrderTransition transition) {
        return allowedTransitions(order).contains(transition);
    }
}
