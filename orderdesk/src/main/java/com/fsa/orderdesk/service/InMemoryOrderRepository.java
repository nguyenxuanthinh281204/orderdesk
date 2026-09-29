package com.fsa.orderdesk.service;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.fsa.orderdesk.domain.Order;

public class InMemoryOrderRepository implements OrderRepository {
    private final Map<String, Order> store = new ConcurrentHashMap<>();

    @Override
    public Optional<Order> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Order> findByCustomer(String customerId) {
        List<Order> results = new ArrayList<>();
        for (Order o : store.values()) {
            if (o.customerId().equals(customerId)) {
                results.add(o);
            }
        }
        return results;
    }

    @Override
    public void save(Order order) {
        store.put(order.id(), order);
    }

    @Override
    public void placeOrder(Order order) {
        save(order);
    }
}
