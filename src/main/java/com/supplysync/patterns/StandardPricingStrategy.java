package com.supplysync.patterns;

import com.supplysync.models.Order;
import com.supplysync.models.Product;
import com.supplysync.patterns.behavioral.strategy.PricingStrategy;

import java.util.List;

public class StandardPricingStrategy implements PricingStrategy {
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
