package com.supplysync.facade;

import com.supplysync.models.Order;
import com.supplysync.services.delivery.DeliveryService;
import com.supplysync.services.inventory.InventoryService;
import com.supplysync.services.notification.NotificationService;
import com.supplysync.services.order.OrderService;
import com.supplysync.services.auth.AuthService;
import com.supplysync.models.Product;
import com.supplysync.models.User;
import com.supplysync.models.Message;
import com.supplysync.models.AdminDashboardStats;
import com.supplysync.models.MarketerCancelResult;
import com.supplysync.models.OrderStatuses;

import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

public class OrderFacade {
    private final OrderService orderService;
    private final InventoryService inventoryService;
    private final DeliveryService deliveryService;
    private final NotificationService notificationService;
    private final AuthService authService;
    private final com.supplysync.repository.Storage storage;
    
    // In-memory cart for the current session
    private final List<Product> cart = new ArrayList<>();

    public OrderFacade(
            OrderService orderService,
            InventoryService inventoryService,
            DeliveryService deliveryService,
            NotificationService notificationService,
            AuthService authService,
            com.supplysync.repository.Storage storage
    ) {
        this.orderService = orderService;
        this.inventoryService = inventoryService;
        this.deliveryService = deliveryService;
        this.notificationService = notificationService;
        this.authService = authService;
        this.storage = storage;
    }

    public void processOrder(Order order) {
        orderService.createOrder(order);
        inventoryService.reserveInventory(order);
        deliveryService.schedule(order);
        notificationService.notifyOrderUpdate(order);
        clearCart();
    }

    public List<Product> getCatalog() {
        return inventoryService.getAllProducts();
    }

    public List<Order> getAllOrders() {
        if (orderService instanceof com.supplysync.services.order.DefaultOrderService) {
            return ((com.supplysync.services.order.DefaultOrderService)orderService).getAllOrders();
        }
        return java.util.Collections.emptyList();
    }

    public Optional<User> login(String email, String password) {
        return authService.login(email, password);
    }

    public void logout() {
        authService.logout();
        clearCart();
    }

    public User getCurrentUser() {
        return authService.getCurrentUser();
    }

    public void register(User user) {
        authService.register(user);
    }

    public boolean resetPassword(String email, String newPassword) {
        return authService.resetPassword(email, newPassword);
    }

    // Cart Management
    public void addToCart(Product product) {
        cart.add(product);
    }

    public List<Product> getCart() {
        return new ArrayList<>(cart);
    }

    public void clearCart() {
        cart.clear();
    }
    
    public void removeFromCart(Product product) {
        cart.removeIf(p -> p.getId().equals(product.getId()));
    }

    public List<com.supplysync.models.Marketer> getAllMarketers() {
        return storage.findAllMarketers();
    }

    public void addMarketer(com.supplysync.models.Marketer marketer) {
        storage.saveMarketer(marketer);
    }
    
    // Message Management
    public void sendMessage(Message message) {
        storage.saveMessage(message);
    }
    
    public List<Message> getMessagesForUser(String email) {
        return storage.findMessagesByRecipient(email);
    }
    
    public List<Message> getAllMessages() {
        return storage.findAllMessages();
    }
    
    public List<User> getAllUsers() {
        return storage.findAllUsers();
    }

    public AdminDashboardStats getAdminDashboardStats() {
        return AdminDashboardStats.from(getCatalog(), getAllOrders());
    }

    public void saveProduct(Product product) {
        storage.saveProduct(product);
    }

    public void deleteProduct(String productId) {
        storage.deleteProduct(productId);
    }

    public void persistOrder(Order order) {
        storage.saveOrder(order);
    }

    /** Orders placed by the logged-in marketer (by user id or legacy name match). */
    public List<Order> getMyOrders() {
        User u = getCurrentUser();
        if (u == null) {
            return java.util.Collections.emptyList();
        }
        return getAllOrders().stream()
                .filter(o -> orderBelongsToMarketer(o, u))
                .sorted(Comparator.comparing(Order::getEffectivePlacedAt).reversed())
                .collect(Collectors.toList());
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

    public void restoreOrderInventory(Order order) {
        inventoryService.restoreInventory(order);
    }

    /**
     * Marketer may cancel only {@link OrderStatuses#PENDING} orders within 24 hours of placement.
     * Restores catalog quantities to the database.
     */
    public MarketerCancelResult cancelOrderAsMarketer(String orderId) {
        User u = getCurrentUser();
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
        LocalDateTime deadline = o.getEffectivePlacedAt().plusHours(24);
        if (!LocalDateTime.now().isBefore(deadline)) {
            return MarketerCancelResult.TOO_LATE;
        }
        inventoryService.restoreInventory(o);
        o.setStatus(OrderStatuses.CANCELLED);
        storage.saveOrder(o);
        return MarketerCancelResult.SUCCESS;
    }
}
