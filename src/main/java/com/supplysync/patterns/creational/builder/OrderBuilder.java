package com.supplysync.patterns.creational.builder;

import com.supplysync.models.Marketer;
import com.supplysync.models.Order;
import com.supplysync.models.Product;

public class OrderBuilder {
    private final Order order = new Order();

    public OrderBuilder withId(String id) {
        order.setId(id);
        return this;
    }

    public OrderBuilder withMarketer(Marketer marketer) {
        order.setMarketer(marketer);
        return this;
    }

    public OrderBuilder addProduct(Product product) {
        order.getProducts().add(product);
        return this;
    }

    public Order build() {
        return order;
    }
}
