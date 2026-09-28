package com.fsa.orderdesk.catalog;

import static org.junit.jupiter.api.Assertions.*;

import com.fsa.orderdesk.domain.Sku;
import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CatalogLoaderTest {

    private final CatalogLoader loader = new CatalogLoader();

    @Nested
    @DisplayName("Exception Translation & Boundary Tests")
    class ExceptionTranslationTests {

        @Test
        @DisplayName("Missing file wraps IOException as cause and includes path in message")
        void testMissingFileWrapsCauseAndPath() {
            Path missingPath = Path.of("non_existent_catalog_file.csv");

            CatalogLoadException ex = assertThrows(CatalogLoadException.class, () -> loader.loadFromPath(missingPath));

            assertTrue(ex.getMessage().contains(missingPath.toString()), "Message must contain the offending path");
            assertNotNull(ex.getCause(), "Exception cause must not be null");
            assertInstanceOf(IOException.class, ex.getCause(), "Cause must be an instance of IOException");
        }

        @Test
        @DisplayName("Malformed CSV row throws CatalogParseException naming the line")
        void testMalformedRowThrowsParseException() {
            String csvData = "KB-01,Keyboard,250000,true\nINVALID_ROW_HERE";
            CatalogParseException ex = assertThrows(CatalogParseException.class,
                    () -> loader.loadFromReader(new StringReader(csvData)));

            assertTrue(ex.getMessage().contains("line 2"), "Exception message must state the offending line");
        }
    }

    @Nested
    @DisplayName("Resource Leak Prevention")
    class ResourceLeakTests {

        @Test
        @DisplayName("Reader is closed when parsing throws midway through file")
        void testReaderClosesOnFailure() {
            AtomicBoolean isClosed = new AtomicBoolean(false);
            String badCsv = "KB-01,Keyboard,250000,true\nBAD_ROW";

            Reader spyReader = new FilterReader(new StringReader(badCsv)) {
                @Override
                public void close() throws IOException {
                    isClosed.set(true);
                    super.close();
                }
            };

            assertThrows(CatalogParseException.class, () -> loader.loadFromReader(spyReader));
            assertTrue(isClosed.get(), "Reader must be closed even when exception occurs during parsing");
        }
    }

    @Nested
    @DisplayName("Happy Path & Valid Loading")
    class HappyPathTests {

        @Test
        @DisplayName("Valid CSV rows parse cleanly into List<Product>")
        void testValidCatalogLoading() {
            String validCsv = "KB-01,Mechanical Keyboard,500000,true\nMS-02,Wireless Mouse,150000,false";
            List<Product> products = loader.loadFromReader(new StringReader(validCsv));

            assertEquals(2, products.size());
            assertEquals(new Sku("KB-01"), products.get(0).sku());
            assertTrue(products.get(0).active());
            assertFalse(products.get(1).active());
        }
    }
}