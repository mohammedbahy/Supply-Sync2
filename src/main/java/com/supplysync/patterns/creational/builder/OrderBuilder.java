package com.supplysync.patterns.creational.builder;

import com.supplysync.models.Marketer;
import com.supplysync.models.Order;
import com.supplysync.domain.order.OrderStatusHydrator;
import com.supplysync.models.OrderStatuses;
import com.supplysync.models.Product;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderBuilder {
    private String id;
    private String marketerId;
    private String customerName;
    private String customerPhone;
    private String shippingAddress;
    private String shippingCity;
    private final List<Product> products = new ArrayList<>();
    private double commission;
    private double totalAmount;
    private String trackingNumber;

    public OrderBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public OrderBuilder withMarketer(Marketer marketer) {
        if (marketer != null) {
            this.marketerId = marketer.getId();
        }
        return this;
    }

    public OrderBuilder withMarketerId(String marketerId) {
        this.marketerId = marketerId;
        return this;
    }

    public OrderBuilder withCustomerName(String customerName) {
        this.customerName = customerName;
        return this;
    }

    public OrderBuilder withCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
        return this;
    }

    public OrderBuilder withShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
        return this;
    }

    public OrderBuilder withShippingCity(String shippingCity) {
        this.shippingCity = shippingCity;
        return this;
    }

    public OrderBuilder addProduct(Product product) {
        if (product != null) {
            this.products.add(product);
        }
        return this;
    }

    public OrderBuilder withProducts(List<Product> products) {
        this.products.clear();
        if (products != null) {
            this.products.addAll(products);
        }
        return this;
    }

    public OrderBuilder withCommission(double commission) {
        this.commission = commission;
        return this;
    }

    public OrderBuilder withTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
        return this;
    }

    public OrderBuilder withTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
        return this;
    }

    public Order build() {
        if (id == null || marketerId == null || products.isEmpty()) {
            throw new IllegalStateException("Missing required fields: id, marketerId, or products");
        }

        Order order = new Order();
        order.setId(id);
        order.setMarketer(new Marketer(marketerId, ""));
        order.setCustomerName(customerName);
        order.setCustomerPhone(customerPhone);
        order.setShippingAddress(shippingAddress);
        order.setShippingCity(shippingCity);
        order.setCustomerCountry(shippingCity);
        order.getProducts().addAll(products);
        order.setCommission(commission);
        if (totalAmount > 0) {
            order.setTotalAmount(totalAmount);
        } else {
            order.setTotalAmount(products.stream().mapToDouble(Product::getPrice).sum());
        }
        order.setTrackingNumber(trackingNumber);
        OrderStatusHydrator.hydrate(order, OrderStatuses.AWAITING_APPROVAL);
        LocalDateTime now = LocalDateTime.now();
        order.setPlacedAt(now);
        order.setDate(LocalDate.now());
        return order;
    }
}
