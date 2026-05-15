package com.supplysync.domain.pricing.strategy;

import com.supplysync.domain.pricing.CartItemDto;
import com.supplysync.domain.pricing.OrderPricingContext;
import com.supplysync.domain.pricing.PricingResult;

import java.util.HashMap;

/**
 * Calculates the raw subtotal before any discounts or commissions are applied.
 */
public class BasePricingStrategy implements PricingStrategy {
    @Override
    public PricingResult apply(OrderPricingContext context, PricingResult currentResult) {
        double subtotal = 0.0;
        for (CartItemDto item : context.getItems()) {
            subtotal += item.getUnitPrice() * item.getQuantity();
        }

        // Return a fresh result with the base subtotal
        return new PricingResult(
                subtotal,
                currentResult != null ? currentResult.getDiscountsBreakdown() : new HashMap<>(),
                currentResult != null ? currentResult.getCommission() : 0.0
        );
    }
}
