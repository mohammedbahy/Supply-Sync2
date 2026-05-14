package com.supplysync.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Saved checkout draft for a marketer (cart lines + contact fields).
 */
public class MarketerOrderDraft {
    private String marketerId;
    private String customerName;
    private String customerPhone;
    private String customerCountry;
    private String shippingAddress;
    private final List<DraftCartLine> lines = new ArrayList<>();

    public String getMarketerId() {
        return marketerId;
    }

    public void setMarketerId(String marketerId) {
        this.marketerId = marketerId;
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

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public List<DraftCartLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    public void addLine(String productId, int quantity) {
        if (productId == null || quantity <= 0) {
            return;
        }
        lines.add(new DraftCartLine(productId, quantity));
    }

    public static final class DraftCartLine {
        private final String productId;
        private final int quantity;

        public DraftCartLine(String productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public String getProductId() {
            return productId;
        }

        public int getQuantity() {
            return quantity;
        }
    }
}
