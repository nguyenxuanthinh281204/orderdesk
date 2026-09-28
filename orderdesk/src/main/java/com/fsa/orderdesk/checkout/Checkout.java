package com.fsa.orderdesk.checkout;

import com.fsa.orderdesk.domain.Money;
import com.fsa.orderdesk.domain.Order;
import java.time.Instant;
import java.util.Objects;

public class Checkout {
    private final ShippingPolicy shippingPolicy;
    private final DiscountPolicy discountPolicy;

    public Checkout(ShippingPolicy shippingPolicy, DiscountPolicy discountPolicy) {
        this.shippingPolicy = Objects.requireNonNull(shippingPolicy, "Shipping policy cannot be null");
        this.discountPolicy = Objects.requireNonNull(discountPolicy, "Discount policy cannot be null");
    }

    public Money calculateShipping(Order order) {
        return shippingPolicy.cost(order);
    }

    public Instant promisedDate(Instant placedAt) {
        return shippingPolicy.promise(placedAt);
    }

    public Money calculateDiscount(Order order) {
        return discountPolicy.discount(order);
    }

}