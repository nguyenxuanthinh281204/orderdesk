package com.fsa.orderdesk.service;

import com.fsa.orderdesk.domain.Sku;

public class UnknownSkuException extends RuntimeException {
    private final Sku sku;

    public UnknownSkuException(Sku sku) {
        super("Unknown product SKU: " + sku.value());
        this.sku = sku;
    }

    public Sku sku() {
        return sku;
    }
}
