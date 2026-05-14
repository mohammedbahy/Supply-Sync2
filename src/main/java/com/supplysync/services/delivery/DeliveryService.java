package com.supplysync.services.delivery;

import com.supplysync.models.Order;

public interface DeliveryService {
    void schedule(Order order);
}
