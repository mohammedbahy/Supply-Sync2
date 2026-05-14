package com.supplysync.services.inventory;

import com.supplysync.models.Order;
import com.supplysync.models.Product;
import java.util.List;

public interface InventoryService {
    void reserveInventory(Order order);

    /** Add back one unit per line item (mirrors {@link #reserveInventory(Order)}). */
    void restoreInventory(Order order);

    List<Product> getAllProducts();
}
