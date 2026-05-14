package com.supplysync.patterns.behavioral.strategy;

import com.supplysync.models.Order;

public interface PricingStrategy {
    double calculate(Order order);
}
