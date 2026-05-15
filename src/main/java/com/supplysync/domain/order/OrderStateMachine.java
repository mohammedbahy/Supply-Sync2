package com.supplysync.domain.order;

import com.supplysync.models.Order;
import com.supplysync.models.OrderStatuses;
import com.supplysync.models.User;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Single source of truth for legal status transitions.
 */
public final class OrderStateMachine {
    private static final Map<String, Map<OrderTransition, String>> TRANSITION_TABLE = buildTable();
    private static final Map<String, Set<OrderTransition>> ROLE_TRANSITIONS = buildRoleTransitions();

    private static Map<String, Map<OrderTransition, String>> buildTable() {
        Map<String, Map<OrderTransition, String>> table = new HashMap<>();

        put(table, OrderStatuses.AWAITING_APPROVAL, OrderTransition.APPROVE, OrderStatuses.APPROVED);
        put(table, OrderStatuses.AWAITING_APPROVAL, OrderTransition.PLACE_ON_HOLD, OrderStatuses.ON_HOLD);
        put(table, OrderStatuses.AWAITING_APPROVAL, OrderTransition.CANCEL, OrderStatuses.CANCELLED);

        put(table, OrderStatuses.APPROVED, OrderTransition.PLACE_ON_HOLD, OrderStatuses.ON_HOLD);
        put(table, OrderStatuses.APPROVED, OrderTransition.SHIP_PARTIAL, OrderStatuses.PARTIALLY_SHIPPED);
        put(table, OrderStatuses.APPROVED, OrderTransition.SHIP, OrderStatuses.IN_TRANSIT);
        put(table, OrderStatuses.APPROVED, OrderTransition.CANCEL, OrderStatuses.CANCELLED);

        put(table, OrderStatuses.ON_HOLD, OrderTransition.RELEASE_HOLD, OrderStatuses.APPROVED);
        put(table, OrderStatuses.ON_HOLD, OrderTransition.CANCEL, OrderStatuses.CANCELLED);

        put(table, OrderStatuses.PARTIALLY_SHIPPED, OrderTransition.SHIP_PARTIAL, OrderStatuses.PARTIALLY_SHIPPED);
        put(table, OrderStatuses.PARTIALLY_SHIPPED, OrderTransition.SHIP, OrderStatuses.IN_TRANSIT);
        put(table, OrderStatuses.PARTIALLY_SHIPPED, OrderTransition.CANCEL, OrderStatuses.CANCELLED);
        put(table, OrderStatuses.PARTIALLY_SHIPPED, OrderTransition.PLACE_ON_HOLD, OrderStatuses.ON_HOLD);

        put(table, OrderStatuses.IN_TRANSIT, OrderTransition.DELIVER, OrderStatuses.DELIVERED);
        put(table, OrderStatuses.IN_TRANSIT, OrderTransition.PLACE_ON_HOLD, OrderStatuses.ON_HOLD);
        put(table, OrderStatuses.IN_TRANSIT, OrderTransition.CANCEL, OrderStatuses.CANCELLED);

        put(table, OrderStatuses.DELIVERED, OrderTransition.RETURN, OrderStatuses.RETURNED);

        put(table, OrderStatuses.PENDING, OrderTransition.APPROVE, OrderStatuses.APPROVED);
        put(table, OrderStatuses.PENDING, OrderTransition.CANCEL, OrderStatuses.CANCELLED);
        put(table, OrderStatuses.PENDING, OrderTransition.PLACE_ON_HOLD, OrderStatuses.ON_HOLD);

        return table;
    }

    private static void put(Map<String, Map<OrderTransition, String>> table,
                            String from,
                            OrderTransition transition,
                            String to) {
        table.computeIfAbsent(from, k -> new EnumMap<>(OrderTransition.class)).put(transition, to);
    }

    private static Map<String, Set<OrderTransition>> buildRoleTransitions() {
        Map<String, Set<OrderTransition>> map = new HashMap<>();
        map.put("ADMIN", EnumSet.allOf(OrderTransition.class));
        map.put("ACCOUNTANT", EnumSet.of(
                OrderTransition.APPROVE,
                OrderTransition.PLACE_ON_HOLD,
                OrderTransition.RELEASE_HOLD,
                OrderTransition.CANCEL
        ));
        map.put("WAREHOUSE_MANAGER", EnumSet.of(
                OrderTransition.SHIP_PARTIAL,
                OrderTransition.SHIP,
                OrderTransition.DELIVER
        ));
        map.put("MARKETER", EnumSet.of(OrderTransition.CANCEL));
        return map;
    }

    public boolean isAdmin(User actor) {
        return actor != null
                && actor.getRole() != null
                && "ADMIN".equalsIgnoreCase(actor.getRole());
    }

