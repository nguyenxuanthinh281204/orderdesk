package com.fsa.orderdesk.checkout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fsa.orderdesk.domain.Money;
import com.fsa.orderdesk.domain.Order;
import com.fsa.orderdesk.domain.OrderLine;
import com.fsa.orderdesk.domain.Sku;

public class CheckoutTest {
    private Order createOrderWithValue(String amount) {
        Order order = new Order("ORD-TEST");
        order.addLine(new OrderLine(new Sku("KB-01"), 1, Money.of(amount, "VND")));
        return order;
    }

    @Test
    @DisplayName("Substitution test: Interchangeable policies with identical test assertions")
    void testPolicySubstitution() {
        Order order = createOrderWithValue("100000");
        Instant now = Instant.parse("2026-09-27T10:00:00Z");

        // Standard
        Checkout standardCheckout = new Checkout(new StandardShippingPolicy(), DiscountPolicy.NONE);
        assertEquals(Money.of("25000", "VND"), standardCheckout.calculateShipping(order));
        assertEquals(now.plus(5, ChronoUnit.DAYS), standardCheckout.promisedDate(now));

        // Express
        Checkout expressCheckout = new Checkout(new ExpressShippingPolicy(), DiscountPolicy.NONE);
        assertEquals(Money.of("80000", "VND"), expressCheckout.calculateShipping(order));
        assertEquals(now.plus(1, ChronoUnit.DAYS), expressCheckout.promisedDate(now));

        // Pickup
        Checkout pickupCheckout = new Checkout(new PickupShippingPolicy(), DiscountPolicy.NONE);
        assertEquals(Money.of("0", "VND"), pickupCheckout.calculateShipping(order));
        assertEquals(now.plus(2, ChronoUnit.DAYS), pickupCheckout.promisedDate(now));

        // Overnight
        Checkout overnightCheckout = new Checkout(new OvernightShippingPolicy(), DiscountPolicy.NONE);
        assertEquals(Money.of("150000", "VND"), overnightCheckout.calculateShipping(order));
        assertEquals(now.plus(0, ChronoUnit.DAYS), overnightCheckout.promisedDate(now));
    }

    @Test
    @DisplayName("Boundary test: Standard shipping free strictly over 500,000 VND")
    void testStandardShippingBoundary() {
        Checkout checkout = new Checkout(new StandardShippingPolicy(), DiscountPolicy.NONE);

        Order at499k = createOrderWithValue("499999");
        assertEquals(Money.of("25000", "VND"), checkout.calculateShipping(at499k));

        Order at500k = createOrderWithValue("500000");
        assertEquals(Money.of("25000", "VND"), checkout.calculateShipping(at500k));

        Order at501k = createOrderWithValue("500001");
        assertEquals(Money.of("0", "VND"), checkout.calculateShipping(at501k));
    }

    @Test
    @DisplayName("No-op Discount: passing NONE avoids null and computes zero discount")
    void testDiscountPolicyNone() {
        Order order = createOrderWithValue("200000");
        Checkout checkout = new Checkout(new StandardShippingPolicy(), DiscountPolicy.NONE);

        assertEquals(Money.of("0", "VND"), checkout.calculateDiscount(order));
        assertThrows(NullPointerException.class, () -> new Checkout(new StandardShippingPolicy(), null));
    }
}
