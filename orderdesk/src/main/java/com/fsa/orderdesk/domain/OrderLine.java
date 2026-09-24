package com.fsa.orderdesk.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record OrderLine(Sku sku, long quantity, Money unitPrice) {

    public OrderLine {
        Objects.requireNonNull(sku, "SKU cannot be null");
        Objects.requireNonNull(unitPrice, "Unit price cannot be null");

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive, got: " + quantity);
        }
        if (unitPrice.amount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative, got: " + unitPrice.amount());
        }
    }

    public Money lineTotal() {
        return unitPrice.times(quantity);
    }
}