package com.supplysync.models;

/**
 * Canonical workflow status values and display labels.
 */
public final class OrderStatuses {
    public static final String AWAITING_APPROVAL = "AWAITING_APPROVAL";
    public static final String APPROVED = "APPROVED";
    public static final String ON_HOLD = "ON_HOLD";
    public static final String PARTIALLY_SHIPPED = "PARTIALLY_SHIPPED";
    public static final String IN_TRANSIT = "IN_TRANSIT";
    public static final String DELIVERED = "DELIVERED";
    public static final String RETURNED = "RETURNED";
    public static final String CANCELLED = "CANCELLED";

    /** @deprecated use {@link #AWAITING_APPROVAL} — kept for legacy DB rows */
    public static final String PENDING = "PENDING";

    private OrderStatuses() {
    }

    /**
     * Maps persisted/legacy values to current workflow statuses (SSOT hydration).
     */
    public static String normalizeWorkflow(String status) {
        if (status == null || status.isBlank()) {
            return AWAITING_APPROVAL;
        }
        switch (status) {
            case PENDING:
                return AWAITING_APPROVAL;
            case "SHIPPED":
                return IN_TRANSIT;
            default:
                return status;
        }
    }

    /** @deprecated use {@link #normalizeWorkflow(String)} */
    public static String normalize(String status) {
        return normalizeWorkflow(status);
    }

    public static String displayLabel(String status, boolean arabic) {
        String n = normalizeWorkflow(status);
        switch (n) {
            case AWAITING_APPROVAL:
                return arabic ? "بانتظار الموافقة" : "Awaiting approval";
            case APPROVED:
                return arabic ? "معتمد" : "Approved";
            case ON_HOLD:
                return arabic ? "معلق" : "On hold";
            case PARTIALLY_SHIPPED:
                return arabic ? "شحن جزئي" : "Partially shipped";
            case IN_TRANSIT:
                return arabic ? "في الطريق" : "In transit";
            case DELIVERED:
                return arabic ? "تم التسليم" : "Delivered";
            case RETURNED:
                return arabic ? "مرتجع" : "Returned";
            case CANCELLED:
                return arabic ? "ملغى" : "Cancelled";
            case PENDING:
                return arabic ? "قيد المعالجة" : "Processing";
            default:
                return status != null ? status : "";
        }
    }
}
