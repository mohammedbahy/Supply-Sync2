package com.supplysync.models;

/**
 * Canonical order status values and display labels.
 */
public final class OrderStatuses {
    public static final String PENDING = "PENDING";
    public static final String IN_TRANSIT = "IN_TRANSIT";
    public static final String DELIVERED = "DELIVERED";
    public static final String CANCELLED = "CANCELLED";
    /** Legacy DB value — treated as {@link #IN_TRANSIT} in UI and logic. */
    public static final String APPROVED = "APPROVED";

    private OrderStatuses() {}

    /** Normalize legacy status for comparisons. */
    public static String normalize(String status) {
        if (status == null) {
            return PENDING;
        }
        if (APPROVED.equals(status)) {
            return IN_TRANSIT;
        }
        return status;
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
            default:
                return status != null ? status : "";
        }
    }
}
