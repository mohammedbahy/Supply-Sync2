package com.supplysync.facade;

import com.supplysync.dashboard.DashboardDataPort;
import com.supplysync.models.AdminDashboardStats;
import com.supplysync.models.Marketer;
import com.supplysync.models.Product;
import com.supplysync.repository.MarketerRepository;
import com.supplysync.services.order.OrderService;

import java.util.ArrayList;
import java.util.List;

/**
 * Dashboard statistics and admin marketer management (SRP).
 */
public final class DashboardFacade implements DashboardDataPort {
    private final OrderService orderService;
    private final CatalogFacade catalog;
    private final MarketerRepository marketers;

    public DashboardFacade(OrderService orderService, CatalogFacade catalog, MarketerRepository marketers) {
        this.orderService = orderService;
        this.catalog = catalog;
        this.marketers = marketers;
    }

    @Override
    public AdminDashboardStats loadStatistics() {
        return AdminDashboardStats.from(catalog.getCatalog(), orderService.findAllOrders());
    }

    @Override
    public List<Product> loadProductCatalogSnapshot() {
        return new ArrayList<>(catalog.getCatalog());
    }

    public List<Marketer> getAllMarketers() {
        return marketers.findAllMarketers();
    }

    public void addMarketer(Marketer marketer) {
        marketers.saveMarketer(marketer);
    }
}
