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
    RETURN;

    public String displayLabel(boolean arabic) {
        switch (this) {
            case APPROVE: return arabic ? "موافقة" : "Approve";
            case PLACE_ON_HOLD: return arabic ? "تعليق" : "Hold";
            case RELEASE_HOLD: return arabic ? "إلغاء التعليق" : "Release Hold";
            case SHIP_PARTIAL: return arabic ? "شحن جزئي" : "Ship Partial";
            case SHIP: return arabic ? "شحن" : "Ship";
            case DELIVER: return arabic ? "تسليم" : "Deliver";
            case CANCEL: return arabic ? "إلغاء" : "Cancel";
            case RETURN: return arabic ? "إرجاع" : "Return";
            default: return name();
        }
    }
}
