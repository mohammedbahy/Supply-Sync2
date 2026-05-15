package com.supplysync.repository;

/**
 * Composite persistence port. Implementations satisfy all segregated repositories (ISP).
 * Prefer depending on specific repository interfaces in new code.
 */
public interface Storage extends
        OrderRepository,
        ProductRepository,
        UserRepository,
        MarketerRepository,
        MessageRepository,
        DraftRepository,
        OrderStatusHistoryRepository {
}
