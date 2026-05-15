package com.supplysync.patterns.behavioral.observer;

import com.supplysync.models.Order;

public interface OrderObserver {
    void onOrderUpdated(Order order);

    /** @deprecated use {@link #onOrderUpdated(Order)} */
    default void onOrderChanged(Order order) {
        onOrderUpdated(order);
    }
}
