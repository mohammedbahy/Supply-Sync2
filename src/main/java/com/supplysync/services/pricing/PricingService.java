package com.supplysync.services.pricing;

import com.supplysync.domain.pricing.OrderPricingContext;
import com.supplysync.domain.pricing.PricingResult;
import com.supplysync.domain.pricing.factory.PricingStrategyFactory;
import com.supplysync.domain.pricing.strategy.PricingStrategy;

/**
 * Service orchestrating the dynamic pricing engine.
 * Acts as the entry point for pricing previews and calculations.
 */
public class PricingService {
    private final PricingStrategyFactory strategyFactory;

    public PricingService(PricingStrategyFactory strategyFactory) {
        this.strategyFactory = strategyFactory;
    }

    /**
     * Previews the full pricing result for the given context.
     * @param context the order pricing context
     * @return the calculated pricing result snapshot
     */
    public PricingResult previewPricing(OrderPricingContext context) {
        if (context == null || context.getItems().isEmpty()) {
            return PricingResult.empty();
        }

        PricingStrategy strategy = strategyFactory.buildStrategy(context);
        return strategy.apply(context, null);
    }
}
