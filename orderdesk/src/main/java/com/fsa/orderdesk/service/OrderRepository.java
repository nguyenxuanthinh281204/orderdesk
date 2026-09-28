package com.fsa.orderdesk.service;

import com.fsa.orderdesk.domain.Order;
import java.util.Optional;

public interface OrderRepository {
    void save(Order order);

    Optional<Order> findById(String id);
}
