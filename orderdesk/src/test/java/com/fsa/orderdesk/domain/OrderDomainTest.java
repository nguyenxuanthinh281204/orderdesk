package com.fsa.orderdesk.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderDomainTest {

    @Test
    @DisplayName("Sku: Accepts valid pattern and rejects malformed inputs")
    void testSkuValidation() {
        assertDoesNotThrow(() -> new Sku("KB-01"));

        assertThrows(IllegalArgumentException.class, () -> new Sku("kb-01")); // lowercase
        assertThrows(IllegalArgumentException.class, () -> new Sku("KBA-01")); // 3 letters
        assertThrows(IllegalArgumentException.class, () -> new Sku("KB-1")); // 1 digit
        assertThrows(IllegalArgumentException.class, () -> new Sku("")); // empty
    }

    @Test
    @DisplayName("Money: Plus adds same currency and rejects mismatch with both currency names")
    void testMoneyPlus() {
        Money m1 = Money.of("100.50", "VND");
        Money m2 = Money.of("50.00", "VND");
        assertEquals(Money.of("150.50", "VND"), m1.plus(m2));

        Money usd = Money.of("10.00", "USD");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> m1.plus(usd));
        assertTrue(ex.getMessage().contains("USD") && ex.getMessage().contains("VND"));
    }

    @Test
    @DisplayName("OrderStatus: fromDb parses known and throws on unknown")
    void testOrderStatusFromDb() {
        assertEquals(OrderStatus.PLACED, OrderStatus.fromDb("PLACED"));
        assertEquals(OrderStatus.PICKING, OrderStatus.fromDb("picking"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> OrderStatus.fromDb("UNKNOWN"));
        assertTrue(ex.getMessage().contains("UNKNOWN"));
    }

    @Test
    @DisplayName("OrderLine: Validates quantity and price")
    void testOrderLineValidation() {
        Sku sku = new Sku("MS-04");
        Money price = Money.of("100000", "VND");

        assertThrows(IllegalArgumentException.class, () -> new OrderLine(sku, 0, price));
        assertThrows(IllegalArgumentException.class, () -> new OrderLine(sku, -2, price));
        assertThrows(IllegalArgumentException.class, () -> new OrderLine(sku, 1, Money.of("-500", "VND")));

        OrderLine line = new OrderLine(sku, 3, price);
        assertEquals(Money.of("300000", "VND"), line.lineTotal());
    }

    @Test
    @DisplayName("Order: addLine only permitted in PLACED status")
    void testOrderAddLineStateRule() {
        Order order = new Order("ORD-001");
        OrderLine line = new OrderLine(new Sku("KB-01"), 1, Money.of("150000", "VND"));

        assertDoesNotThrow(() -> order.addLine(line));

        order.advanceTo(OrderStatus.PICKING);
        assertThrows(IllegalStateException.class, () -> order.addLine(line));
    }

    @Test
    @DisplayName("Order: cancel allowed only in PLACED or PICKING")
    void testOrderCancellationRules() {
        Order ord1 = new Order("ORD-001");
        ord1.cancel();
        assertEquals(OrderStatus.CANCELLED, ord1.status());

        Order ord2 = new Order("ORD-002");
        ord2.advanceTo(OrderStatus.PICKING);
        ord2.cancel();
        assertEquals(OrderStatus.CANCELLED, ord2.status());

        Order ord3 = new Order("ORD-003");
        ord3.advanceTo(OrderStatus.DISPATCHED);
        assertThrows(IllegalStateException.class, ord3::cancel);
    }

    @Test
    @DisplayName("Encapsulation: mutating lines() view throws UnsupportedOperationException")
    void testLinesImmutability() {
        Order order = new Order("ORD-001");
        OrderLine line = new OrderLine(new Sku("KB-01"), 1, Money.of("100", "VND"));
        order.addLine(line);

        List<OrderLine> exposedLines = order.lines();
        assertThrows(UnsupportedOperationException.class, () -> exposedLines.add(line));
        assertEquals(1, order.lines().size());
    }

    @Test
    @DisplayName("Hash Contract: HashSet finds equal order with different instance")
    void testOrderHashContractInHashSet() {
        Order order1 = new Order("ORD-999");
        order1.addLine(new OrderLine(new Sku("KB-01"), 2, Money.of("50", "VND")));

        Set<Order> orderSet = new HashSet<>();
        orderSet.add(order1);

        Order order2 = new Order("ORD-999"); // Khác instance, không có lines nhưng cùng ID

        assertEquals(order1, order2);
        assertTrue(orderSet.contains(order2), "HashSet must find the order by ID equality and matching hashCode");
    }
}