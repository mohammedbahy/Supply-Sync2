package com.supplysync.models;

import com.supplysync.domain.order.OrderTransition;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Order aggregate. Workflow status is SSOT via {@link #workflowStatus};
 * use {@link com.supplysync.services.order.OrderWorkflowService} to change status.
 */
public class Order {
    private String id;
    private Marketer marketer;
    private final List<Product> products = new ArrayList<>();
    private String customerName;
    private String customerPhone;
    private String customerCountry;
    private String customerAddress;
    private String shippingCity;
    private String workflowStatus = OrderStatuses.AWAITING_APPROVAL;
    private double totalAmount;
    private double commission;
    private LocalDate date;
    private LocalDateTime placedAt;
    private String trackingNumber;
    private boolean inventoryReserved;
    /** Status before {@link OrderStatuses#ON_HOLD} when held during delivery. */
    private String statusBeforeHold;

    public Order() {
        this.date = LocalDate.now();
        this.placedAt = LocalDateTime.now();
        this.workflowStatus = OrderStatuses.AWAITING_APPROVAL;
    }

    public Order(String id, Marketer marketer) {
        this();
        this.id = id;
        this.marketer = marketer;
    }

    /** Workflow status — single source of truth. */
    public String getStatus() {
        return workflowStatus;
    }

    /** Repository hydration only — called from {@link com.supplysync.domain.order.OrderStatusHydrator}. */
    public void internalSetWorkflowStatus(String status) {
        this.workflowStatus = status != null ? OrderStatuses.normalizeWorkflow(status) : OrderStatuses.AWAITING_APPROVAL;
    }

    public void markInventoryReserved(boolean reserved) {
        this.inventoryReserved = reserved;
    }

    public boolean isInventoryReserved() {
        return inventoryReserved;
    }

    /**
     * Allowed transitions for the given actor; populated by {@link com.supplysync.domain.order.OrderStateMachine}.
     */
    public Set<OrderTransition> getAllowedTransitions(User actor,
                                                      com.supplysync.domain.order.OrderStateMachine machine) {
        if (machine == null || actor == null) {
            return Collections.emptySet();
        }
        return machine.getAllowedTransitions(this, actor);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Marketer getMarketer() {
        return marketer;
    }

    public void setMarketer(Marketer marketer) {
        this.marketer = marketer;
    }

    public List<Product> getProducts() {
        return products;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getCustomerCountry() {
        return customerCountry;
    }

    public void setCustomerCountry(String customerCountry) {
        this.customerCountry = customerCountry;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getMarketerId() {
        return marketer != null ? marketer.getId() : null;
    }

    public void setMarketerId(String marketerId) {
        if (marketerId == null) {
            this.marketer = null;
            return;
        }
        if (this.marketer == null) {
            this.marketer = new Marketer(marketerId, "");
        } else {
            this.marketer.setId(marketerId);
        }
    }

    public String getShippingAddress() {
        return customerAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.customerAddress = shippingAddress;
    }

    public String getShippingCity() {
        return shippingCity != null ? shippingCity : customerCountry;
    }

    public void setShippingCity(String shippingCity) {
        this.shippingCity = shippingCity;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getStatusBeforeHold() {
        return statusBeforeHold;
    }

    public void setStatusBeforeHold(String statusBeforeHold) {
        this.statusBeforeHold = statusBeforeHold;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getCommission() {
        return commission;
    }

    public void setCommission(double commission) {
        this.commission = commission;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDateTime getPlacedAt() {
        return placedAt;
    }

    public void setPlacedAt(LocalDateTime placedAt) {
        this.placedAt = placedAt;
    }

    public LocalDateTime getEffectivePlacedAt() {
        if (placedAt != null) {
            return placedAt;
        }
        if (date != null) {
            return date.atStartOfDay();
        }
        return LocalDateTime.now();
    }

}
