package com.supplysync.repository;

/**
 * Persists order workflow audit trail (status transitions).
 */
public interface OrderStatusHistoryRepository {
    void appendStatusHistory(String orderId,
                             String fromStatus,
                             String toStatus,
                             String transition,
                             String actorUserId);
}
