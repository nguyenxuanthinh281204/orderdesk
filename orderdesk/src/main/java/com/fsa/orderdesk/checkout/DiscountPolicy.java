package com.fsa.orderdesk.checkout;

import com.fsa.orderdesk.domain.Money;
import com.fsa.orderdesk.domain.Order;

public interface DiscountPolicy {
    Money discount(Order order);

    DiscountPolicy NONE = order -> Money.of("0", order.total().currency());
}