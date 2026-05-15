package com.supplysync.services.order;

import com.supplysync.models.Order;

public interface OrderService {
    void createOrder(Order order);

    java.util.List<Order> findAllOrders();
}
