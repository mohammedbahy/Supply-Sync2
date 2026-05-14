package com.supplysync.patterns.behavioral.observer;

import com.supplysync.models.Order;

public interface OrderObserver {
    void onOrderChanged(Order order);
}
