package com.supplysync.domain.order.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Publishes domain events after successful workflow transitions.
 */
public final class OrderEventBus {
    private final List<OrderDomainListener> listeners = new CopyOnWriteArrayList<>();

    public void subscribe(OrderDomainListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void unsubscribe(OrderDomainListener listener) {
        listeners.remove(listener);
    }

    public void publish(OrderStatusChangedEvent event) {
        for (OrderDomainListener listener : listeners) {
            listener.onOrderStatusChanged(event);
        }
    }
}
