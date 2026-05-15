package com.supplysync.domain.pricing.strategy;

import com.supplysync.domain.pricing.CartItemDto;
import com.supplysync.domain.pricing.OrderPricingContext;
import com.supplysync.domain.pricing.PricingResult;

import java.util.HashMap;
import java.util.Map;

/**
 * Applies a 5% discount if the total quantity of items exceeds a threshold (e.g., 50 items).
 */
public class WholesaleVolumeStrategy implements PricingStrategy {
    private static final int VOLUME_THRESHOLD = 50;
    private static final double DISCOUNT_RATE = 0.05;

    @Override
    public PricingResult apply(OrderPricingContext context, PricingResult currentResult) {
        if (currentResult == null) {
            return currentResult;
        }

        int totalQuantity = context.getItems().stream()
                .mapToInt(CartItemDto::getQuantity)
                .sum();

        if (totalQuantity > VOLUME_THRESHOLD) {
            double currentFinalPrice = currentResult.getFinalPrice();
            double discountAmount = currentFinalPrice * DISCOUNT_RATE;

            Map<String, Double> newDiscounts = new HashMap<>(currentResult.getDiscountsBreakdown());
            newDiscounts.put("Wholesale Volume Discount", discountAmount);

            return new PricingResult(
                    currentResult.getSubtotal(),
                    newDiscounts,
                    currentResult.getCommission()
            );
        }

        return currentResult;
    }
}
