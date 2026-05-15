package com.supplysync.facade;

import com.supplysync.models.Message;
import com.supplysync.repository.MessageRepository;

import java.util.List;

/**
 * Messages and report communication (SRP).
 */
public final class NotificationFacade {
    private final MessageRepository messages;

    public NotificationFacade(MessageRepository messages) {
        this.messages = messages;
    }

    public void sendMessage(Message message) {
        messages.saveMessage(message);
    }

    public List<Message> getMessagesForUser(String email) {
        return messages.findMessagesByRecipient(email);
    }

    public List<Message> getAllMessages() {
        return messages.findAllMessages();
    }
}
