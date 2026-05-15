package com.supplysync.domain.order;

/**
 * Explicit workflow actions — the only way to change order status (besides repository hydration).
 */
public enum OrderTransition {
    APPROVE,
    PLACE_ON_HOLD,
    RELEASE_HOLD,
    SHIP_PARTIAL,
    SHIP,
    DELIVER,
    CANCEL,
    RETURN
}
