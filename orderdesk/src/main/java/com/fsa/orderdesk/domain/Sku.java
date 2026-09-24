package com.fsa.orderdesk.domain;

import java.util.regex.Pattern;

public record Sku(String value) {
    private static final Pattern SKU_PATTERN = Pattern.compile("^[A-Z]{2}-\\d{2}$");

    public Sku {
        if (value == null || !SKU_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid SKU format: '" + value
                    + "'. Must match two uppercase letters, hyphen, two digits (e.g. KB-01).");
        }
    }
}
