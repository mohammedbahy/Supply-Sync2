package com.supplysync.workflow;

import com.supplysync.models.Order;
import com.supplysync.models.OrderStatusHistoryEntry;
import com.supplysync.models.OrderStatuses;
import com.supplysync.models.User;
import com.supplysync.repository.Storage;
import com.supplysync.services.inventory.InventoryService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class OrderWorkflowService {
    private final Storage storage;
    private final InventoryService inventoryService;

    public OrderWorkflowService(Storage storage, InventoryService inventoryService) {
        this.storage = Objects.requireNonNull(storage);
        this.inventoryService = Objects.requireNonNull(inventoryService);
    }

    public List<OrderTransition> getAllowedTransitions(Order order) {
        return OrderStateMachine.allowedTransitions(order);
    }

    public Order executeTransition(String orderId, OrderTransition transition, User actor) {
        Order order = storage.findOrderById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        if (!OrderStateMachine.isAllowed(order, transition)) {
            throw new IllegalStateException("Transition " + transition + " not allowed for order " + orderId);
        }

        String from = order.getStatus();
        String to = OrderStateMachine.targetStatus(from, transition);

        if (transition == OrderTransition.CANCEL) {
            inventoryService.restoreInventory(order);
        }

        order.setStatus(to);
        storage.saveOrder(order);
        appendHistory(orderId, from, to, transition, actor);
        return order;
    }

    public void recordOrderPlaced(Order order, User actor) {
        if (order == null || order.getId() == null) {
            return;
        }
        appendHistory(
                order.getId(),
                null,
                order.getStatus() != null ? order.getStatus() : OrderStatuses.PENDING,
                "ORDER_PLACED",
                actor
        );
    }

    private void appendHistory(String orderId, String from, String to, OrderTransition transition, User actor) {
        appendHistory(orderId, from, to, transition.name(), actor);
    }

    private void appendHistory(String orderId, String from, String to, String transitionName, User actor) {
        OrderStatusHistoryEntry entry = new OrderStatusHistoryEntry();
        entry.setOrderId(orderId);
        entry.setFromStatus(from);
        entry.setToStatus(to);
        entry.setTransitionName(transitionName);
        if (actor != null) {
            entry.setActorId(actor.getId());
            entry.setActorName(actor.getName() != null ? actor.getName() : actor.getEmail());
        } else {
            entry.setActorName("System");
        }
        entry.setCreatedAt(LocalDateTime.now());
        storage.appendOrderStatusHistory(entry);
    }
}
