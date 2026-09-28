package com.fsa.orderdesk.service;

import com.fsa.orderdesk.domain.Sku;

public class InactiveProductException extends RuntimeException {
    private final Sku sku;

    public InactiveProductException(Sku sku) {
        super("Product is inactive for SKU: " + sku.value());
        this.sku = sku;
    }

    public Sku sku() {
        return sku;
    }
}