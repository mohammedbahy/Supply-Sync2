package com.supplysync.repository;

import com.supplysync.models.Order;
import com.supplysync.models.Product;
import com.supplysync.models.User;
import com.supplysync.models.Marketer;
import com.supplysync.models.MarketerOrderDraft;
import com.supplysync.models.Message;
import com.supplysync.models.OrderStatusHistoryEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryStorage implements Storage {
    private final List<Order> orders = new ArrayList<>();
    private final List<Product> products = new ArrayList<>();
    private final List<User> users = new ArrayList<>();
    private final List<Marketer> marketers = new ArrayList<>();
    private final List<Message> messages = new ArrayList<>();
    private final Map<String, MarketerOrderDraft> marketerDrafts = new ConcurrentHashMap<>();
    private final List<OrderStatusHistoryEntry> statusHistory = new ArrayList<>();
    private long historySeq = 1L;

    public InMemoryStorage() {
        // Seed Products from 212.txt
        seedProducts();

        // Seed users
        users.add(new User("1", "admin@gmail.com", "Admin@123!", "Admin User", "ADMIN"));
        users.add(new User("2", "user@gmail.com", "User@123", "Marcus Miller", "MARKETER"));
        
        // Seed marketers for the management page
        marketers.add(new Marketer("1", "Marcus Miller"));
        marketers.add(new Marketer("2", "John Doe"));
    }

    private void seedProducts() {
        for (Object[] row : ProductSeedData.rows()) {
            products.add(new Product(
                (String) row[0],
                (String) row[1],
                (int) row[3],
                (String) row[2],
                (double) row[4],
                ""
            ));
        }
    }

    @Override
    public void saveOrder(Order order) {
        orders.removeIf(o -> o.getId().equals(order.getId()));
        orders.add(order);
    }

    @Override
    public Optional<Order> findOrderById(String id) {
        return orders.stream().filter(o -> o.getId().equals(id)).findFirst();
    }

    @Override
    public List<Order> findAllOrders() {
        return new ArrayList<>(orders);
    }

    @Override
    public void saveProduct(Product product) {
        products.removeIf(p -> p.getId().equals(product.getId()));
        products.add(product);
    }

    @Override
    public void deleteProduct(String productId) {
        products.removeIf(p -> p.getId().equals(productId));
    }

    @Override
    public List<Product> findAllProducts() {
        return new ArrayList<>(products);
    }

    @Override
    public void saveUser(User user) {
        users.removeIf(u -> u.getEmail().equalsIgnoreCase(user.getEmail())
                || (u.getId() != null && u.getId().equals(user.getId())));
        users.add(user);
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        return users.stream().filter(u -> u.getEmail().equals(email)).findFirst();
    }
    
    @Override
    public List<Marketer> findAllMarketers() {
        return new ArrayList<>(marketers);
    }
    
    @Override
    public void saveMarketer(Marketer marketer) {
        marketers.removeIf(m -> m.getId().equals(marketer.getId()));
        marketers.add(marketer);
    }
    
    @Override
    public void saveMessage(Message message) {
        messages.add(message);
    }
    
    @Override
    public List<Message> findMessagesByRecipient(String recipientEmail) {
        return messages.stream()
            .filter(m -> m.getRecipientEmail().equals(recipientEmail))
            .collect(java.util.stream.Collectors.toList());
    }
    
    @Override
    public List<Message> findAllMessages() {
        return new ArrayList<>(messages);
    }
    
    @Override
    public List<User> findAllUsers() {
        return new ArrayList<>(users);
    }

    @Override
    public void saveMarketerOrderDraft(MarketerOrderDraft draft) {
        if (draft.getMarketerId() != null) {
            marketerDrafts.put(draft.getMarketerId(), draft);
        }
    }

    @Override
    public Optional<MarketerOrderDraft> findMarketerOrderDraft(String marketerId) {
        if (marketerId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(marketerDrafts.get(marketerId));
    }

    @Override
    public void deleteMarketerOrderDraft(String marketerId) {
        if (marketerId != null) {
            marketerDrafts.remove(marketerId);
        }
    }

    @Override
    public void appendStatusHistory(String orderId, String fromStatus, String toStatus, String transition, String actorUserId) {
        OrderStatusHistoryEntry e = new OrderStatusHistoryEntry();
        e.setOrderId(orderId);
        e.setFromStatus(fromStatus);
        e.setToStatus(toStatus);
        e.setTransitionName(transition);
        e.setActorId(actorUserId);
        e.setActorName("System");
        e.setCreatedAt(java.time.LocalDateTime.now());
        appendOrderStatusHistory(e);
    }

    @Override
    public void appendOrderStatusHistory(OrderStatusHistoryEntry entry) {
        entry.setId(historySeq++);
        statusHistory.add(entry);
    }

    @Override
    public List<OrderStatusHistoryEntry> findOrderStatusHistory(String orderId) {
        List<OrderStatusHistoryEntry> out = new ArrayList<>();
        for (OrderStatusHistoryEntry e : statusHistory) {
            if (orderId != null && orderId.equals(e.getOrderId())) {
                out.add(e);
            }
        }
        return out;
    }
}
