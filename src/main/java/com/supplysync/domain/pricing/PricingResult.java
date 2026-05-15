package com.supplysync.domain.pricing;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Immutable snapshot of a pricing calculation.
 */
public class PricingResult {
    private final double subtotal;
    private final Map<String, Double> discountsBreakdown;
    private final double commission;
    private final double finalPrice;

    public PricingResult(double subtotal, Map<String, Double> discountsBreakdown, double commission) {
        this.subtotal = subtotal;
        this.discountsBreakdown = Collections.unmodifiableMap(new HashMap<>(discountsBreakdown));
        this.commission = commission;

        double totalDiscounts = discountsBreakdown.values().stream().mapToDouble(Double::doubleValue).sum();
        this.finalPrice = Math.max(0, subtotal - totalDiscounts);
    }

    public static PricingResult empty() {
        return new PricingResult(0.0, Collections.emptyMap(), 0.0);
    }

    public double getSubtotal() {
        return subtotal;
    }

    public Map<String, Double> getDiscountsBreakdown() {
        return discountsBreakdown;
    }

    public double getTotalDiscounts() {
        return discountsBreakdown.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    public double getCommission() {
        return commission;
    }

    public double getFinalPrice() {
        return finalPrice;
    }
}
