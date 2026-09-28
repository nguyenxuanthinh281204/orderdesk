package com.fsa.orderdesk.checkout;

import com.fsa.orderdesk.domain.Money;
import com.fsa.orderdesk.domain.Order;
import java.time.Instant;

public interface ShippingPolicy {
    Money cost(Order order);

    Instant promise(Instant placedAt);
}