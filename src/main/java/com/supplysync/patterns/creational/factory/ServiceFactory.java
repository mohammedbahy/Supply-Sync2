package com.supplysync.patterns.creational.factory;

import com.supplysync.patterns.structural.adapter.DeliveryAdapter;
import com.supplysync.patterns.structural.adapter.MockDeliveryGateway;
import com.supplysync.services.delivery.DeliveryService;
import com.supplysync.services.inventory.DefaultInventoryService;
import com.supplysync.services.inventory.InventoryService;
import com.supplysync.patterns.structural.decorator.AuditNotificationDecorator;
import com.supplysync.services.notification.DefaultNotificationService;
import com.supplysync.services.notification.NotificationService;
import com.supplysync.services.order.DefaultOrderService;
import com.supplysync.services.order.OrderService;
import com.supplysync.services.auth.AuthService;
import com.supplysync.services.auth.DefaultAuthService;

import com.supplysync.repository.InMemoryStorage;
import com.supplysync.repository.SqliteStorage;
import com.supplysync.repository.Storage;

import java.nio.file.Paths;

public final class ServiceFactory {
    private static final Storage storage = createStorage();
    private static final AuthService authService = new DefaultAuthService(storage);
    
    private ServiceFactory() {
    }

    private static Storage createStorage() {
        String env = System.getenv("SUPPLYSYNC_DB");
        String path = (env != null && !env.isBlank())
                ? env.trim()
                : Paths.get(System.getProperty("user.dir"), "database.db").toAbsolutePath().toString();
        try {
            return new SqliteStorage(path);
        } catch (IllegalStateException ex) {
            System.err.println("SupplySync: SQLite unavailable at " + path + ", using in-memory storage. (" + ex.getMessage() + ")");
            return new InMemoryStorage();
        }
    }

    public static AuthService createAuthService() {
        return authService;
    }

    public static OrderService createOrderService() {
        return new DefaultOrderService(storage);
    }

    public static InventoryService createInventoryService() {
        return new DefaultInventoryService(storage);
    }

    public static DeliveryService createDeliveryService() {
        return new DeliveryAdapter(new MockDeliveryGateway());
    }

    public static NotificationService createNotificationService() {
        NotificationService baseNotification = new DefaultNotificationService();
        return new AuditNotificationDecorator(baseNotification, storage);
    }
    
    public static Storage getStorage() {
        return storage;
    }
}
