package com.supplysync.domain.pricing.strategy;

import com.supplysync.domain.pricing.OrderPricingContext;
import com.supplysync.domain.pricing.PricingResult;

import java.time.Month;
import java.util.HashMap;
import java.util.Map;

/**
 * Applies a seasonal discount (e.g., 10% off during Summer - July/August).
 */
public class SeasonalPromotionStrategy implements PricingStrategy {
    private static final double SUMMER_DISCOUNT_RATE = 0.10;

    @Override
    public PricingResult apply(OrderPricingContext context, PricingResult currentResult) {
        if (currentResult == null) return currentResult;

        Month currentMonth = context.getDate().getMonth();
        
        // Example: Summer discount in July and August
        if (currentMonth == Month.JULY || currentMonth == Month.AUGUST) {
            double currentFinalPrice = currentResult.getFinalPrice();
            double discountAmount = currentFinalPrice * SUMMER_DISCOUNT_RATE;

            Map<String, Double> newDiscounts = new HashMap<>(currentResult.getDiscountsBreakdown());
            newDiscounts.put("Summer Seasonal Promo", discountAmount);

            return new PricingResult(
                    currentResult.getSubtotal(),
                    newDiscounts,
                    currentResult.getCommission()
            );
        }

        return currentResult;
    }
}
