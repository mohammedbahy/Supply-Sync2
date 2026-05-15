package com.supplysync.domain.order;

import com.supplysync.models.Order;
import com.supplysync.models.User;
import com.supplysync.repository.OrderStatusHistoryRepository;

public final class OrderStatusHistorySideEffect implements OrderTransitionSideEffect {
    private final OrderStatusHistoryRepository historyRepository;

    public OrderStatusHistorySideEffect(OrderStatusHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @Override
    public void beforeTransition(Order order,
                                 OrderTransition transition,
                                 String fromStatus,
                                 String toStatus,
                                 User actor) {
    }

    @Override
    public void afterTransition(Order order,
                                OrderTransition transition,
                                String fromStatus,
                                String toStatus,
                                User actor) {
        String actorId = actor != null ? actor.getId() : null;
        historyRepository.appendStatusHistory(
                order.getId(),
                fromStatus,
                toStatus,
                transition.name(),
                actorId
        );
    }
}
