package com.supplysync.patterns;

import com.supplysync.models.Order;

/**
 * State pattern: order lifecycle transitions (approve, ship, deliver, cancel).
 */
public interface OrderState {
    void approve(Order order);

    void ship(Order order);

    void deliver(Order order);

    void cancel(Order order);

    String getStatusName();
}
