package com.supplysync.dashboard;

import com.supplysync.facade.OrderFacade;
import com.supplysync.models.AdminDashboardStats;
import com.supplysync.models.Product;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapts {@link OrderFacade} to {@link DashboardDataPort} (Adapter pattern / DIP).
 */
public final class OrderFacadeDashboardDataAdapter implements DashboardDataPort {
    private final OrderFacade facade;

    public OrderFacadeDashboardDataAdapter(OrderFacade facade) {
        this.facade = facade;
    }

    @Override
    public AdminDashboardStats loadStatistics() {
        return facade.getAdminDashboardStats();
    }

    @Override
    public List<Product> loadProductCatalogSnapshot() {
        return new ArrayList<>(facade.getCatalog());
    }
}
