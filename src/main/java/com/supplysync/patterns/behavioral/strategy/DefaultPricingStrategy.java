package com.supplysync.patterns.behavioral.strategy;

import com.supplysync.models.Order;

public class DefaultPricingStrategy implements PricingStrategy {
    @Override
    public double calculate(Order order) {
        return order.getProducts().stream()
                .mapToDouble(ProductPricing::unitPrice)
                .sum();
    }

    private static final class ProductPricing {
        private ProductPricing() {
        }

        private static double unitPrice(com.supplysync.models.Product product) {
            return product.getQuantity();
        }
    }
}
