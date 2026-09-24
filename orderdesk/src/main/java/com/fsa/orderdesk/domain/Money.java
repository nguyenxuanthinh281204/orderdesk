package com.fsa.orderdesk.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record Money(BigDecimal amount, String currency) implements Comparable<Money> {

    public Money {
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");
        if (currency.isBlank()) {
            throw new IllegalArgumentException("Currency cannot be blank");
        }
    }

    public static Money of(String amount, String currency) {
        return new Money(new BigDecimal(amount), currency);
    }

    public Money plus(Money other) {
        Objects.requireNonNull(other, "Cannot add null Money");
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Currency mismatch: cannot add " + other.currency + " to " + this.currency);
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money times(long multiplier) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(multiplier)), this.currency);
    }

    @Override
    public int compareTo(Money other) {
        Objects.requireNonNull(other, "Cannot compare to null Money");
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Cannot compare different currencies: " + this.currency + " and " + other.currency);
        }
        return this.amount.compareTo(other.amount);
    }
}
