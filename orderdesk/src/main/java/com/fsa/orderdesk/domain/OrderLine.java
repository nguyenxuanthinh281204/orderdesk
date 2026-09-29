package com.fsa.orderdesk.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "order_line")
public class OrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "sku", length = 50, nullable = false))
    private Sku sku;

    @Column(name = "quantity", nullable = false)
    private long quantity;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPriceAmount;

    @Column(name = "currency", length = 10, nullable = false)
    private String currency;

    protected OrderLine() {} // Masapul iti JPA

    public OrderLine(Sku sku, long quantity, Money unitPrice) {
        this.sku = Objects.requireNonNull(sku, "SKU cannot be null");

        // 1. Panang-check iti quantity: masapul a dakdakkel ngem 0
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero, got: " + quantity);
        }
        this.quantity = quantity;

        Objects.requireNonNull(unitPrice, "Unit price cannot be null");

        // 2. Panang-check iti unit price: saan a mabalin ti negatibo a presio
        if (unitPrice.amount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative, got: " + unitPrice.amount());
        }

        this.unitPriceAmount = unitPrice.amount();
        this.currency = unitPrice.currency();
    }

    public Long getId() { return id; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public Sku sku() { return sku; }
    public long quantity() { return quantity; }
    public Money unitPrice() { return new Money(unitPriceAmount, currency); }

    public Money lineTotal() {
        return new Money(unitPriceAmount.multiply(BigDecimal.valueOf(quantity)), currency);
    }
}