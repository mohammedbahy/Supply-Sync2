package com.supplysync.patterns;

import com.supplysync.models.Order;

/** @deprecated Use {@link com.supplysync.services.order.OrderWorkflowService}. */
@Deprecated
public class InTransitState implements OrderState {
    @Override
    public void approve(Order order) {
        throw new UnsupportedOperationException("Use OrderWorkflowService");
    }

    @Override
    public void ship(Order order) {
        throw new UnsupportedOperationException("Use OrderWorkflowService");
    }

    @Override
    public void deliver(Order order) {
        throw new UnsupportedOperationException("Use OrderWorkflowService");
    }

    @Override
    public void cancel(Order order) {
        throw new UnsupportedOperationException("Use OrderWorkflowService");
    }

    @Override
    public String getStatusName() {
        return "IN_TRANSIT";
    }
}
