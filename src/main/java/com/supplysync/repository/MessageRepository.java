package com.supplysync.repository;

import com.supplysync.models.Message;

import java.util.List;

/** Message persistence (ISP). */
public interface MessageRepository {
    void saveMessage(Message message);

    List<Message> findMessagesByRecipient(String recipientEmail);

    List<Message> findAllMessages();
}
