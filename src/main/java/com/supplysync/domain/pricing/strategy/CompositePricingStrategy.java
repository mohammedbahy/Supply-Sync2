package com.supplysync.domain.pricing.strategy;

import com.supplysync.domain.pricing.OrderPricingContext;
import com.supplysync.domain.pricing.PricingResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Composite pattern implementation for pricing strategies.
 * Evaluates strategies in the order they were added, piping the result.
 */
public class CompositePricingStrategy implements PricingStrategy {
    private final List<PricingStrategy> strategies = new ArrayList<>();

    public void addStrategy(PricingStrategy strategy) {
        if (strategy != null) {
            strategies.add(strategy);
        }
    }

    @Override
    public PricingResult apply(OrderPricingContext context, PricingResult currentResult) {
        PricingResult result = currentResult;
        for (PricingStrategy strategy : strategies) {
            result = strategy.apply(context, result);
        }
        return result;
    }
}
