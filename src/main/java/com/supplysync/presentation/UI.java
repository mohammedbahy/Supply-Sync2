package com.supplysync.presentation;

import com.supplysync.facade.OrderFacade;

public class UI {
    private final OrderFacade orderFacade;

    public UI(OrderFacade orderFacade) {
        this.orderFacade = orderFacade;
    }

    public void run() {
        // Entry point for future UI flow.
    }
}
