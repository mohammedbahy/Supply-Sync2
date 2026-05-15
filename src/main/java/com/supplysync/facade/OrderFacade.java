package com.supplysync.facade;

import com.supplysync.models.MarketerCancelResult;
import com.supplysync.models.Order;
import com.supplysync.models.OrderStatuses;
import com.supplysync.models.User;
import com.supplysync.repository.OrderRepository;
import com.supplysync.services.delivery.DeliveryService;
import com.supplysync.services.inventory.InventoryService;
import com.supplysync.services.notification.NotificationService;
import com.supplysync.patterns.StandardPricingStrategy;
import com.supplysync.patterns.behavioral.observer.OrderObserver;
import com.supplysync.patterns.behavioral.observer.OrderSubject;
import com.supplysync.patterns.behavioral.strategy.PricingStrategy;
import com.supplysync.services.order.OrderService;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Order creation, processing, status, and marketer cancellation (SRP).
 */
public final class OrderFacade {
    private final OrderService orderService;
    private final InventoryService inventoryService;
    private final DeliveryService deliveryService;
    private final NotificationService notificationService;
    private final OrderRepository orders;
    private final AuthFacade auth;
    private final CatalogFacade catalog;
    private final DraftFacade drafts;
    private final OrderSubject orderSubject = new OrderSubject();
    private PricingStrategy pricingStrategy = new StandardPricingStrategy();

    public OrderFacade(
            OrderService orderService,
            InventoryService inventoryService,
            DeliveryService deliveryService,
            NotificationService notificationService,
            OrderRepository orders,
            AuthFacade auth,
            CatalogFacade catalog,
            DraftFacade drafts
    ) {
        this.orderService = orderService;
        this.inventoryService = inventoryService;
        this.deliveryService = deliveryService;
        this.notificationService = notificationService;
        this.orders = orders;
        this.auth = auth;
        this.catalog = catalog;
        this.drafts = drafts;
    }

    public void setPricingStrategy(PricingStrategy strategy) {
        this.pricingStrategy = strategy != null ? strategy : new StandardPricingStrategy();
    }

    public void addOrderObserver(OrderObserver observer) {
        orderSubject.addObserver(observer);
    }

    public void removeOrderObserver(OrderObserver observer) {
        orderSubject.removeObserver(observer);
    }

    public void processOrder(Order order) {
        double commission = pricingStrategy.calculateCommission(order);
        order.setCommission(commission);
        orderService.createOrder(order);
        inventoryService.reserveInventory(order);
        deliveryService.schedule(order);
        notificationService.notifyOrderUpdate(order);
        catalog.clearCart();
        drafts.discardDraftForCurrentUser();
        orderSubject.notifyObservers(order);
    }

    public List<Order> getAllOrders() {
        return orderService.findAllOrders();
    }

    public List<Order> getMyOrders() {
        User u = auth.getCurrentUser();
        if (u == null) {
            return java.util.Collections.emptyList();
        }
        return getAllOrders().stream()
                .filter(o -> orderBelongsToMarketer(o, u))
                .sorted(Comparator.comparing(Order::getEffectivePlacedAt).reversed())
                .collect(Collectors.toList());
    }

    public void persistOrder(Order order) {
        orders.saveOrder(order);
        orderSubject.notifyObservers(order);
    }

    public void restoreOrderInventory(Order order) {
        inventoryService.restoreInventory(order);
    }

    public MarketerCancelResult cancelOrderAsMarketer(String orderId) {
        User u = auth.getCurrentUser();
        if (u == null) {
            return MarketerCancelResult.NOT_OWNER;
        }
        Optional<Order> opt = getAllOrders().stream().filter(o -> o.getId().equals(orderId)).findFirst();
        if (!opt.isPresent()) {
            return MarketerCancelResult.NOT_FOUND;
        }
        Order o = opt.get();
        if (!orderBelongsToMarketer(o, u)) {
            return MarketerCancelResult.NOT_OWNER;
        }
        if (!OrderStatuses.PENDING.equals(o.getStatus())) {
            return MarketerCancelResult.INVALID_STATUS;
        }
        try {
            o.cancel();
        } catch (IllegalStateException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("24 hours")) {
                return MarketerCancelResult.TOO_LATE;
            }
            return MarketerCancelResult.INVALID_STATUS;
        }
        inventoryService.restoreInventory(o);
        orders.saveOrder(o);
        orderSubject.notifyObservers(o);
        return MarketerCancelResult.SUCCESS;
    }

    private static boolean orderBelongsToMarketer(Order o, User u) {
        if (o.getMarketer() != null && u.getId() != null && u.getId().equals(o.getMarketer().getId())) {
            return true;
        }
        return o.getMarketer() == null
                && o.getCustomerName() != null
                && u.getName() != null
                && o.getCustomerName().trim().equalsIgnoreCase(u.getName().trim());
    }
}
