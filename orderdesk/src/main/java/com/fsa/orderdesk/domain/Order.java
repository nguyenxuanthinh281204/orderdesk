package com.fsa.orderdesk.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Order {
    private final String id;
    private OrderStatus status;
    private final List<OrderLine> lines;

    public Order(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Order ID cannot be null or blank");
        }
        this.id = id;
        this.status = OrderStatus.PLACED;
        this.lines = new ArrayList<>();
    }

    public String id() {
        return id;
    }

    public OrderStatus status() {
        return status;
    }

    // Trả về Unmodifiable View để bảo vệ tính đóng gói
    public List<OrderLine> lines() {
        return Collections.unmodifiableList(lines);
    }

    public void addLine(OrderLine line) {
        Objects.requireNonNull(line, "Order line cannot be null");
        if (this.status != OrderStatus.PLACED) {
            throw new IllegalStateException(
                    "Cannot add line: Order is in status " + this.status + " (only allowed in PLACED)");
        }
        this.lines.add(line);
    }

    public void cancel() {
        if (!this.status.canBeCancelled()) {
            throw new IllegalStateException("Cannot cancel order in status " + this.status);
        }
        this.status = OrderStatus.CANCELLED;
    }

    public void advanceTo(OrderStatus newStatus) {
        Objects.requireNonNull(newStatus, "New status cannot be null");
        this.status = newStatus;
    }

    // Định danh đơn hàng chỉ dựa trên ID
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Order other))
            return false;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}