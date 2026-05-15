package com.supplysync.presentation.components;

import com.supplysync.models.Order;
import com.supplysync.models.OrderStatusHistoryEntry;
import com.supplysync.models.OrderStatuses;
import com.supplysync.domain.order.OrderTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Visual timeline for {@code order_status_history} rows.
 */
public final class OrderStatusTimelineView extends VBox {
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private OrderStatusTimelineView() {
        super(8);
        getStyleClass().add("timeline-root");
    }

    public static OrderStatusTimelineView create(List<OrderStatusHistoryEntry> history, Order order, boolean arabic) {
        OrderStatusTimelineView view = new OrderStatusTimelineView();
        if (history == null || history.isEmpty()) {
            view.getChildren().add(syntheticEntry(
                    arabic ? "تم إنشاء الطلب" : "Order placed",
                    order != null ? OrderStatuses.displayLabel(order.getStatus(), arabic) : "",
                    order != null ? order.getEffectivePlacedAt().format(TS) : "",
                    "timeline-dot-pending"
            ));
            return view;
        }
        for (int i = 0; i < history.size(); i++) {
            OrderStatusHistoryEntry e = history.get(i);
            view.getChildren().add(entryRow(e, arabic));
            if (i < history.size() - 1) {
                view.getChildren().add(new Separator());
            }
        }
        return view;
    }

    private static VBox entryRow(OrderStatusHistoryEntry e, boolean arabic) {
        String from = e.getFromStatus() != null
                ? OrderStatuses.displayLabel(e.getFromStatus(), arabic)
                : (arabic ? "—" : "—");
        String to = OrderStatuses.displayLabel(e.getToStatus(), arabic);
        String transition = formatTransition(e.getTransitionName(), arabic);
        String actor = e.getActorName() != null ? e.getActorName() : "System";
        String when = e.getCreatedAt() != null ? e.getCreatedAt().format(TS) : "";

        Label dot = new Label("●");
        dot.getStyleClass().add(timelineDotClass(e.getToStatus()));

        Label arrow = new Label("→");
        arrow.getStyleClass().add("timeline-arrow");

        Label statusLine = new Label(from + "  " + arrow.getText() + "  " + to);
        statusLine.getStyleClass().add("timeline-status");

        Label meta = new Label(transition + " · " + actor + " · " + when);
        meta.getStyleClass().add("timeline-meta");

        HBox top = new HBox(10, dot, statusLine);
        top.setAlignment(Pos.CENTER_LEFT);

        VBox block = new VBox(4, top, meta);
        block.getStyleClass().add("timeline-entry");
        HBox.setHgrow(block, Priority.ALWAYS);
        return block;
    }

    private static VBox syntheticEntry(String title, String status, String when, String dotClass) {
        Label dot = new Label("●");
        dot.getStyleClass().add(dotClass);
        Label statusLine = new Label(title + " → " + status);
        statusLine.getStyleClass().add("timeline-status");
        Label meta = new Label(when);
        meta.getStyleClass().add("timeline-meta");
        HBox top = new HBox(10, dot, statusLine);
        top.setAlignment(Pos.CENTER_LEFT);
        return new VBox(4, top, meta);
    }

    private static String formatTransition(String name, boolean arabic) {
        if (name == null) {
            return "";
        }
        try {
            return OrderTransition.valueOf(name).displayLabel(arabic);
        } catch (IllegalArgumentException ex) {
            if ("ORDER_PLACED".equals(name)) {
                return arabic ? "طلب جديد" : "Order placed";
            }
            return name.replace('_', ' ');
        }
    }

    private static String timelineDotClass(String status) {
        if (status == null) {
            return "timeline-dot-pending";
        }
        switch (OrderStatuses.normalize(status)) {
            case OrderStatuses.DELIVERED:
                return "timeline-dot-delivered";
            case OrderStatuses.CANCELLED:
                return "timeline-dot-cancelled";
            case OrderStatuses.ON_HOLD:
                return "timeline-dot-hold";
            case OrderStatuses.PARTIALLY_SHIPPED:
                return "timeline-dot-partial";
            case OrderStatuses.IN_TRANSIT:
                return "timeline-dot-transit";
            default:
                return "timeline-dot-pending";
        }
    }
}
