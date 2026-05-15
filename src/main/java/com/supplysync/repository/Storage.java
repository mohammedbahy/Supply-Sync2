package com.supplysync.repository;

import com.supplysync.models.Order;
import com.supplysync.models.Product;
import com.supplysync.models.User;
import com.supplysync.models.MarketerOrderDraft;
import com.supplysync.models.Message;
import com.supplysync.models.Marketer;
import com.supplysync.models.OrderStatusHistoryEntry;

import java.util.List;
import java.util.Optional;

public interface Storage extends OrderRepository, ProductRepository, UserRepository, MarketerRepository, MessageRepository, DraftRepository, OrderStatusHistoryRepository {
    void saveOrder(Order order);
    Optional<Order> findOrderById(String id);
    List<Order> findAllOrders();

    void saveProduct(Product product);
    void deleteProduct(String productId);
    List<Product> findAllProducts();

    void saveUser(User user);
    Optional<User> findUserByEmail(String email);
    List<User> findAllUsers();

    List<Marketer> findAllMarketers();
    void saveMarketer(Marketer marketer);

    void saveMessage(Message message);
    List<Message> findMessagesByRecipient(String recipientEmail);
    List<Message> findAllMessages();

    void saveMarketerOrderDraft(MarketerOrderDraft draft);
    Optional<MarketerOrderDraft> findMarketerOrderDraft(String marketerId);
    void deleteMarketerOrderDraft(String marketerId);

    void appendOrderStatusHistory(OrderStatusHistoryEntry entry);

    List<OrderStatusHistoryEntry> findOrderStatusHistory(String orderId);
}
