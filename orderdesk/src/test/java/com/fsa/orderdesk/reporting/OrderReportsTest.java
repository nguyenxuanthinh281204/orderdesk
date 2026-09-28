package com.fsa.orderdesk.reporting;

import static org.junit.jupiter.api.Assertions.*;

import com.fsa.orderdesk.catalog.Product;
import com.fsa.orderdesk.domain.Money;
import com.fsa.orderdesk.domain.Order;
import com.fsa.orderdesk.domain.OrderLine;
import com.fsa.orderdesk.domain.OrderStatus;
import com.fsa.orderdesk.domain.Sku;
import java.time.Instant;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class OrderReportsTest {

    private Order createOrder(String id, String customerId, Instant placedAt, OrderStatus status, long qty,
            String price) {
        Order order = new Order(id, customerId, placedAt);
        order.addLine(new OrderLine(new Sku("KB-01"), qty, Money.of(price, "VND")));
        if (status == OrderStatus.CANCELLED) {
            order.cancel();
        } else if (status != OrderStatus.PLACED) {
            order.advanceTo(status);
        }
        return order;
    }

    @Nested
    @DisplayName("Catalog Index and Duplicate SKU Merge Tests")
    class CatalogIndexTests {

        @Test
        @DisplayName("Duplicate SKU resolves using replacement policy")
        void testDuplicateSkuMerge() {
            Sku sku = new Sku("KB-01");
            Product oldProd = new Product(sku, "Old Keyboard", Money.of("100000", "VND"), true);
            Product newProd = new Product(sku, "New Keyboard", Money.of("150000", "VND"), true);

            Map<Sku, Product> index = OrderReports.buildCatalogIndex(List.of(oldProd, newProd));
            assertEquals(1, index.size());
            assertEquals("New Keyboard", index.get(sku).name());
        }
    }

    @Nested
    @DisplayName("Six Stream Operations Tests")
    class StreamOperationsTests {

        @Test
        @DisplayName("1. Total units across orders")
        void testTotalUnits() {
            Order o1 = createOrder("O1", "C1", Instant.now(), OrderStatus.PLACED, 5, "10000");
            Order o2 = createOrder("O2", "C2", Instant.now(), OrderStatus.PLACED, 3, "20000");
            assertEquals(8, OrderReports.totalUnits(List.of(o1, o2)));
        }

        @Test
        @DisplayName("2 & 3. Group and Count per status in single pass")
        void testGroupingAndCounting() {
            Order o1 = createOrder("O1", "C1", Instant.now(), OrderStatus.PLACED, 1, "1000");
            Order o2 = createOrder("O2", "C2", Instant.now(), OrderStatus.DELIVERED, 1, "1000");
            Order o3 = createOrder("O3", "C3", Instant.now(), OrderStatus.PLACED, 1, "1000");

            Map<OrderStatus, List<Order>> grouped = OrderReports.ordersByStatus(List.of(o1, o2, o3));
            assertEquals(2, grouped.get(OrderStatus.PLACED).size());
            assertEquals(1, grouped.get(OrderStatus.DELIVERED).size());

            Map<OrderStatus, Long> counts = OrderReports.countPerStatus(List.of(o1, o2, o3));
            assertEquals(2L, counts.get(OrderStatus.PLACED));
            assertEquals(1L, counts.get(OrderStatus.DELIVERED));
        }

        @Test
        @DisplayName("4. Distinct SKUs appearing in orders")
        void testDistinctSkus() {
            Order o1 = new Order("O1");
            o1.addLine(new OrderLine(new Sku("KB-01"), 1, Money.of("100", "VND")));
            o1.addLine(new OrderLine(new Sku("MS-02"), 1, Money.of("100", "VND")));

            Order o2 = new Order("O2");
            o2.addLine(new OrderLine(new Sku("KB-01"), 2, Money.of("100", "VND")));

            Set<Sku> skus = OrderReports.distinctSkus(List.of(o1, o2));
            assertEquals(Set.of(new Sku("KB-01"), new Sku("MS-02")), skus);
        }

        @Test
        @DisplayName("5. Sorted orders by status, date desc, tie-break by id")
        void testOrderSortingWithTieBreak() {
            Instant t1 = Instant.parse("2026-09-28T10:00:00Z");
            Instant t2 = Instant.parse("2026-09-28T12:00:00Z");

            Order o1 = createOrder("ORD-A", "C1", t1, OrderStatus.PLACED, 1, "100");
            Order o2 = createOrder("ORD-B", "C2", t2, OrderStatus.PLACED, 1, "100");
            Order o3 = createOrder("ORD-C", "C3", t1, OrderStatus.PLACED, 1, "100"); // Same status & date as ORD-A

            List<Order> sorted = OrderReports.sortOrdersByStatusAndDate(List.of(o1, o2, o3));

            assertEquals("ORD-B", sorted.get(0).id()); // Newest date first
            assertEquals("ORD-A", sorted.get(1).id()); // Tie-break ORD-A before ORD-C
            assertEquals("ORD-C", sorted.get(2).id());
        }

        @Test
        @DisplayName("6. Top customers by LTV excluding cancelled, with tie-break")
        void testTopCustomersExcludingCancelled() {
            Instant now = Instant.now();
            Order o1 = createOrder("O1", "CUST-A", now, OrderStatus.DELIVERED, 1, "300000");
            Order o2 = createOrder("O2", "CUST-B", now, OrderStatus.DELIVERED, 1, "300000"); // Same LTV as A
            Order o3 = createOrder("O3", "CUST-C", now, OrderStatus.CANCELLED, 1, "999999"); // Cancelled, excluded

            List<Map.Entry<String, Money>> top = OrderReports.topCustomers(List.of(o1, o2, o3), 2);

            assertEquals(2, top.size());
            assertEquals("CUST-A", top.get(0).getKey()); // Tie-break alphabetical
            assertEquals("CUST-B", top.get(1).getKey());
            assertEquals(Money.of("300000", "VND"), top.get(0).getValue());
        }
    }

    @Nested
    @DisplayName("Hash Contract & Performance Tests")
    class HashContractAndPerformanceTests {

        @Test
        @DisplayName("Mutating field in hashCode breaks HashSet.contains")
        void testHashSetMutationBreaksContains() {
            Set<Order> set = new HashSet<>();
            Order order = new Order("ORD-HASH");
            set.add(order);

            assertTrue(set.contains(order));

            boolean foundBroken = false;
            for (int i = 1; i <= 20; i++) {
                order.setTag("TAG_" + i);
                if (!set.contains(order)) {
                    foundBroken = true;
                    break;
                }
            }

            assertTrue(foundBroken,
                    "HashSet must fail to find the element once its hashCode-contributing field is mutated");
            assertFalse(set.contains(order), "Contains must return false after mutating hash-contributing field");
        }

        @Test
        @DisplayName("Compare costs: List.contains vs HashSet.contains on 50,000 items")
        void testTimingComparison() {
            int n = 50_000;
            List<String> list = new ArrayList<>(n);
            Set<String> set = new HashSet<>(n);

            for (int i = 0; i < n; i++) {
                String val = "SKU-" + i;
                list.add(val);
                set.add(val);
            }

            long startList = System.nanoTime();
            list.contains("SKU-49999");
            long endList = System.nanoTime();

            long startSet = System.nanoTime();
            set.contains("SKU-49999");
            long endSet = System.nanoTime();

            long listDurationNs = endList - startList;
            long setDurationNs = endSet - startSet;

            assertTrue(setDurationNs <= listDurationNs);
        }

        @Test
        @DisplayName("Safe removal with removeIf vs ConcurrentModificationException")
        void testSafeRemoval() {
            List<Order> orders = new ArrayList<>(List.of(
                    createOrder("O1", "C1", Instant.now(), OrderStatus.CANCELLED, 1, "100"),
                    createOrder("O2", "C2", Instant.now(), OrderStatus.PLACED, 1, "100"),
                    createOrder("O3", "C3", Instant.now(), OrderStatus.PLACED, 1, "100")));

            assertThrows(ConcurrentModificationException.class, () -> {
                for (Order o : orders) {
                    if (o.status() == OrderStatus.CANCELLED) {
                        orders.remove(o);
                    }
                }
            });

            OrderReports.dropCancelledOrders(orders);
            assertEquals(2, orders.size());
            assertTrue(orders.stream().allMatch(o -> o.status() == OrderStatus.PLACED));
        }
    }
}
