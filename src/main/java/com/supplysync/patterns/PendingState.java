package com.supplysync.patterns;

import com.supplysync.models.Order;

/**
 * @deprecated Legacy pattern stub — use {@link com.supplysync.domain.order.OrderStateMachine} via
 * {@link com.supplysync.services.order.OrderWorkflowService}.
 */
@Deprecated
public class PendingState implements OrderState {
    private static UnsupportedOperationException disabled() {
        return new UnsupportedOperationException("Use OrderWorkflowService for status changes");
    }

    @Override
    public void approve(Order order) {
        throw disabled();
    }

    @Override
    public void cancel(Order order) {
        throw disabled();
    }

    @Override
    public void ship(Order order) {
        throw disabled();
    }

    @Override
    public void deliver(Order order) {
        throw disabled();
    }

    @Override
    public String getStatusName() {
        return "PENDING";
    }
}
