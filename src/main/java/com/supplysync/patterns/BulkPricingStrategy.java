package com.supplysync.patterns;

import com.supplysync.models.Order;
import com.supplysync.models.Product;
import com.supplysync.patterns.behavioral.strategy.PricingStrategy;

import java.util.List;

public class BulkPricingStrategy implements PricingStrategy {
    @Override
    public double calculateCommission(Order order) {
        return order.getTotalAmount() * 0.03;
    }

    @Override
    public double calculateTotal(List<Product> products) {
        double total = products.stream()
                .mapToDouble(Product::getPrice)
                .sum();
        return total * 0.90;
    }
}
