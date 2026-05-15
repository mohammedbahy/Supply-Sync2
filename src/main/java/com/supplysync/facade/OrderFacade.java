package com.supplysync.facade;

import com.supplysync.domain.order.OrderTransition;
import com.supplysync.domain.order.event.OrderEventBus;
import com.supplysync.models.MarketerCancelResult;
import com.supplysync.models.Order;
import com.supplysync.models.OrderStatuses;
import com.supplysync.models.User;
import com.supplysync.services.inventory.InventoryService;
import com.supplysync.services.order.OrderWorkflowService;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Thin facade: delegates workflow to {@link OrderWorkflowService}.
 */
public final class OrderFacade {
    private final OrderWorkflowService workflowService;
    private final AuthFacade auth;
    private final CatalogFacade catalog;
    private final DraftFacade drafts;
    private final InventoryService inventoryService;

    public OrderFacade(
            OrderWorkflowService workflowService,
            OrderEventBus eventBus,
            AuthFacade auth,
            CatalogFacade catalog,
            DraftFacade drafts,
            InventoryService inventoryService
    ) {
        this.workflowService = workflowService;
        this.auth = auth;
        this.catalog = catalog;
        this.drafts = drafts;
        this.inventoryService = inventoryService;
    }

    public Order submitOrder(Order order) {
        User actor = requireCurrentUser();
        Order submitted = workflowService.submitOrder(order, actor);
        catalog.clearCart();
        drafts.discardDraftForCurrentUser();
        return submitted;
    }

    public Order executeTransition(String orderId, OrderTransition transition) {
        return workflowService.executeTransition(orderId, transition, requireCurrentUser());
    }

    public Set<OrderTransition> getAllowedTransitions(Order order) {
        User actor = auth.getCurrentUser();
        if (order == null || actor == null) {
            return java.util.Collections.emptySet();
        }
        return workflowService.getAllowedTransitions(order, actor);
    }

    public List<Order> getAllOrders() {
        return workflowService.getAllOrders();
    }

    public java.util.Optional<Order> findOrderById(String orderId) {
        return workflowService.findOrder(orderId);
    }

    public List<Order> getMyOrders() {
        User u = auth.getCurrentUser();
        if (u == null) {
            return java.util.Collections.emptyList();
        }
        return getAllOrders().stream()
                .filter(o -> orderBelongsToMarketer(o, u))
                .sorted(Comparator.comparing(Order::getEffectivePlacedAt).reversed())
                .collect(Collectors.toList());
    }

    public MarketerCancelResult cancelOrderAsMarketer(String orderId) {
        User u = auth.getCurrentUser();
        if (u == null) {
            return MarketerCancelResult.NOT_OWNER;
        }
        Order o = workflowService.findOrder(orderId).orElse(null);
        if (o == null) {
            return MarketerCancelResult.NOT_FOUND;
        }
        if (!orderBelongsToMarketer(o, u)) {
            return MarketerCancelResult.NOT_OWNER;
        }
        String status = o.getStatus();
        if (!OrderStatuses.AWAITING_APPROVAL.equals(status) && !OrderStatuses.PENDING.equals(status)) {
            return MarketerCancelResult.INVALID_STATUS;
        }
        try {
            workflowService.executeTransition(orderId, OrderTransition.CANCEL, u);
            return MarketerCancelResult.SUCCESS;
        } catch (IllegalStateException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("24 hours")) {
                return MarketerCancelResult.TOO_LATE;
            }
            return MarketerCancelResult.INVALID_STATUS;
        } catch (SecurityException ex) {
            return MarketerCancelResult.NOT_OWNER;
        }
    }

    public void restoreOrderInventory(Order order) {
        inventoryService.restoreInventory(order);
    }

    private User requireCurrentUser() {
        User u = auth.getCurrentUser();
        if (u == null) {
            throw new SecurityException("User must be signed in");
        }
        return u;
    }

    private static boolean orderBelongsToMarketer(Order o, User u) {
        if (o.getMarketer() != null && u.getId() != null && u.getId().equals(o.getMarketer().getId())) {
            return true;
        }
        return o.getMarketer() == null
                && o.getCustomerName() != null
                && u.getName() != null
                && o.getCustomerName().trim().equalsIgnoreCase(u.getName().trim());
    }
}
