package com.supplysync.domain.pricing.strategy;

import com.supplysync.domain.pricing.OrderPricingContext;
import com.supplysync.domain.pricing.PricingResult;

/**
 * Interface for dynamic pricing strategies (Strategy Pattern).
 * Follows Open/Closed Principle (OCP) - easy to add new pricing rules.
 */
public interface PricingStrategy {
    /**
     * Applies this pricing strategy to the given context.
     * @param context the order context
     * @param currentResult the result from previously applied strategies in the pipeline
     * @return the new pricing result after applying this strategy
     */
    PricingResult apply(OrderPricingContext context, PricingResult currentResult);
}
