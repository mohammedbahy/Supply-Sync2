package com.supplysync.domain.pricing.strategy;

import com.supplysync.domain.pricing.OrderPricingContext;
import com.supplysync.domain.pricing.PricingResult;
import com.supplysync.models.User;

import java.util.HashMap;

/**
 * Calculates marketer commission based on their tier.
 * e.g., 5% base commission, VIP gets 7%.
 */
public class MarketerTierCommissionStrategy implements PricingStrategy {
    private static final double BASE_COMMISSION_RATE = 0.05;
    private static final double VIP_COMMISSION_RATE = 0.07;

    @Override
    public PricingResult apply(OrderPricingContext context, PricingResult currentResult) {
        if (currentResult == null || context.getMarketer().isEmpty()) {
            return currentResult;
        }

        // Base the commission on the subtotal (not final price, so marketer isn't penalized by discounts)
        double subtotal = currentResult.getSubtotal();
        
        // Let's assume User has a way to check if they are a VIP marketer.
        // For simplicity, we can check if they have a specific role or metadata.
        // For now, if the marketer is the customer and has a specific role, we use VIP rate.
        double rate = BASE_COMMISSION_RATE;
        if (context.getCustomer().isPresent()) {
            User u = context.getCustomer().get();
            if ("VIP_MARKETER".equalsIgnoreCase(u.getRole())) {
                rate = VIP_COMMISSION_RATE;
            }
        }

        double commission = subtotal * rate;

        return new PricingResult(
                currentResult.getSubtotal(),
                new HashMap<>(currentResult.getDiscountsBreakdown()),
                commission
        );
    }
}
