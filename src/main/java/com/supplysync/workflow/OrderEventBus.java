package com.supplysync.workflow;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Lightweight pub/sub for UI refresh after workflow transitions.
 */
public final class OrderEventBus {
    private static final OrderEventBus INSTANCE = new OrderEventBus();

    @FunctionalInterface
    public interface OrderChangeListener {
        void onOrderChanged(String orderId);
    }

    private final List<OrderChangeListener> listeners = new CopyOnWriteArrayList<>();

    private OrderEventBus() {
    }

    public static OrderEventBus getInstance() {
        return INSTANCE;
    }

    public void subscribe(OrderChangeListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void unsubscribe(OrderChangeListener listener) {
        listeners.remove(listener);
    }

    public void publish(String orderId) {
        for (OrderChangeListener listener : listeners) {
            listener.onOrderChanged(orderId);
        }
    }
}
