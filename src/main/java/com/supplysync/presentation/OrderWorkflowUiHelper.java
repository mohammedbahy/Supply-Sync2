package com.supplysync.presentation;

import com.supplysync.domain.order.OrderTransition;
import com.supplysync.facade.OrderFacade;
import com.supplysync.models.Order;
import com.supplysync.models.OrderStatuses;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

final class OrderWorkflowUiHelper {
    private OrderWorkflowUiHelper() {
    }

    static void rebuildWorkflowButtons(
            VBox container,
            Order order,
            OrderFacade facade,
            Runnable onSuccess
    ) {
        if (container == null || order == null || facade == null || order.getId() == null) {
            return;
        }
        container.getChildren().clear();

        Order current = facade.findOrderById(order.getId()).orElse(order);
        boolean arabic = LanguageManager.isArabic();
        Set<OrderTransition> allowed = facade.getAllowedTransitions(current);
        String status = current.getStatus();

        if (allowed.isEmpty()) {
            Label hint = new Label(noActionsHint(arabic));
            hint.getStyleClass().add("workflow-hint");
            hint.setWrapText(true);
            container.getChildren().add(hint);
            return;
        }

        if (OrderStatuses.ON_HOLD.equals(status)) {
            addOnHoldPanel(container, current, arabic);
        } else if (isDeliveryPhase(status)) {
            Label section = new Label(arabic ? "إجراءات التوصيل" : "Delivery actions");
            section.getStyleClass().add("workflow-section-title");
            container.getChildren().add(section);
        } else {
            Label section = new Label(arabic ? "إجراءات الطلب" : "Order actions");
            section.getStyleClass().add("workflow-section-title");
            container.getChildren().add(section);
        }

        List<OrderTransition> actions = transitionsToShow(allowed, status);
        final String orderId = current.getId();
        for (OrderTransition transition : actions) {
            Button btn = new Button(actionLabel(transition, current, arabic));
            applyButtonStyle(btn, transition, status);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setOnAction(e -> runTransition(facade, orderId, transition, onSuccess));
            container.getChildren().add(btn);
        }
    }

    private static void addOnHoldPanel(VBox container, Order order, boolean arabic) {
        Label banner = new Label(arabic ? "الطلب معلّق حالياً" : "Order is on hold");
        banner.getStyleClass().add("workflow-holding-banner");
        banner.setWrapText(true);
        container.getChildren().add(banner);

        String before = order.getStatusBeforeHold();
        if (before != null && !before.isBlank()) {
            String resumeHint = arabic
                    ? "قبل التعليق: " + OrderStatuses.displayLabel(before, true)
                    : "Before hold: " + OrderStatuses.displayLabel(before, false);
            Label sub = new Label(resumeHint);
            sub.getStyleClass().add("workflow-hint");
            sub.setWrapText(true);
            container.getChildren().add(sub);
        }

        Label section = new Label(arabic ? "إدارة التعليق" : "Hold management");
        section.getStyleClass().add("workflow-section-title");
        container.getChildren().add(section);
    }

    private static boolean isDeliveryPhase(String status) {
        return OrderStatuses.IN_TRANSIT.equals(status)
                || OrderStatuses.PARTIALLY_SHIPPED.equals(status);
    }

    /** Only transitions valid for the current status (prevents stale PLACE_ON_HOLD on ON_HOLD). */
    private static List<OrderTransition> transitionsToShow(Set<OrderTransition> allowed, String status) {
        List<OrderTransition> ordered = new ArrayList<>();
        if (OrderStatuses.ON_HOLD.equals(status)) {
            addIfPresent(ordered, allowed, OrderTransition.RELEASE_HOLD);
            addIfPresent(ordered, allowed, OrderTransition.CANCEL);
            return ordered;
        }
        if (isDeliveryPhase(status)) {
            addIfPresent(ordered, allowed, OrderTransition.DELIVER);
            addIfPresent(ordered, allowed, OrderTransition.PLACE_ON_HOLD);
            addIfPresent(ordered, allowed, OrderTransition.CANCEL);
            return ordered;
        }
        addIfPresent(ordered, allowed, OrderTransition.APPROVE);
        addIfPresent(ordered, allowed, OrderTransition.SHIP);
        addIfPresent(ordered, allowed, OrderTransition.SHIP_PARTIAL);
        addIfPresent(ordered, allowed, OrderTransition.PLACE_ON_HOLD);
        addIfPresent(ordered, allowed, OrderTransition.CANCEL);
        addIfPresent(ordered, allowed, OrderTransition.DELIVER);
        addIfPresent(ordered, allowed, OrderTransition.RETURN);
        for (OrderTransition t : allowed) {
            if (!ordered.contains(t)) {
                ordered.add(t);
            }
        }
        return ordered;
    }

    private static void addIfPresent(List<OrderTransition> list, Set<OrderTransition> allowed, OrderTransition t) {
        if (allowed.contains(t)) {
            list.add(t);
        }
    }

    private static String actionLabel(OrderTransition transition, Order order, boolean arabic) {
        String status = order.getStatus();
        if (OrderStatuses.ON_HOLD.equals(status) && transition == OrderTransition.RELEASE_HOLD) {
            return arabic ? "إلغاء التعليق (استئناف)" : "Release hold (resume)";
        }
        if (transition == OrderTransition.DELIVER && isDeliveryPhase(status)) {
            return arabic ? "إتمام الطلب" : "Complete order";
        }
        if (transition == OrderTransition.PLACE_ON_HOLD && isDeliveryPhase(status)) {
            return arabic ? "تعليق الطلب (أثناء التوصيل)" : "Hold order (during delivery)";
        }
        if (transition == OrderTransition.PLACE_ON_HOLD) {
            return arabic ? "تعليق الطلب" : "Place on hold";
        }
        return transition.displayLabel(arabic);
    }

    private static void applyButtonStyle(Button btn, OrderTransition transition, String status) {
        btn.getStyleClass().add("full-btn");
        if (transition == OrderTransition.CANCEL) {
            btn.getStyleClass().add("danger-link");
            return;
        }
        if (OrderStatuses.ON_HOLD.equals(status) && transition == OrderTransition.RELEASE_HOLD) {
            btn.getStyleClass().add("workflow-btn-release");
            return;
        }
        if (isDeliveryPhase(status) && transition == OrderTransition.DELIVER) {
            btn.getStyleClass().add("workflow-btn-complete");
            return;
        }
        if (isDeliveryPhase(status) && transition == OrderTransition.PLACE_ON_HOLD) {
            btn.getStyleClass().add("workflow-btn-hold");
            return;
        }
        btn.getStyleClass().add("primary-btn");
    }

    private static String noActionsHint(boolean arabic) {
        return arabic
                ? "لا توجد إجراءات متاحة لدورك في هذه الحالة."
                : "No actions available for your role in this status.";
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

    private static void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