    /** Repairs DB/UI mismatch: status_before_hold set but status not ON_HOLD. */
    public void repairHoldState(Order order) {
        if (order == null) {
            return;
        }
        if (order.getStatusBeforeHold() != null
                && !order.getStatusBeforeHold().isBlank()
                && !OrderStatuses.ON_HOLD.equals(order.getStatus())) {
            order.internalSetWorkflowStatus(OrderStatuses.ON_HOLD);
        }
    }

    public boolean isTransitionPermitted(Order order, User actor, OrderTransition transition) {
        if (isAdmin(actor)) {
            return true;
        }
        return getAllowedTransitions(order, actor).contains(transition);
    }

    public String resolveTargetStatus(Order order, OrderTransition transition) {
        return resolveTargetStatus(order, transition, null);
    }

    public String resolveTargetStatus(Order order, OrderTransition transition, User actor) {
        repairHoldState(order);
        if (isAdmin(actor)) {
            return resolveAdminTargetStatus(order, transition);
        }
        if (transition == OrderTransition.RELEASE_HOLD
                && OrderStatuses.ON_HOLD.equals(order.getStatus())
                && order.getStatusBeforeHold() != null
                && !order.getStatusBeforeHold().isBlank()) {
            return order.getStatusBeforeHold();
        }
        String current = order.getStatus();
        Map<OrderTransition, String> row = TRANSITION_TABLE.get(current);
        if (row == null) {
            throw new IllegalStateException("No transitions defined for status: " + current);
        }
        String target = row.get(transition);
        if (target == null) {
            throw new IllegalStateException(
                    "Transition " + transition + " is not allowed from status " + current);
        }
        return target;
    }

    private String resolveAdminTargetStatus(Order order, OrderTransition transition) {
        String status = order.getStatus();

        if (transition == OrderTransition.RELEASE_HOLD) {
            if (OrderStatuses.ON_HOLD.equals(status)) {
                String resume = order.getStatusBeforeHold();
                return resume != null && !resume.isBlank() ? resume : OrderStatuses.APPROVED;
            }
            if (order.getStatusBeforeHold() != null && !order.getStatusBeforeHold().isBlank()) {
                return order.getStatusBeforeHold();
            }
            return status;
        }

        if (transition == OrderTransition.PLACE_ON_HOLD) {
            if (OrderStatuses.ON_HOLD.equals(status)) {
                return status;
            }
            return OrderStatuses.ON_HOLD;
        }

        try {
            return resolveTargetStatus(order, transition);
        } catch (IllegalStateException ex) {
            return status;
        }
    }

    public Set<OrderTransition> getAllowedTransitions(Order order, User actor) {
        if (actor == null || actor.getRole() == null) {
            return Collections.emptySet();
        }
        repairHoldState(order);
        if (isAdmin(actor)) {
            return adminAllowedTransitions(order);
        }
        String current = order.getStatus();
        Map<OrderTransition, String> row = TRANSITION_TABLE.get(current);
        if (row == null || row.isEmpty()) {
            return Collections.emptySet();
        }
        Set<OrderTransition> roleAllowed = ROLE_TRANSITIONS.getOrDefault(
                actor.getRole().toUpperCase(),
                Collections.emptySet()
        );
        Set<OrderTransition> result = EnumSet.noneOf(OrderTransition.class);
        for (OrderTransition t : row.keySet()) {
            if (roleAllowed.contains(t)) {
                result.add(t);
            }
        }
        return result;
    }

    private Set<OrderTransition> adminAllowedTransitions(Order order) {
        String current = order.getStatus();
        EnumSet<OrderTransition> result = EnumSet.noneOf(OrderTransition.class);
        Map<OrderTransition, String> row = TRANSITION_TABLE.get(current);
        if (row != null) {
            result.addAll(row.keySet());
        }
        if (order.getStatusBeforeHold() != null && !order.getStatusBeforeHold().isBlank()) {
            result.add(OrderTransition.RELEASE_HOLD);
        }
        if (!isTerminal(current)) {
            result.add(OrderTransition.CANCEL);
        }
        return result;
    }

    private static boolean isTerminal(String status) {
        return OrderStatuses.DELIVERED.equals(status)
                || OrderStatuses.CANCELLED.equals(status)
                || OrderStatuses.RETURNED.equals(status);
    }

    public void applyTransition(Order order, OrderTransition transition) {
        applyTransition(order, transition, null);
    }

    public void applyTransition(Order order, OrderTransition transition, User actor) {
        repairHoldState(order);
        String from = order.getStatus();

        if (transition == OrderTransition.PLACE_ON_HOLD) {
            if (OrderStatuses.ON_HOLD.equals(from)) {
                if (!isAdmin(actor)) {
                    throw new IllegalStateException("Order is already on hold");
                }
                return;
            }
            order.setStatusBeforeHold(from);
        }

        String target = resolveTargetStatus(order, transition, actor);

        if (transition == OrderTransition.RELEASE_HOLD) {
            order.setStatusBeforeHold(null);
        }
        if (!target.equals(from)) {
            order.internalSetWorkflowStatus(target);
        }
    }
}
