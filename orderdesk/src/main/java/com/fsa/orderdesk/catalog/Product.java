package com.fsa.orderdesk.catalog;

import com.fsa.orderdesk.domain.Money;
import com.fsa.orderdesk.domain.Sku;
import java.util.Objects;

public record Product(Sku sku, String name, Money price, boolean active) {
    public Product {
        Objects.requireNonNull(sku, "SKU cannot be null");
        Objects.requireNonNull(name, "Name cannot be null");
        Objects.requireNonNull(price, "Price cannot be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be blank");
        }
    }
}
