package com.fsa.orderdesk.service;

import com.fsa.orderdesk.catalog.Product;
import com.fsa.orderdesk.domain.Order;
import com.fsa.orderdesk.domain.OrderLine;
import com.fsa.orderdesk.domain.Sku;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class OrderService {
    private final Map<Sku, Product> catalogIndex = new HashMap<>();
    private final OrderRepository repository;

    public OrderService(List<Product> products, OrderRepository repository) {
        Objects.requireNonNull(products, "Products list cannot be null");
        this.repository = Objects.requireNonNull(repository, "Repository cannot be null");
        for (Product product : products) {
            catalogIndex.put(product.sku(), product);
        }
    }

    public void place(Order order) {
        Objects.requireNonNull(order, "Order cannot be null");

        for (OrderLine line : order.lines()) {
            Sku lineSku = line.sku();
            Product product = catalogIndex.get(lineSku);

            if (product == null) {
                throw new UnknownSkuException(lineSku);
            }
            if (!product.active()) {
                throw new InactiveProductException(lineSku);
            }
        }

        repository.save(order);
    }
}