package com.supplysync.workflow;

/**
 * Named workflow actions — all status changes go through these, not direct {@code setStatus}.
 */
public enum OrderTransition {
    APPROVE,
    MARK_DELIVERED,
    CANCEL,
    PLACE_ON_HOLD,
    RELEASE_HOLD,
    SHIP_PARTIAL;

    public String displayLabel(boolean arabic) {
        switch (this) {
            case APPROVE:
                return arabic ? "اعتماد / في الطريق" : "Approve / Mark in transit";
            case MARK_DELIVERED:
                return arabic ? "تم التسليم" : "Mark delivered";
            case CANCEL:
                return arabic ? "إلغاء الطلب" : "Cancel order";
            case PLACE_ON_HOLD:
                return arabic ? "تعليق الطلب" : "Place on hold";
            case RELEASE_HOLD:
                return arabic ? "إلغاء التعليق" : "Release hold";
            case SHIP_PARTIAL:
                return arabic ? "شحن جزئي" : "Ship partial";
            default:
                return name();
        }
    }
}
