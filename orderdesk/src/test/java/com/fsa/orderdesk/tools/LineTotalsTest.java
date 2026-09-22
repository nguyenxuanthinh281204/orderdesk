package com.fsa.orderdesk.tools;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LineTotalsTest {

    @Test
    @DisplayName("Happy path: Specification example with 2 rows")
    void testHappyPath() {
        List<String> input = List.of(
                "sku,quantity,unit_price",
                "KB-01,3,150000",
                "MS-04,1,99999");
        LineTotals.Summary summary = LineTotals.calculate(input);
        assertEquals(2, summary.lines());
        assertEquals(4, summary.units());
        assertEquals(new BigDecimal("549999"), summary.total());
    }

    @Test
    @DisplayName("Edge case: Header only produces zeros")
    void testHeaderOnly() {
        List<String> input = List.of("sku,quantity,unit_price");
        LineTotals.Summary summary = LineTotals.calculate(input);
        assertEquals(0, summary.lines());
        assertEquals(0, summary.units());
        assertEquals(BigDecimal.ZERO, summary.total());
    }

    @Test
    @DisplayName("Edge case: Values containing surrounding whitespace")
    void testWhitespaceHandling() {
        List<String> input = List.of(
                "sku,quantity,unit_price",
                "  KB-01  ,  2  ,  50000  ");
        LineTotals.Summary summary = LineTotals.calculate(input);
        assertEquals(1, summary.lines());
        assertEquals(2, summary.units());
        assertEquals(new BigDecimal("100000"), summary.total());
    }

    @Test
    @DisplayName("Edge case: Unit price is zero (allowed)")
    void testZeroUnitPrice() {
        List<String> input = List.of(
                "sku,quantity,unit_price",
                "FREE-01,5,0");
        LineTotals.Summary summary = LineTotals.calculate(input);
        assertEquals(1, summary.lines());
        assertEquals(5, summary.units());
        assertEquals(new BigDecimal("0"), summary.total());
    }

    @Test
    @DisplayName("Error rule: Quantity is 0 (must be positive)")
    void testZeroQuantityFails() {
        List<String> input = List.of(
                "sku,quantity,unit_price",
                "KB-01,0,150000");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> LineTotals.calculate(input));
        assertTrue(ex.getMessage().contains("Line 2"));
    }

    @Test
    @DisplayName("Error rule: Quantity is negative")
    void testNegativeQuantityFails() {
        List<String> input = List.of(
                "sku,quantity,unit_price",
                "KB-01,-3,150000");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> LineTotals.calculate(input));
        assertTrue(ex.getMessage().contains("Line 2"));
    }

    @Test
    @DisplayName("Error rule: Quantity is non-integer string")
    void testUnparseableQuantityFails() {
        List<String> input = List.of(
                "sku,quantity,unit_price",
                "KB-01,three,150000");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> LineTotals.calculate(input));
        assertTrue(ex.getMessage().contains("Line 2"));
    }

    @Test
    @DisplayName("Error rule: Unit price is negative")
    void testNegativeUnitPriceFails() {
        List<String> input = List.of(
                "sku,quantity,unit_price",
                "KB-01,3,-500");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> LineTotals.calculate(input));
        assertTrue(ex.getMessage().contains("Line 2"));
    }

    @Test
    @DisplayName("Error rule: Unit price is unparseable")
    void testUnparseableUnitPriceFails() {
        List<String> input = List.of(
                "sku,quantity,unit_price",
                "KB-01,3,expensive");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> LineTotals.calculate(input));
        assertTrue(ex.getMessage().contains("Line 2"));
    }

    @Test
    @DisplayName("Error rule: Fewer than three fields")
    void testFewerThanThreeFieldsFails() {
        List<String> input = List.of(
                "sku,quantity,unit_price",
                "KB-01,3");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> LineTotals.calculate(input));
        assertTrue(ex.getMessage().contains("Line 2"));
    }

    @Test
    @DisplayName("Error rule: Trailing empty field is not dropped")
    void testTrailingEmptyFieldFails() {
        List<String> input = List.of(
                "sku,quantity,unit_price",
                "KB-01,3,");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> LineTotals.calculate(input));
        assertTrue(ex.getMessage().contains("Line 2"));
    }
}