package com.supplysync.domain.order;

import com.supplysync.models.Marketer;
import com.supplysync.models.Order;
import com.supplysync.models.OrderStatuses;
import com.supplysync.models.Product;
import com.supplysync.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderStateMachineTest {
    private OrderStateMachine machine;
    private User admin;
    private User marketer;

    @BeforeEach
    void setUp() {
        machine = new OrderStateMachine();
        admin = new User("a1", "admin@test.com", "x", "Admin", "ADMIN");
        marketer = new User("m1", "m@test.com", "x", "Marketer One", "MARKETER");
    }

    @Test
    void approve_movesAwaitingApprovalToApproved() {
        Order order = sampleOrder(OrderStatuses.AWAITING_APPROVAL);
        machine.applyTransition(order, OrderTransition.APPROVE);
        assertEquals(OrderStatuses.APPROVED, order.getStatus());
    }

    @Test
    void ship_movesApprovedToInTransit() {
        Order order = sampleOrder(OrderStatuses.APPROVED);
        machine.applyTransition(order, OrderTransition.SHIP);
        assertEquals(OrderStatuses.IN_TRANSIT, order.getStatus());
    }

    @Test
    void marketerCannotApprove() {
        Order order = sampleOrder(OrderStatuses.AWAITING_APPROVAL);
        Set<OrderTransition> allowed = machine.getAllowedTransitions(order, marketer);
        assertTrue(allowed.isEmpty() || !allowed.contains(OrderTransition.APPROVE));
    }

    @Test
    void illegalTransitionThrows() {
        Order order = sampleOrder(OrderStatuses.DELIVERED);
        assertThrows(IllegalStateException.class,
                () -> machine.applyTransition(order, OrderTransition.APPROVE));
    }

    private static Order sampleOrder(String status) {
        Order order = new Order("ORD-TEST", new Marketer("m1", "Marketer"));
        order.getProducts().add(new Product("p1", "Item", 10, "Cat", 100.0, ""));
        order.setTotalAmount(100.0);
        OrderStatusHydrator.hydrate(order, status);
        return order;
    }
}
