package com.supplysync.dashboard;

import com.supplysync.models.AdminDashboardStats;
import com.supplysync.models.Product;

import java.util.List;

/**
 * Abstraction for dashboard metrics (DIP): UI depends on this port, not on {@code OrderFacade} directly.
 */
public interface DashboardDataPort {
    AdminDashboardStats loadStatistics();

    List<Product> loadProductCatalogSnapshot();
}
