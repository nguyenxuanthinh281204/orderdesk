package com.fsa.orderdesk.checkout;

import com.fsa.orderdesk.domain.Money;
import com.fsa.orderdesk.domain.Order;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public final class StandardShippingPolicy implements ShippingPolicy {
    public static final BigDecimal FREE_THRESHOLD = new BigDecimal("500000");
    public static final Money FEE = Money.of("25000", "VND");
    public static final Money FREE = Money.of("0", "VND");

    @Override
    public Money cost(Order order) {
        if (order.total().amount().compareTo(FREE_THRESHOLD) > 0) {
            return FREE;
        }
        return FEE;
    }

    @Override
    public Instant promise(Instant placedAt) {
        return placedAt.plus(5, ChronoUnit.DAYS);
    }
}