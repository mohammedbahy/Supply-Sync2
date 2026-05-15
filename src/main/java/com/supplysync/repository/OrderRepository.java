package com.supplysync.repository;

import com.supplysync.models.Order;

import java.util.List;
import java.util.Optional;

/** Order persistence (ISP). */
public interface OrderRepository {
    void saveOrder(Order order);

    Optional<Order> findOrderById(String id);

    List<Order> findAllOrders();
}
