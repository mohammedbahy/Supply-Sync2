package com.supplysync.models;

import java.time.LocalDateTime;

public class Message {
    private String id;
    private String orderId;
    private String recipientEmail;
    private String senderEmail;
    private String title;
    private String content;
    private String status; // PENDING, APPROVED, CANCELLED, READ
    private LocalDateTime createdAt;
    private boolean isRead;

    public Message() {
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }

    public Message(String id, String orderId, String recipientEmail, String senderEmail, 
                   String title, String content, String status) {
        this();
        this.id = id;
        this.orderId = orderId;
        this.recipientEmail = recipientEmail;
        this.senderEmail = senderEmail;
        this.title = title;
        this.content = content;
        this.status = status;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }

    public String getSenderEmail() { return senderEmail; }
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}
