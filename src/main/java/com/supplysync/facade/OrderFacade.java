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
import com.supplysync.models.MarketerOrderDraft;
import com.supplysync.models.OrderStatuses;
import com.supplysync.models.OrderStatusHistoryEntry;
import com.supplysync.workflow.OrderEventBus;
import com.supplysync.workflow.OrderTransition;
import com.supplysync.workflow.OrderWorkflowService;

import java.util.Comparator;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
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
    private final OrderWorkflowService workflowService;

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
        this.workflowService = new OrderWorkflowService(storage, inventoryService);
    }

    public void processOrder(Order order) {
        orderService.createOrder(order);
        inventoryService.reserveInventory(order);
        deliveryService.schedule(order);
        notificationService.notifyOrderUpdate(order);
        workflowService.recordOrderPlaced(order, getCurrentUser());
        clearCart();
        User u = getCurrentUser();
        if (u != null) {
            storage.deleteMarketerOrderDraft(u.getId());
        }
        OrderEventBus.getInstance().publish(order.getId());
    }

    public Optional<Order> findOrderById(String orderId) {
        return storage.findOrderById(orderId);
    }

    public List<OrderTransition> getAllowedTransitions(String orderId) {
        return storage.findOrderById(orderId)
                .map(workflowService::getAllowedTransitions)
                .orElse(Collections.emptyList());
    }

    public void executeTransition(String orderId, OrderTransition transition) {
        workflowService.executeTransition(orderId, transition, getCurrentUser());
        storage.findOrderById(orderId).ifPresent(notificationService::notifyOrderUpdate);
        OrderEventBus.getInstance().publish(orderId);
    }

    public List<OrderStatusHistoryEntry> getOrderStatusHistory(String orderId) {
        return storage.findOrderStatusHistory(orderId);
    }

    public List<Order> getOrdersAwaitingApproval() {
        return getAllOrders().stream()
                .filter(o -> OrderStatuses.PENDING.equals(OrderStatuses.normalize(o.getStatus())))
                .sorted(Comparator.comparing(Order::getEffectivePlacedAt).reversed())
                .collect(Collectors.toList());
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

    public void saveUser(User user) {
        storage.saveUser(user);
    }

    public boolean emailTakenByOtherUser(String email, String excludeUserId) {
        if (email == null) {
            return false;
        }
        return storage.findUserByEmail(email.trim())
                .filter(u -> excludeUserId == null || !excludeUserId.equals(u.getId()))
                .isPresent();
    }

    public boolean resetPassword(String email, String newPassword) {
        return authService.resetPassword(email, newPassword);
    }

    // Cart Management
    public int countProductInCart(String productId) {
        if (productId == null) {
            return 0;
        }
        return (int) cart.stream().filter(p -> productId.equals(p.getId())).count();
    }

    /**
     * How many more units of this product can be added given DB stock and current cart.
     */
    public int availableUnitsToAddFromCatalog(Product product) {
        if (product == null) {
            return 0;
        }
        return Math.max(0, product.getQuantity() - countProductInCart(product.getId()));
    }

    public boolean addToCart(Product product) {
        if (product == null) {
            return false;
        }
        if (availableUnitsToAddFromCatalog(product) <= 0) {
            return false;
        }
        cart.add(product);
        return true;
    }

    public List<Product> getCart() {
        return new ArrayList<>(cart);
    }

    public void clearCart() {
        cart.clear();
    }

    /**
     * Removes a single cart line matching the product id (one unit).
     */
    public boolean removeOneUnitFromCart(String productId) {
        if (productId == null) {
            return false;
        }
        for (int i = 0; i < cart.size(); i++) {
            if (productId.equals(cart.get(i).getId())) {
                cart.remove(i);
                return true;
            }
        }
        return false;
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

    public void persistCheckoutContactForCurrentUser(String customerName, String phone, String country, String address) {
        User u = getCurrentUser();
        if (u == null) {
            return;
        }
        u.setPrefCustomerName(customerName != null ? customerName.trim() : "");
        u.setPrefCustomerPhone(phone != null ? phone.trim() : "");
        u.setPrefCustomerCountry(country != null ? country.trim() : "");
        u.setPrefShippingAddress(address != null ? address.trim() : "");
        storage.saveUser(u);
    }

    public void saveOrderDraftFromForm(String customerName, String phone, String country, String address) {
        User u = getCurrentUser();
        if (u == null || u.getId() == null) {
            return;
        }
        MarketerOrderDraft d = new MarketerOrderDraft();
        d.setMarketerId(u.getId());
        d.setCustomerName(customerName != null ? customerName.trim() : "");
        d.setCustomerPhone(phone != null ? phone.trim() : "");
        d.setCustomerCountry(country != null ? country.trim() : "");
        d.setShippingAddress(address != null ? address.trim() : "");
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Product p : cart) {
            counts.merge(p.getId(), 1, Integer::sum);
        }
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            d.addLine(e.getKey(), e.getValue());
        }
        storage.saveMarketerOrderDraft(d);
    }

    public boolean hasOrderDraft() {
        User u = getCurrentUser();
        return u != null && u.getId() != null && storage.findMarketerOrderDraft(u.getId()).isPresent();
    }

    public Optional<MarketerOrderDraft> getOrderDraft() {
        User u = getCurrentUser();
        if (u == null || u.getId() == null) {
            return Optional.empty();
        }
        return storage.findMarketerOrderDraft(u.getId());
    }

    public void discardOrderDraft() {
        User u = getCurrentUser();
        if (u != null && u.getId() != null) {
            storage.deleteMarketerOrderDraft(u.getId());
        }
    }

    public void applyOrderDraft(MarketerOrderDraft draft) {
        clearCart();
        List<Product> catalog = getCatalog();
        for (MarketerOrderDraft.DraftCartLine line : draft.getLines()) {
            Product ref = catalog.stream().filter(p -> p.getId().equals(line.getProductId())).findFirst().orElse(null);
            if (ref == null) {
                continue;
            }
            for (int i = 0; i < line.getQuantity(); i++) {
                if (!addToCart(ref)) {
                    break;
                }
            }
        }
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
