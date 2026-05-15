package com.supplysync.domain.order;

import com.supplysync.models.Order;
import com.supplysync.models.OrderStatuses;
import com.supplysync.models.User;
import com.supplysync.services.inventory.InventoryService;

/**
 * Reserves stock on approval; restores on cancellation from stocked states.
 */
public final class InventoryTransitionSideEffect implements OrderTransitionSideEffect {
    private final InventoryService inventoryService;

    public InventoryTransitionSideEffect(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Override
    public void beforeTransition(Order order,
                                 OrderTransition transition,
                                 String fromStatus,
                                 String toStatus,
                                 User actor) {
        // no-op before
    }

    @Override
    public void afterTransition(Order order,
                                OrderTransition transition,
                                String fromStatus,
                                String toStatus,
                                User actor) {
        if (transition == OrderTransition.APPROVE
                && OrderStatuses.APPROVED.equals(toStatus)) {
            inventoryService.reserveInventory(order);
            order.markInventoryReserved(true);
            return;
        }
        if (transition == OrderTransition.CANCEL && order.isInventoryReserved()) {
            inventoryService.restoreInventory(order);
            order.markInventoryReserved(false);
            return;
        }
        if (transition == OrderTransition.CANCEL
                && (OrderStatuses.APPROVED.equals(fromStatus)
                || OrderStatuses.PARTIALLY_SHIPPED.equals(fromStatus)
                || OrderStatuses.ON_HOLD.equals(fromStatus))) {
            inventoryService.restoreInventory(order);
            order.markInventoryReserved(false);
        }
    }
}
