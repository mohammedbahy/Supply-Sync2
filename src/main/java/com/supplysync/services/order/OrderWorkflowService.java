package com.supplysync.services.order;

import com.supplysync.domain.order.OrderStateMachine;
import com.supplysync.domain.order.OrderStatusHydrator;
import com.supplysync.domain.order.OrderTransition;
import com.supplysync.domain.order.OrderTransitionGuard;
import com.supplysync.domain.order.OrderTransitionSideEffect;
import com.supplysync.domain.order.event.OrderEventBus;
import com.supplysync.domain.order.event.OrderStatusChangedEvent;
import com.supplysync.models.Order;
import com.supplysync.models.OrderStatuses;
import com.supplysync.models.User;
import com.supplysync.patterns.behavioral.strategy.PricingStrategy;
import com.supplysync.repository.OrderRepository;
import com.supplysync.repository.OrderStatusHistoryRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Orchestrates order workflow — all status changes go through here.
 */
public final class OrderWorkflowService {
    private final OrderRepository orders;
    private final OrderStateMachine stateMachine;
    private final OrderTransitionGuard guard;
    private final List<OrderTransitionSideEffect> sideEffects;
    private final OrderEventBus eventBus;
    private final PricingStrategy pricingStrategy;

    public OrderWorkflowService(OrderRepository orders,
                                OrderStateMachine stateMachine,
                                OrderTransitionGuard guard,
                                List<OrderTransitionSideEffect> sideEffects,
                                OrderEventBus eventBus,
                                PricingStrategy pricingStrategy) {
        this.orders = orders;
        this.stateMachine = stateMachine;
        this.guard = guard;
        this.sideEffects = sideEffects;
        this.eventBus = eventBus;
        this.pricingStrategy = pricingStrategy;
    }

    public Order submitOrder(Order order, User actor) {
        guard.validateSubmit(order, actor);
        double commission = pricingStrategy.calculateCommission(order);
        order.setCommission(commission);
        OrderStatusHydrator.hydrate(order, OrderStatuses.AWAITING_APPROVAL);
        orders.saveOrder(order);
        publishTransition(order, null, order.getStatus(), null, actor);
        return order;
    }

    public Order executeTransition(String orderId, OrderTransition transition, User actor) {
        Order order = orders.findOrderById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        stateMachine.repairHoldState(order);

        if (!stateMachine.isTransitionPermitted(order, actor, transition)) {
            throw new IllegalStateException(
                    "Transition " + transition + " is not allowed for role " + actor.getRole()
                            + " in status " + order.getStatus());
        }

        guard.validate(order, transition, actor);
        String fromStatus = order.getStatus();
        String toStatus = stateMachine.resolveTargetStatus(order, transition, actor);

        for (OrderTransitionSideEffect effect : sideEffects) {
            effect.beforeTransition(order, transition, fromStatus, toStatus, actor);
        }

        stateMachine.applyTransition(order, transition, actor);
        orders.saveOrder(order);

        for (OrderTransitionSideEffect effect : sideEffects) {
            effect.afterTransition(order, transition, fromStatus, toStatus, actor);
        }

        publishTransition(order, fromStatus, toStatus, transition, actor);
        return order;
    }

    public Set<OrderTransition> getAllowedTransitions(Order order, User actor) {
        return stateMachine.getAllowedTransitions(order, actor);
    }

    public List<Order> getAllOrders() {
        return orders.findAllOrders();
    }

    public Optional<Order> findOrder(String orderId) {
        return orders.findOrderById(orderId);
    }

    private void publishTransition(Order order,
                                   String fromStatus,
                                   String toStatus,
                                   OrderTransition transition,
                                   User actor) {
        String from = fromStatus != null ? fromStatus : order.getStatus();
        String to = toStatus != null ? toStatus : order.getStatus();
        eventBus.publish(new OrderStatusChangedEvent(order, from, to, transition, actor));
    }

    public static OrderWorkflowService createDefault(
            OrderRepository orders,
            com.supplysync.services.inventory.InventoryService inventoryService,
            com.supplysync.services.delivery.DeliveryService deliveryService,
            com.supplysync.services.notification.NotificationService notificationService,
            OrderStatusHistoryRepository historyRepository,
            OrderEventBus eventBus,
            PricingStrategy pricingStrategy) {
        OrderStateMachine machine = new OrderStateMachine();
        OrderTransitionGuard guard = new OrderTransitionGuard();
        List<OrderTransitionSideEffect> effects = Arrays.asList(
                new com.supplysync.domain.order.InventoryTransitionSideEffect(inventoryService),
                new com.supplysync.domain.order.DeliveryTransitionSideEffect(deliveryService),
                new com.supplysync.domain.order.NotificationTransitionSideEffect(notificationService),
                new com.supplysync.domain.order.OrderStatusHistorySideEffect(historyRepository)
        );
        return new OrderWorkflowService(orders, machine, guard, effects, eventBus, pricingStrategy);
    }
}
