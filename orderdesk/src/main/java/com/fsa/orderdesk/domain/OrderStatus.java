package com.fsa.orderdesk.domain;

public enum OrderStatus {
    PLACED("PLACED"),
    PICKING("PICKING"),
    DISPATCHED("DISPATCHED"),
    DELIVERED("DELIVERED"),
    CANCELLED("CANCELLED");

    private final String dbValue;

    OrderStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String dbValue() {
        return dbValue;
    }

    public static OrderStatus fromDb(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("Database status value cannot be null");
        }
        for (OrderStatus status : values()) {
            if (status.dbValue.equalsIgnoreCase(raw.trim())) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown order status in database: '" + raw + "'");
    }

    public boolean canBeCancelled() {
        return this == PLACED || this == PICKING;
    }
}