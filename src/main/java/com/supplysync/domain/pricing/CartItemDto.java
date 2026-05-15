package com.supplysync.domain.pricing;

import java.util.Objects;

/**
 * Data Transfer Object representing an item in the cart.
 */
public class CartItemDto {
    private final String productId;
    private final String name;
    private final double unitPrice;
    private final int quantity;

    public CartItemDto(String productId, String name, double unitPrice, int quantity) {
        this.productId = productId;
        this.name = name;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public String getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItemDto that = (CartItemDto) o;
        return Double.compare(that.unitPrice, unitPrice) == 0 &&
                quantity == that.quantity &&
                Objects.equals(productId, that.productId) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, name, unitPrice, quantity);
    }
}
