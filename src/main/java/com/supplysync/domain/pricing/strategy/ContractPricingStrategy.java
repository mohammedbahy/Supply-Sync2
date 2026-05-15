package com.supplysync.domain.pricing.strategy;

import com.supplysync.domain.pricing.OrderPricingContext;
import com.supplysync.domain.pricing.PricingResult;
import com.supplysync.models.User;

import java.util.HashMap;
import java.util.Map;

/**
 * Applies a flat 15% discount for VIP contract customers.
 */
public class ContractPricingStrategy implements PricingStrategy {
    private static final double VIP_DISCOUNT_RATE = 0.15;

    @Override
    public PricingResult apply(OrderPricingContext context, PricingResult currentResult) {
        if (currentResult == null) return currentResult;

        if (context.getCustomer().isPresent()) {
            User u = context.getCustomer().get();
            // If the user has a VIP role or a special contract flag
            if ("VIP_CUSTOMER".equalsIgnoreCase(u.getRole())) {
                double currentFinalPrice = currentResult.getFinalPrice();
                double discountAmount = currentFinalPrice * VIP_DISCOUNT_RATE;

                Map<String, Double> newDiscounts = new HashMap<>(currentResult.getDiscountsBreakdown());
                newDiscounts.put("VIP Contract Discount", discountAmount);

                return new PricingResult(
                        currentResult.getSubtotal(),
                        newDiscounts,
                        currentResult.getCommission()
                );
            }
        }

        return currentResult;
    }
}
