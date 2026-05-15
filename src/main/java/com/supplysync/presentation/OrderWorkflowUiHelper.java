package com.supplysync.presentation;

import com.supplysync.facade.OrderFacade;
import com.supplysync.models.Order;
import com.supplysync.workflow.OrderEventBus;
import com.supplysync.workflow.OrderTransition;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

final class OrderWorkflowUiHelper {
    private OrderWorkflowUiHelper() {
    }

    static void rebuildWorkflowButtons(
            VBox container,
            Order order,
            OrderFacade facade,
            Runnable onSuccess
    ) {
        if (container == null || order == null || facade == null) {
            return;
        }
        container.getChildren().clear();
        boolean arabic = LanguageManager.isArabic();
        for (OrderTransition transition : facade.getAllowedTransitions(order.getId())) {
            Button btn = new Button(transition.displayLabel(arabic));
            btn.getStyleClass().add(transition == OrderTransition.CANCEL ? "danger-link" : "primary-btn");
            if (transition != OrderTransition.CANCEL) {
                btn.getStyleClass().add("full-btn");
            }
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setOnAction(e -> runTransition(facade, order.getId(), transition, onSuccess));
            container.getChildren().add(btn);
        }
    }

    static void runTransition(OrderFacade facade, String orderId, OrderTransition transition, Runnable onSuccess) {
        try {
            facade.executeTransition(orderId, transition);
            if (onSuccess != null) {
                onSuccess.run();
            }
        } catch (Exception ex) {
            showError(
                    LanguageManager.isArabic() ? "فشل تنفيذ الإجراء" : "Transition failed",
                    ex.getMessage() != null ? ex.getMessage() : ex.toString()
            );
        }
    }

    static void subscribeRefresh(String orderId, Consumer<String> refresher, OrderEventBus.OrderChangeListener holder) {
        OrderEventBus.OrderChangeListener listener = changedId -> {
            if (orderId == null || orderId.equals(changedId)) {
                refresher.accept(changedId);
            }
        };
        OrderEventBus.getInstance().subscribe(listener);
        if (holder != null) {
            // caller stores listener for unsubscribe — use array trick in controller
        }
    }

    private static void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
