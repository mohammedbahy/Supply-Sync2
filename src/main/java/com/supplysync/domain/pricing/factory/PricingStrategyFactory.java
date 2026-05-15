package com.supplysync.domain.pricing.factory;

import com.supplysync.domain.pricing.OrderPricingContext;
import com.supplysync.domain.pricing.strategy.*;

/**
 * Factory for creating the correct pricing strategy based on context.
 * For now, this statically decides which rules apply. In the future,
 * it could fetch active rules from a PricingRuleRepository.
 */
public class PricingStrategyFactory {

    /**
     * Builds the composite strategy tailored for this order context.
     * @param context the order pricing context
     * @return a composite pricing strategy to evaluate
     */
    public PricingStrategy buildStrategy(OrderPricingContext context) {
        CompositePricingStrategy composite = new CompositePricingStrategy();

        // 1. Always start with base pricing
        composite.addStrategy(new BasePricingStrategy());

        // 2. Evaluate Seasonal Promotions (e.g. Summer discount)
        composite.addStrategy(new SeasonalPromotionStrategy());

        // 3. Evaluate VIP/Contract discounts
        composite.addStrategy(new ContractPricingStrategy());

        // 4. Evaluate Wholesale discounts
        composite.addStrategy(new WholesaleVolumeStrategy());

        // 5. Evaluate Marketer Commissions
        composite.addStrategy(new MarketerTierCommissionStrategy());

        return composite;
    }
}
