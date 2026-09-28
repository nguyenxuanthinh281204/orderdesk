package com.fsa.orderdesk.catalog;

public class CatalogParseException extends RuntimeException {
    public CatalogParseException(String message) {
        super(message);
    }

    public CatalogParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
