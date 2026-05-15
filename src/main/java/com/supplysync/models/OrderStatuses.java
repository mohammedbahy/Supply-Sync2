package com.supplysync.models;

/**
 * Canonical order status values and display labels.
 */
public final class OrderStatuses {
    public static final String PENDING = "PENDING";
    public static final String IN_TRANSIT = "IN_TRANSIT";
    public static final String DELIVERED = "DELIVERED";
    public static final String CANCELLED = "CANCELLED";
    public static final String ON_HOLD = "ON_HOLD";
    public static final String PARTIALLY_SHIPPED = "PARTIALLY_SHIPPED";
    public static final String RETURNED = "RETURNED";
    /** Legacy DB value — treated as {@link #IN_TRANSIT} in UI and logic. */
    public static final String APPROVED = "APPROVED";
    /** Legacy alias for PENDING. */
    public static final String AWAITING_APPROVAL = PENDING;

    private OrderStatuses() {}

    /** Normalize legacy status for comparisons. */
    public static String normalize(String status) {
        if (status == null || AWAITING_APPROVAL.equals(status)) {
            return PENDING;
        }
        if (APPROVED.equals(status)) {
            return IN_TRANSIT;
        }
        return status;
    }
    
    public static String normalizeWorkflow(String status) {
        return normalize(status);
    }

    public static String displayLabel(String status, boolean arabic) {
        String n = normalize(status);
        switch (n) {
            case PENDING:
                return arabic ? "قيد المعالجة" : "Processing";
            case IN_TRANSIT:
                return arabic ? "في الطريق" : "In transit";
            case DELIVERED:
                return arabic ? "تم التسليم" : "Delivered";
            case CANCELLED:
                return arabic ? "ملغى" : "Cancelled";
            case ON_HOLD:
                return arabic ? "معلق" : "On hold";
            case PARTIALLY_SHIPPED:
                return arabic ? "شحن جزئي" : "Partially shipped";
            default:
                return status != null ? status : "";
        }
    }
}
