package com.supplysync.patterns.structural.adapter;

import com.supplysync.models.Order;
import com.supplysync.services.delivery.DeliveryService;

public class DeliveryAdapter implements DeliveryService {
    private final ExternalDeliveryGateway externalDeliveryGateway;

    public DeliveryAdapter(ExternalDeliveryGateway externalDeliveryGateway) {
        this.externalDeliveryGateway = externalDeliveryGateway;
    }

    @Override
    public void schedule(Order order) {
        String tracking = externalDeliveryGateway.scheduleDelivery(
                order.getId(),
                order.getShippingAddress(),
                order.getCustomerName()
        );
        order.setTrackingNumber(tracking);
        externalDeliveryGateway.push(order);
    }
}
