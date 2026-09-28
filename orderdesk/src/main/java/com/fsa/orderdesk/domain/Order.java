package com.fsa.orderdesk.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Order {
    private final String id;
    private final String customerId;
    private final Instant placedAt;
    private OrderStatus status;
    private final List<OrderLine> lines;
    private String tag;

    public Order(String id) {
        this(id, "CUST-DEFAULT", Instant.now());
    }

    public Order(String id, String customerId, Instant placedAt) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Order ID cannot be null or blank");
        }
        this.id = id;
        this.customerId = Objects.requireNonNullElse(customerId, "CUST-DEFAULT");
        this.placedAt = Objects.requireNonNullElse(placedAt, Instant.now());
        this.status = OrderStatus.PLACED;
        this.lines = new ArrayList<>();
        this.tag = "INIT";
    }

    public String id() {
        return id;
    }

    public String customerId() {
        return customerId;
    }

    public Instant placedAt() {
        return placedAt;
    }

    public OrderStatus status() {
        return status;
    }

    public String tag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Order other))
            return false;
        return Objects.equals(this.id, other.id) && Objects.equals(this.tag, other.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, tag);
    }

    public Money total() {
        if (lines.isEmpty()) {
            return Money.of("0", "VND");
        }
        String currency = lines.get(0).unitPrice().currency();
        Money sum = Money.of("0", currency);
        for (OrderLine line : lines) {
            sum = sum.plus(line.lineTotal());
        }
        return sum;
    }
}