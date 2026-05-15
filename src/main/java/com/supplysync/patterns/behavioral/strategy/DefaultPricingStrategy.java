package com.supplysync.patterns.behavioral.strategy;

import com.supplysync.models.Order;
import com.supplysync.models.Product;
import java.util.List;

public class DefaultPricingStrategy implements PricingStrategy {
    @Override
    public double calculateCommission(Order order) {
        return order.getTotalAmount() * 0.05;
    }

    @Override
    public double calculateTotal(List<Product> products) {
        return products.stream()
                .mapToDouble(Product::getPrice)
                .sum();
    }
}
