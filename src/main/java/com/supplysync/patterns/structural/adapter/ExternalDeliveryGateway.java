package com.supplysync.patterns.structural.adapter;

import com.supplysync.models.Order;

public interface ExternalDeliveryGateway {
    void push(Order order);

    String scheduleDelivery(String orderId, String address, String customerName);

    String checkStatus(String trackingNumber);
}
