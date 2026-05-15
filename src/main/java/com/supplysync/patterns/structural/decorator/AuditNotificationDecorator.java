package com.supplysync.patterns.structural.decorator;

import com.supplysync.models.Message;
import com.supplysync.models.Order;
import com.supplysync.repository.Storage;
import com.supplysync.services.notification.NotificationService;

import java.time.LocalDateTime;
import java.util.UUID;

public class AuditNotificationDecorator extends NotificationDecorator {
    private final Storage storage;

    public AuditNotificationDecorator(NotificationService wrapped, Storage storage) {
        super(wrapped);
        this.storage = storage;
    }

    @Override
    public void notifyOrderUpdate(Order order) {
        Message auditMessage = new Message();
        auditMessage.setId(UUID.randomUUID().toString());
        auditMessage.setOrderId(order.getId());
        auditMessage.setContent(String.format(
                "AUDIT: Order %s updated to status %s for marketer %s at %s",
                order.getId(),
                order.getStatus(),
                order.getMarketerId(),
                LocalDateTime.now()
        ));
        auditMessage.setRecipientEmail("audit@supplysync.com");
        auditMessage.setSenderEmail("system@supplysync.com");
        auditMessage.setTitle("Order audit");
        auditMessage.setStatus(order.getStatus());
        auditMessage.setCreatedAt(LocalDateTime.now());
        storage.saveMessage(auditMessage);

        wrapped.notifyOrderUpdate(order);
    }
}
