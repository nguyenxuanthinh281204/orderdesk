package com.fsa.orderdesk.checkout;

import com.fsa.orderdesk.domain.Money;
import com.fsa.orderdesk.domain.Order;
import java.math.BigDecimal;

public class Checkout {
    public Money shippingCost(Order order, String tier) {
        if ("Standard".equalsIgnoreCase(tier)) {
            if (order.total().amount().compareTo(new BigDecimal("500000")) > 0) {
                return Money.of("0", "VND");
            }
            return Money.of("25000", "VND");
        } else if ("Express".equalsIgnoreCase(tier)) {
            return Money.of("80000", "VND");
        } else if ("Pickup".equalsIgnoreCase(tier)) {
            return Money.of("0", "VND");
        } else {
            throw new IllegalArgumentException("Unknown tier: " + tier);
        }
    }
}