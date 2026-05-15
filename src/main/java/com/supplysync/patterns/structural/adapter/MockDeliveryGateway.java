package com.supplysync.patterns.structural.adapter;

import com.supplysync.models.Order;

public class MockDeliveryGateway implements ExternalDeliveryGateway {
    @Override
    public void push(Order order) {
        scheduleDelivery(order.getId(), order.getShippingAddress(), order.getCustomerName());
    }

    @Override
    public String scheduleDelivery(String orderId, String address, String customerName) {
        System.out.println("[MOCK DELIVERY] Order " + orderId + " to " + address);
        return "TRK-" + orderId.substring(0, Math.min(8, orderId.length())).toUpperCase();
    }

    @Override
    public String checkStatus(String trackingNumber) {
        return "IN_TRANSIT";
    }
}
