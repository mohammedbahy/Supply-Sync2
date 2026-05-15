package com.supplysync.domain.order.event;

@FunctionalInterface
public interface OrderDomainListener {
    void onOrderStatusChanged(OrderStatusChangedEvent event);
}
