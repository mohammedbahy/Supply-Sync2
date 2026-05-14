package com.supplysync.services.order;

import com.supplysync.models.Order;
import com.supplysync.repository.Storage;
import java.util.List;

public class DefaultOrderService implements OrderService {
    private final Storage storage;

    public DefaultOrderService(Storage storage) {
        this.storage = storage;
    }

    @Override
    public void createOrder(Order order) {
        System.out.println("Creating order: " + order.getId());
        storage.saveOrder(order);
    }

    public List<Order> getAllOrders() {
        return storage.findAllOrders();
    }
}
