package com.supplysync.models;

/**
 * Canonical order workflow status values and display labels.
 */
public final class OrderStatuses {
    public static final String AWAITING_APPROVAL = "AWAITING_APPROVAL";
    /** Legacy DB value — same workflow stage as {@link #AWAITING_APPROVAL}. */
    public static final String PENDING = "PENDING";
    public static final String APPROVED = "APPROVED";
    public static final String IN_TRANSIT = "IN_TRANSIT";
    public static final String DELIVERED = "DELIVERED";
    public static final String CANCELLED = "CANCELLED";
    public static final String ON_HOLD = "ON_HOLD";
    public static final String PARTIALLY_SHIPPED = "PARTIALLY_SHIPPED";
    public static final String RETURNED = "RETURNED";

    private OrderStatuses() {}

    /** Normalize persisted / legacy values into canonical workflow statuses. */
    public static String normalizeWorkflow(String status) {
        if (status == null || status.isBlank()) {
            return AWAITING_APPROVAL;
        }
        if (PENDING.equals(status)) {
            return AWAITING_APPROVAL;
        }
        return status;
    }

    /** Normalize for UI grouping and legacy comparisons. */
    public static String normalize(String status) {
        if (status == null) {
            return PENDING;
        }
        if (AWAITING_APPROVAL.equals(status)) {
            return PENDING;
        }
        return status;
    }

    public static String displayLabel(String status, boolean arabic) {
        String n = normalizeWorkflow(status);
        switch (n) {
            case AWAITING_APPROVAL:
                return arabic ? "بانتظار الاعتماد" : "Awaiting approval";
            case PENDING:
                return arabic ? "قيد المعالجة" : "Processing";
            case APPROVED:
                return arabic ? "معتمد" : "Approved";
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
            case RETURNED:
                return arabic ? "مرتجع" : "Returned";
            default:
                return status != null ? status : "";
        }
    }
}
