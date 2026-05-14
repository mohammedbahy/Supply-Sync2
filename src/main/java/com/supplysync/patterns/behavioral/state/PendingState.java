package com.supplysync.patterns.behavioral.state;

public class PendingState implements OrderState {
    @Override
    public String name() {
        return "PENDING";
    }
}
