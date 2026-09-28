package com.fsa.orderdesk.catalog;

import java.io.IOException;

public class CatalogLoadException extends Exception {
    public CatalogLoadException(String message, IOException cause) {
        super(message, cause);
    }
}
