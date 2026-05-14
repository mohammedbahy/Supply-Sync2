package com.supplysync.patterns.structural.adapter;

import com.supplysync.models.Order;

public interface ExternalDeliveryGateway {
    void push(Order order);
}
