package com.supplysync.patterns.behavioral.state;

/**
 * Legacy behavioral package entry; delegates to {@link com.supplysync.patterns.OrderState}.
 */
public interface OrderState extends com.supplysync.patterns.OrderState {
    /** @deprecated use {@link #getStatusName()} */
    default String name() {
        return getStatusName();
    }
}
