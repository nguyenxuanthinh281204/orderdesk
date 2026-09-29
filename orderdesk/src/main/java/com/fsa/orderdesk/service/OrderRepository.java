package com.fsa.orderdesk.service;

import com.fsa.orderdesk.domain.Order;
import java.util.Optional;
import java.util.List;

public interface OrderRepository {
    void save(Order order);

    Optional<Order> findById(String id);

    List<Order> findByCustomer(String customerId);

    void placeOrder(Order order);
}
