package com.supplysync.patterns;

import com.supplysync.models.Order;

public class CancelledState implements OrderState {
    @Override
    public void approve(Order order) {
        throw new IllegalStateException("Cannot approve cancelled order");
    }

    @Override
    public void ship(Order order) {
        throw new IllegalStateException("Cannot ship cancelled order");
    }

    @Override
    public void deliver(Order order) {
        throw new IllegalStateException("Cannot deliver cancelled order");
    }

    @Override
    public void cancel(Order order) {
        throw new IllegalStateException("Order already cancelled");
    }

    @Override
    public String getStatusName() {
        return "CANCELLED";
    }
}
