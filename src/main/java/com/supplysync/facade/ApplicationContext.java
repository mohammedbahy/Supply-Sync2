package com.supplysync.facade;

import com.supplysync.dashboard.DashboardDataPort;
import com.supplysync.domain.order.event.OrderEventBus;
import com.supplysync.patterns.behavioral.strategy.DefaultPricingStrategy;
import com.supplysync.patterns.creational.factory.ServiceFactory;
import com.supplysync.repository.Storage;
import com.supplysync.services.auth.AuthService;
import com.supplysync.services.delivery.DeliveryService;
import com.supplysync.services.inventory.InventoryService;
import com.supplysync.services.notification.NotificationService;
import com.supplysync.services.order.OrderService;
import com.supplysync.services.order.OrderWorkflowService;

public final class ApplicationContext {
    private final AuthFacade auth;
    private final CatalogFacade catalog;
    private final OrderFacade orders;
    private final DashboardFacade dashboard;
    private final NotificationFacade notifications;
    private final DraftFacade drafts;
    private final OrderWorkflowService orderWorkflow;
    private final OrderEventBus eventBus;

    public ApplicationContext(
            AuthFacade auth,
            CatalogFacade catalog,
            OrderFacade orders,
            DashboardFacade dashboard,
            NotificationFacade notifications,
            DraftFacade drafts,
            OrderWorkflowService orderWorkflow,
            OrderEventBus eventBus
    ) {
        this.auth = auth;
        this.catalog = catalog;
        this.orders = orders;
        this.dashboard = dashboard;
        this.notifications = notifications;
        this.drafts = drafts;
        this.orderWorkflow = orderWorkflow;
        this.eventBus = eventBus;
    }

    public static ApplicationContext createDefault() {
        Storage storage = ServiceFactory.getStorage();
        AuthService authService = ServiceFactory.createAuthService();
        OrderService orderService = ServiceFactory.createOrderService();
        InventoryService inventoryService = ServiceFactory.createInventoryService();
        DeliveryService deliveryService = ServiceFactory.createDeliveryService();
        NotificationService notificationService = ServiceFactory.createNotificationService();
        OrderEventBus eventBus = new OrderEventBus();
        OrderWorkflowService workflow = OrderWorkflowService.createDefault(
                storage,
                inventoryService,
                deliveryService,
                notificationService,
                storage,
                eventBus,
                new DefaultPricingStrategy()
        );

        AuthFacade auth = new AuthFacade(authService, storage);
        CatalogFacade catalog = new CatalogFacade(inventoryService, storage);
        DraftFacade drafts = new DraftFacade(storage, auth, catalog);
        OrderFacade orders = new OrderFacade(
                workflow,
                eventBus,
                auth,
                catalog,
                drafts,
                inventoryService
        );
        DashboardFacade dashboard = new DashboardFacade(orderService, catalog, storage);
        NotificationFacade notifications = new NotificationFacade(storage);

        return new ApplicationContext(auth, catalog, orders, dashboard, notifications, drafts, workflow, eventBus);
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

    public OrderWorkflowService orderWorkflow() {
        return orderWorkflow;
    }

    public DashboardDataPort dashboardData() {
        return dashboard;
    }

    public OrderEventBus eventBus() {
        return eventBus;
    }

    public void logout() {
        auth.logout();
        catalog.clearCart();
    }
}
