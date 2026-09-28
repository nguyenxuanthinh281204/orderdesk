package com.fsa.orderdesk.checkout;

import com.fsa.orderdesk.domain.Money;
import com.fsa.orderdesk.domain.Order;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public final class PickupShippingPolicy implements ShippingPolicy {
    public static final Money FEE = Money.of("0", "VND");

    @Override
    public Money cost(Order order) {
        return FEE;
    }

    @Override
    public Instant promise(Instant placedAt) {
        return placedAt.plus(2, ChronoUnit.DAYS);
    }
}