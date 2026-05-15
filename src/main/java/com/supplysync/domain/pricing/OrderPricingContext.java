package com.supplysync.domain.pricing;

import com.supplysync.models.Marketer;
import com.supplysync.models.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Encapsulates all data required to calculate the price of an order.
 */
public class OrderPricingContext {
    private final List<CartItemDto> items;
    private final User customer; // e.g. the marketer placing it, or a direct customer
    private final Marketer marketer;
    private final LocalDateTime date;
    private final List<String> promoCodes;

    public OrderPricingContext(List<CartItemDto> items, User customer, Marketer marketer, LocalDateTime date, List<String> promoCodes) {
        this.items = items != null ? Collections.unmodifiableList(items) : Collections.emptyList();
        this.customer = customer;
        this.marketer = marketer;
        this.date = date != null ? date : LocalDateTime.now();
        this.promoCodes = promoCodes != null ? Collections.unmodifiableList(promoCodes) : Collections.emptyList();
    }

    public List<CartItemDto> getItems() {
        return items;
    }

    public Optional<User> getCustomer() {
        return Optional.ofNullable(customer);
    }

    public Optional<Marketer> getMarketer() {
        return Optional.ofNullable(marketer);
    }

    public LocalDateTime getDate() {
        return date;
    }

    public List<String> getPromoCodes() {
        return promoCodes;
    }
}
