package com.supplysync.patterns.behavioral.strategy;

import com.supplysync.models.Order;

public class DefaultPricingStrategy extends com.supplysync.patterns.StandardPricingStrategy implements PricingStrategy {
    @Override
    public double calculate(Order order) {
        return calculateCommission(order);
    }
}
