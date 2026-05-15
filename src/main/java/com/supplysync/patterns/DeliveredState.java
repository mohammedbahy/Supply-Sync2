package com.supplysync.patterns;

import com.supplysync.models.Order;

public class DeliveredState implements OrderState {
    @Override
    public void approve(Order order) {
        throw new IllegalStateException("Order already delivered");
    }

    @Override
    public void ship(Order order) {
        throw new IllegalStateException("Order already delivered");
    }

    @Override
    public void deliver(Order order) {
        throw new IllegalStateException("Order already delivered");
    }

    @Override
    public void cancel(Order order) {
        throw new IllegalStateException("Cannot cancel delivered order");
    }

    @Override
    public String getStatusName() {
        return "DELIVERED";
    }
}
