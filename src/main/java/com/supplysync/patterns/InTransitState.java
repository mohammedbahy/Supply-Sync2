package com.supplysync.patterns;

import com.supplysync.models.Order;

public class InTransitState implements OrderState {
    @Override
    public void approve(Order order) {
        throw new IllegalStateException("Order already approved");
    }

    @Override
    public void ship(Order order) {
        throw new IllegalStateException("Order already in transit");
    }

    @Override
    public void deliver(Order order) {
        order.setState(new DeliveredState());
    }

    @Override
    public void cancel(Order order) {
        throw new IllegalStateException("Cannot cancel order in transit");
    }

    @Override
    public String getStatusName() {
        return "IN_TRANSIT";
    }
}
