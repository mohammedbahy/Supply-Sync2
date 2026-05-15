package com.supplysync.domain.order;

import com.supplysync.models.Order;
import com.supplysync.models.OrderStatuses;
import com.supplysync.models.User;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Validates role, ownership, credit, and time-window rules before transitions.
 */
public final class OrderTransitionGuard {
    private static final double APPROVAL_CREDIT_LIMIT_EGP = 100_000.0;
    private static final int MARKETER_CANCEL_HOURS = 24;

    public void validate(Order order, OrderTransition transition, User actor) {
        if (order == null) {
            throw new IllegalArgumentException("Order is required");
        }
        if (actor == null || actor.getRole() == null) {
            throw new SecurityException("Authenticated user with role is required");
        }

        String role = actor.getRole().toUpperCase();

        switch (transition) {
            case APPROVE:
                requireRole(role, "ADMIN", "ACCOUNTANT");
                if (order.getTotalAmount() > APPROVAL_CREDIT_LIMIT_EGP) {
                    requireRole(role, "ADMIN");
                }
                break;
            case PLACE_ON_HOLD:
            case RELEASE_HOLD:
                requireRole(role, "ADMIN", "ACCOUNTANT");
                break;
            case SHIP_PARTIAL:
            case SHIP:
            case DELIVER:
                requireRole(role, "ADMIN", "WAREHOUSE_MANAGER");
                break;
            case RETURN:
                requireRole(role, "ADMIN");
                break;
            case CANCEL:
                validateCancel(order, actor, role);
                break;
            default:
                throw new IllegalStateException("Unknown transition: " + transition);
        }
    }

    public void validateSubmit(Order order, User actor) {
        if (order == null) {
            throw new IllegalArgumentException("Order is required");
        }
        if (actor == null) {
            throw new SecurityException("Marketer must be signed in to submit an order");
        }
        if (order.getProducts() == null || order.getProducts().isEmpty()) {
            throw new IllegalStateException("Cannot submit an order with no products");
        }
        if (order.getId() == null || order.getMarketerId() == null) {
            throw new IllegalStateException("Order id and marketer are required");
        }
    }

    private void validateCancel(Order order, User actor, String role) {
        if ("MARKETER".equals(role)) {
            if (!isMarketerOwner(order, actor)) {
                throw new SecurityException("Marketer can only cancel own orders");
            }
            String status = order.getStatus();
            if (!OrderStatuses.AWAITING_APPROVAL.equals(status)
                    && !OrderStatuses.PENDING.equals(status)) {
                throw new IllegalStateException("Marketer can only cancel orders awaiting approval");
            }
            long hours = ChronoUnit.HOURS.between(order.getEffectivePlacedAt(), LocalDateTime.now());
            if (hours >= MARKETER_CANCEL_HOURS) {
                throw new IllegalStateException("Cannot cancel order older than 24 hours");
            }
            return;
        }
        requireRole(role, "ADMIN", "ACCOUNTANT");
    }

    private static boolean isMarketerOwner(Order order, User actor) {
        if (order.getMarketer() != null && actor.getId() != null) {
            return actor.getId().equals(order.getMarketer().getId());
        }
        return order.getCustomerName() != null
                && actor.getName() != null
                && order.getCustomerName().trim().equalsIgnoreCase(actor.getName().trim());
    }

    private static void requireRole(String actual, String... allowed) {
        for (String r : allowed) {
            if (r.equals(actual)) {
                return;
            }
        }
        throw new SecurityException("Role " + actual + " is not allowed for this action");
    }
}
