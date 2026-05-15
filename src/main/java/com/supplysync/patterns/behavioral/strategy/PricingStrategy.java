package com.supplysync.patterns.behavioral.strategy;

import com.supplysync.models.Order;
import com.supplysync.models.Product;

import java.util.List;

public interface PricingStrategy {
    double calculateCommission(Order order);

    double calculateTotal(List<Product> products);

    /** @deprecated use {@link #calculateCommission(Order)} */
    default double calculate(Order order) {
        return calculateCommission(order);
    }
}
