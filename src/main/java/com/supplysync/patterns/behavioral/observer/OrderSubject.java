package com.supplysync.patterns.behavioral.observer;

import com.supplysync.models.Order;

import java.util.ArrayList;
import java.util.List;

public class OrderSubject {
    private final List<OrderObserver> observers = new ArrayList<>();

    public void attach(OrderObserver observer) {
        addObserver(observer);
    }

    public void detach(OrderObserver observer) {
        removeObserver(observer);
    }

    public void addObserver(OrderObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(OrderObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(Order order) {
        for (OrderObserver observer : observers) {
            observer.onOrderUpdated(order);
        }
    }
}
