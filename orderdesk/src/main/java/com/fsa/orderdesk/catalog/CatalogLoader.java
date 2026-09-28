package com.fsa.orderdesk.catalog;

import com.fsa.orderdesk.domain.Money;
import com.fsa.orderdesk.domain.Sku;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CatalogLoader {

    public List<Product> loadFromPath(Path path) throws CatalogLoadException {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            return loadFromReader(reader);
        } catch (NoSuchFileException | FileNotFoundException e) {
            throw new CatalogLoadException("Catalog file not found at: " + path, e);
        } catch (IOException e) {
            throw new CatalogLoadException("Unreadable catalog file at: " + path, e);
        }
    }

    public List<Product> loadFromReader(Reader rawReader) {
        List<Product> products = new ArrayList<>();
        try (BufferedReader reader = (rawReader instanceof BufferedReader br) ? br : new BufferedReader(rawReader)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }

                String[] parts = trimmed.split(",", -1);
                if (parts.length != 4) {
                    throw new CatalogParseException(
                            "Malformed row at line " + lineNumber + ": expected 4 columns, got " + parts.length);
                }

                try {
                    Sku sku = new Sku(parts[0].trim());
                    String name = parts[1].trim();
                    Money price = Money.of(parts[2].trim(), "VND");
                    boolean active = Boolean.parseBoolean(parts[3].trim());
                    products.add(new Product(sku, name, price, active));
                } catch (Exception e) {
                    throw new CatalogParseException("Malformed data at line " + lineNumber + ": " + e.getMessage(), e);
                }
            }
        } catch (IOException e) {
            throw new CatalogParseException("I/O error while reading stream: " + e.getMessage(), e);
        }
        return products;
    }
}
