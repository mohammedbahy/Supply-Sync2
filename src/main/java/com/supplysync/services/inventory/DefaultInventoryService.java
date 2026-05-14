package com.supplysync.services.inventory;

import com.supplysync.models.Order;

import com.supplysync.repository.Storage;
import com.supplysync.models.Product;
import java.util.List;

public class DefaultInventoryService implements InventoryService {
    private final Storage storage;

    public DefaultInventoryService(Storage storage) {
        this.storage = storage;
    }

    @Override
    public void reserveInventory(Order order) {
        System.out.println("Reserving inventory for order: " + order.getId());
        for (Product orderProduct : order.getProducts()) {
            storage.findAllProducts().stream()
                .filter(p -> p.getId().equals(orderProduct.getId()))
                .findFirst()
                .ifPresent(p -> {
                    p.setQuantity(Math.max(0, p.getQuantity() - 1));
                    storage.saveProduct(p);
                });
        }
    }

    @Override
    public void restoreInventory(Order order) {
        for (Product orderProduct : order.getProducts()) {
            storage.findAllProducts().stream()
                    .filter(p -> p.getId().equals(orderProduct.getId()))
                    .findFirst()
                    .ifPresent(p -> {
                        p.setQuantity(p.getQuantity() + 1);
                        storage.saveProduct(p);
                    });
        }
    }

    @Override
    public List<Product> getAllProducts() {
        return storage.findAllProducts();
    }
}
