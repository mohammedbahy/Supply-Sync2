package com.supplysync.models;

import java.util.Arrays;
import java.util.List;

/**
 * Aggregates admin dashboard metrics from live product and order lists (e.g. from SQLite).
 */
public final class AdminDashboardStats {
    public final int skuCount;
    public final int totalStockUnits;
    public final int orderCount;
    public final long pendingOrders;
    public final long approvedOrders;
    public final long deliveredOrders;
    public final long shippedOrders;
    public final long cancelledOrders;
    public final double totalRevenue;
    public final double activePipelineFraction;
    public final double stockAvailabilityFraction;
    public final double cancelledFraction;

    private final int[] barRawValues;

    public AdminDashboardStats(
            int skuCount,
            int totalStockUnits,
            int orderCount,
            long pendingOrders,
            long approvedOrders,
            long deliveredOrders,
            long shippedOrders,
            long cancelledOrders,
            double totalRevenue,
            double activePipelineFraction,
            double stockAvailabilityFraction,
            double cancelledFraction,
            int[] barRawValues) {
        this.skuCount = skuCount;
        this.totalStockUnits = totalStockUnits;
        this.orderCount = orderCount;
        this.pendingOrders = pendingOrders;
        this.approvedOrders = approvedOrders;
        this.deliveredOrders = deliveredOrders;
        this.shippedOrders = shippedOrders;
        this.cancelledOrders = cancelledOrders;
        this.totalRevenue = totalRevenue;
        this.activePipelineFraction = activePipelineFraction;
        this.stockAvailabilityFraction = stockAvailabilityFraction;
        this.cancelledFraction = cancelledFraction;
        this.barRawValues = barRawValues;
    }

    public static AdminDashboardStats from(List<Product> products, List<Order> orders) {
        int sku = products.size();
        int stockSum = products.stream().mapToInt(Product::getQuantity).sum();

        long pending = orders.stream().filter(o -> {
            String s = o.getStatus();
            return OrderStatuses.AWAITING_APPROVAL.equals(s)
                    || OrderStatuses.PENDING.equals(s)
                    || OrderStatuses.ON_HOLD.equals(s);
        }).count();
        long inTransit = orders.stream().filter(o -> {
            String s = o.getStatus();
            return OrderStatuses.IN_TRANSIT.equals(s)
                    || OrderStatuses.PARTIALLY_SHIPPED.equals(s)
                    || "SHIPPED".equals(s);
        }).count();
        long delivered = orders.stream().filter(o -> OrderStatuses.DELIVERED.equals(o.getStatus())).count();
        long shipped = orders.stream().filter(o -> "SHIPPED".equals(o.getStatus())).count();
        long cancelled = orders.stream().filter(o -> OrderStatuses.CANCELLED.equals(o.getStatus())).count();

        int n = orders.size();
        double revenue = orders.stream()
                .filter(o -> !OrderStatuses.CANCELLED.equals(o.getStatus()))
                .mapToDouble(Order::getTotalAmount)
                .sum();

        long activePipeline = inTransit + delivered;
        double pipelineFrac = n == 0 ? 0 : Math.min(1.0, (double) activePipeline / n);

        double stockAvail = sku == 0 ? 1.0
                : products.stream().filter(p -> p.getQuantity() > 0).count() / (double) sku;

        double cancelFrac = n == 0 ? 0 : Math.min(1.0, (double) cancelled / n);

        long lowStock = products.stream().filter(p -> p.getQuantity() > 0 && p.getQuantity() < 100).count();
        long outStock = products.stream().filter(p -> p.getQuantity() == 0).count();

        int[] raw = new int[]{
                (int) pending,
                (int) inTransit,
                (int) delivered,
                (int) cancelled,
                sku,
                (int) Math.min(Integer.MAX_VALUE / 4, lowStock + outStock)
        };

        return new AdminDashboardStats(
                sku,
                stockSum,
                n,
                pending,
                inTransit,
                delivered,
                shipped,
                cancelled,
                revenue,
                pipelineFrac,
                stockAvail,
                cancelFrac,
                raw);
    }

    /** Bar heights in pixels for the overview chart (6 bars), scaled from {@link #barRawValues}. */
    public int[] chartBarHeights() {
        int max = Arrays.stream(barRawValues).max().orElse(0);
        if (max <= 0) {
            return new int[]{40, 40, 40, 40, 40, 40};
        }
        int[] h = new int[6];
        for (int i = 0; i < 6; i++) {
            h[i] = 40 + (int) Math.round(200.0 * barRawValues[i] / max);
        }
        return h;
    }
}
