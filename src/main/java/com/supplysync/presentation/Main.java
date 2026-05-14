package com.supplysync.presentation;

import com.supplysync.facade.OrderFacade;
import com.supplysync.patterns.creational.factory.ServiceFactory;

public class Main {
    public static void main(String[] args) {
        OrderFacade orderFacade = new OrderFacade(
                ServiceFactory.createOrderService(),
                ServiceFactory.createInventoryService(),
                ServiceFactory.createDeliveryService(),
                ServiceFactory.createNotificationService(),
                ServiceFactory.createAuthService(),
                ServiceFactory.getStorage()
        );
        UI ui = new UI(orderFacade);
        ui.run();
    }
}
