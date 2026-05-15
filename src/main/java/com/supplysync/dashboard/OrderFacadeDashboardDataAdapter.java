package com.supplysync.dashboard;

import com.supplysync.facade.DashboardFacade;
import com.supplysync.models.AdminDashboardStats;
import com.supplysync.models.Product;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapts {@link DashboardFacade} to {@link DashboardDataPort} (Adapter pattern / DIP).
 */
public final class OrderFacadeDashboardDataAdapter implements DashboardDataPort {
    private final DashboardFacade dashboard;

    public OrderFacadeDashboardDataAdapter(DashboardFacade dashboard) {
        this.dashboard = dashboard;
    }

    @Override
    public AdminDashboardStats loadStatistics() {
        return dashboard.loadStatistics();
    }

    @Override
    public List<Product> loadProductCatalogSnapshot() {
        return new ArrayList<>(dashboard.loadProductCatalogSnapshot());
    }
}
