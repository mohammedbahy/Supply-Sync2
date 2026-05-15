package com.supplysync.domain.order;

import com.supplysync.models.Order;
import com.supplysync.models.User;

/**
 * Side effects executed around workflow transitions (inventory, delivery, audit).
 */
public interface OrderTransitionSideEffect {
    void beforeTransition(Order order,
                            OrderTransition transition,
                            String fromStatus,
                            String toStatus,
                            User actor);

    void afterTransition(Order order,
                         OrderTransition transition,
                         String fromStatus,
                         String toStatus,
                         User actor);
}
