package com.fsa.orderdesk.service;

import static org.junit.jupiter.api.Assertions.*;

import com.fsa.orderdesk.catalog.Product;
import com.fsa.orderdesk.domain.Money;
import com.fsa.orderdesk.domain.Order;
import com.fsa.orderdesk.domain.OrderLine;
import com.fsa.orderdesk.domain.Sku;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class OrderServiceTest {

    private OrderRepository repository;
    private OrderService service;
    private final Sku activeSku = new Sku("KB-01");
    private final Sku inactiveSku = new Sku("MS-02");
    private final Sku unknownSku = new Sku("XX-99");

    @BeforeEach
    void setUp() {
        List<Product> catalog = List.of(
                new Product(activeSku, "Active Keyboard", Money.of("200000", "VND"), true),
                new Product(inactiveSku, "Discontinued Mouse", Money.of("50000", "VND"), false));

        repository = new InMemoryOrderRepository();
        service = new OrderService(catalog, repository);
    }

    @Nested
    @DisplayName("Catalog Validation Rules: Happy path, Edge cases, and Failures")
    class ValidationRules {

        @Test
        @DisplayName("Happy path: Order with active product is placed and saved")
        void testPlaceActiveProductSuccess() {
            Order order = new Order("ORD-101");
            order.addLine(new OrderLine(activeSku, 2, Money.of("200000", "VND")));

            assertDoesNotThrow(() -> service.place(order));
            assertTrue(repository.findById("ORD-101").isPresent());
        }

        @Test
        @DisplayName("Failure: Unknown SKU throws UnknownSkuException carrying the SKU")
        void testUnknownSkuFailure() {
            Order order = new Order("ORD-102");
            order.addLine(new OrderLine(unknownSku, 1, Money.of("100000", "VND")));

            UnknownSkuException ex = assertThrows(UnknownSkuException.class, () -> service.place(order));
            assertEquals(unknownSku, ex.sku());
            assertTrue(ex.getMessage().contains(unknownSku.value()));
        }

        @Test
        @DisplayName("Failure: Inactive product throws InactiveProductException carrying the SKU")
        void testInactiveProductFailure() {
            Order order = new Order("ORD-103");
            order.addLine(new OrderLine(inactiveSku, 1, Money.of("50000", "VND")));

            InactiveProductException ex = assertThrows(InactiveProductException.class, () -> service.place(order));
            assertEquals(inactiveSku, ex.sku());
            assertTrue(ex.getMessage().contains(inactiveSku.value()));
        }
    }

    @Nested
    @DisplayName("Parameterized Boundary Tests for Quantities and Prices")
    class ParameterizedBoundaryTests {

        @ParameterizedTest(name = "Quantity {0} should be valid={1}")
        @CsvSource({
                "1, true",
                "100, true",
                "0, false",
                "-1, false"
        })
        @DisplayName("Boundary check for OrderLine quantities")
        void testQuantityBoundaries(long quantity, boolean shouldBeValid) {
            Money price = Money.of("100000", "VND");
            if (shouldBeValid) {
                assertDoesNotThrow(() -> new OrderLine(activeSku, quantity, price));
            } else {
                assertThrows(IllegalArgumentException.class, () -> new OrderLine(activeSku, quantity, price));
            }
        }
    }
}