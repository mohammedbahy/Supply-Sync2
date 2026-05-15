package com.supplysync.facade;

import com.supplysync.dashboard.DashboardDataPort;
import com.supplysync.patterns.creational.factory.ServiceFactory;
import com.supplysync.repository.Storage;
import com.supplysync.services.auth.AuthService;
import com.supplysync.services.delivery.DeliveryService;
import com.supplysync.services.inventory.InventoryService;
import com.supplysync.services.notification.NotificationService;
import com.supplysync.services.order.OrderService;

/**
 * Composition root: wires focused facades (DIP). Controllers depend on this instead of a god facade.
 */
public final class ApplicationContext {
    private final AuthFacade auth;
    private final CatalogFacade catalog;
    private final OrderFacade orders;
    private final DashboardFacade dashboard;
    private final NotificationFacade notifications;
    private final DraftFacade drafts;

    public ApplicationContext(
            AuthFacade auth,
            CatalogFacade catalog,
            OrderFacade orders,
            DashboardFacade dashboard,
            NotificationFacade notifications,
            DraftFacade drafts
    ) {
        this.auth = auth;
        this.catalog = catalog;
        this.orders = orders;
        this.dashboard = dashboard;
        this.notifications = notifications;
        this.drafts = drafts;
    }

    public static ApplicationContext createDefault() {
        Storage storage = ServiceFactory.getStorage();
        AuthService authService = ServiceFactory.createAuthService();
        OrderService orderService = ServiceFactory.createOrderService();
        InventoryService inventoryService = ServiceFactory.createInventoryService();
        DeliveryService deliveryService = ServiceFactory.createDeliveryService();
        NotificationService notificationService = ServiceFactory.createNotificationService();

        AuthFacade auth = new AuthFacade(authService, storage);
        CatalogFacade catalog = new CatalogFacade(inventoryService, storage);
        DraftFacade drafts = new DraftFacade(storage, auth, catalog);
        OrderFacade orders = new OrderFacade(
                orderService,
                inventoryService,
                deliveryService,
                notificationService,
                storage,
                auth,
                catalog,
                drafts
        );
        DashboardFacade dashboard = new DashboardFacade(orderService, catalog, storage);
        NotificationFacade notifications = new NotificationFacade(storage);

        return new ApplicationContext(auth, catalog, orders, dashboard, notifications, drafts);
    }

    public AuthFacade auth() {
        return auth;
    }

    public CatalogFacade catalog() {
        return catalog;
    }

    public OrderFacade orders() {
        return orders;
    }

    public DashboardFacade dashboard() {
        return dashboard;
    }

    public NotificationFacade notifications() {
        return notifications;
    }

    public DraftFacade drafts() {
        return drafts;
    }

    /** Dashboard port for controllers that only need metrics (ISP). */
    public DashboardDataPort dashboardData() {
        return dashboard;
    }

    public void logout() {
        auth.logout();
        catalog.clearCart();
    }
}
