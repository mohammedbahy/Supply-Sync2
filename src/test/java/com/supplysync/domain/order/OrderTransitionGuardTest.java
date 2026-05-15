package com.supplysync.domain.order;

import com.supplysync.models.Marketer;
import com.supplysync.models.Order;
import com.supplysync.models.OrderStatuses;
import com.supplysync.models.Product;
import com.supplysync.models.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderTransitionGuardTest {
    private final OrderTransitionGuard guard = new OrderTransitionGuard();

    @Test
    void marketerCanCancelWithin24Hours() {
        Order order = new Order("O1", new Marketer("m1", "M"));
        order.getProducts().add(new Product("p1", "X", 1, "C", 10, ""));
        OrderStatusHydrator.hydrate(order, OrderStatuses.AWAITING_APPROVAL);
        order.setPlacedAt(LocalDateTime.now().minusHours(2));
        User marketer = new User("m1", "m@t.com", "p", "M", "MARKETER");
        assertDoesNotThrow(() -> guard.validate(order, OrderTransition.CANCEL, marketer));
    }

    @Test
    void marketerCannotCancelAfter24Hours() {
        Order order = new Order("O1", new Marketer("m1", "M"));
        order.getProducts().add(new Product("p1", "X", 1, "C", 10, ""));
        OrderStatusHydrator.hydrate(order, OrderStatuses.AWAITING_APPROVAL);
        order.setPlacedAt(LocalDateTime.now().minusHours(25));
        User marketer = new User("m1", "m@t.com", "p", "M", "MARKETER");
        assertThrows(IllegalStateException.class,
                () -> guard.validate(order, OrderTransition.CANCEL, marketer));
    }
}
