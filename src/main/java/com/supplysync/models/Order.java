package com.supplysync.models;

import com.supplysync.patterns.OrderState;
import com.supplysync.patterns.OrderStates;
import com.supplysync.patterns.PendingState;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private String id;
    private Marketer marketer;
    private final List<Product> products = new ArrayList<>();
    private String customerName;
    private String customerPhone;
    private String customerCountry;
    private String customerAddress;
    private String shippingCity;
    private String status;
    private double totalAmount;
    private double commission;
    private LocalDate date;
    /** When the order was placed (used for 24h marketer cancel window). */
    private LocalDateTime placedAt;
    private String trackingNumber;
    private OrderState state = new PendingState();

    public Order() {
        this.date = LocalDate.now();
        this.placedAt = LocalDateTime.now();
        this.status = OrderStatuses.PENDING;
        this.state = new PendingState();
    }

    public Order(String id, Marketer marketer) {
        this();
        this.id = id;
        this.marketer = marketer;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Marketer getMarketer() { return marketer; }
    public void setMarketer(Marketer marketer) { this.marketer = marketer; }

    public List<Product> getProducts() { return products; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getCustomerCountry() { return customerCountry; }
    public void setCustomerCountry(String customerCountry) { this.customerCountry = customerCountry; }

    public String getCustomerAddress() { return customerAddress; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }

    public String getStatus() {
        return state != null ? state.getStatusName() : status;
    }

    public void setStatus(String status) {
        this.status = status != null ? OrderStatuses.normalize(status) : OrderStatuses.PENDING;
        this.state = OrderStates.forStatus(this.status);
    }

    public OrderState getState() {
        return state;
    }

    public void setState(OrderState state) {
        this.state = state;
        this.status = state != null ? state.getStatusName() : OrderStatuses.PENDING;
    }

    public void approve() {
        state.approve(this);
    }

    public void ship() {
        state.ship(this);
    }

    public void deliver() {
        state.deliver(this);
    }

    public void cancel() {
        state.cancel(this);
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

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public double getCommission() { return commission; }
    public void setCommission(double commission) { this.commission = commission; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalDateTime getPlacedAt() { return placedAt; }
    public void setPlacedAt(LocalDateTime placedAt) { this.placedAt = placedAt; }

    /** For cancel window when legacy rows have no placed_at in DB. */
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
