package com.fsa.orderdesk.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @Column(name = "id", length = 50)
    private String id;

    @Column(name = "customer_id", length = 50, nullable = false)
    private String customerId;

    @Column(name = "placed_at", nullable = false)
    private Instant placedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private OrderStatus status;

    @Column(name = "tag", length = 50)
    private String tag;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderLine> lines = new ArrayList<>();

    protected Order() {} // Bắt buộc cho JPA

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
        this.tag = "INIT";
    }

    public String id() { return id; }
    public String customerId() { return customerId; }
    public Instant placedAt() { return placedAt; }
    public OrderStatus status() { return status; }
    public String tag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    public List<OrderLine> lines() {
        return Collections.unmodifiableList(lines);
    }

    // Helper method đồng bộ 2 chiều mối quan hệ
    public void addLine(OrderLine line) {
        Objects.requireNonNull(line, "Order line cannot be null");
        if (this.status != OrderStatus.PLACED) {
            throw new IllegalStateException("Cannot add line: Order is in status " + this.status);
        }
        this.lines.add(line);
        line.setOrder(this); // Thiết lập Owning Side
    }

    public void cancel() {
        if (!this.status.canBeCancelled()) {
            throw new IllegalStateException("Cannot cancel order in status " + this.status);
        }
        this.status = OrderStatus.CANCELLED;
    }

    public void advanceTo(OrderStatus newStatus) {
        this.status = Objects.requireNonNull(newStatus);
    }

    public Money total() {
        if (lines.isEmpty()) {
            return Money.of("0", "VND");
        }
        String curr = lines.get(0).unitPrice().currency();
        Money sum = Money.of("0", curr);
        for (OrderLine line : lines) {
            sum = sum.plus(line.lineTotal());
        }
        return sum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order other)) return false;
        return Objects.equals(this.id, other.id) && Objects.equals(this.tag, other.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, tag);
    }
}