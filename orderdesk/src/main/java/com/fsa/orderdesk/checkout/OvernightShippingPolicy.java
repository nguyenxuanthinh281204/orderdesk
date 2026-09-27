package com.fsa.orderdesk.checkout;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import com.fsa.orderdesk.domain.Money;
import com.fsa.orderdesk.domain.Order;

public final class OvernightShippingPolicy implements ShippingPolicy {
    public static final Money FEE = Money.of("150000", "VND");

    @Override
    public Money cost(Order order) {
        return FEE;
    }

    @Override
    public Instant promise(Instant placedAt) {
        return placedAt.plus(0, ChronoUnit.DAYS);
    }
}