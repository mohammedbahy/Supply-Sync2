package com.supplysync.patterns;

import com.supplysync.models.Order;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class PendingState implements OrderState {
    @Override
    public void approve(Order order) {
        order.setState(new InTransitState());
    }

    @Override
    public void cancel(Order order) {
        long hours = ChronoUnit.HOURS.between(
                order.getEffectivePlacedAt(),
                LocalDateTime.now()
        );
        if (hours < 24) {
            order.setState(new CancelledState());
        } else {
            throw new IllegalStateException("Cannot cancel order older than 24 hours");
        }
    }

    @Override
    public void ship(Order order) {
        throw new IllegalStateException("Must approve order before shipping");
    }

    @Override
    public void deliver(Order order) {
        throw new IllegalStateException("Must ship order before delivery");
    }

    @Override
    public String getStatusName() {
        return "PENDING";
    }
}
