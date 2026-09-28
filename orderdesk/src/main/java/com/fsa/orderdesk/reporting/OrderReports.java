package com.fsa.orderdesk.reporting;

import com.fsa.orderdesk.catalog.Product;
import com.fsa.orderdesk.domain.Money;
import com.fsa.orderdesk.domain.Order;
import com.fsa.orderdesk.domain.OrderLine;
import com.fsa.orderdesk.domain.OrderStatus;
import com.fsa.orderdesk.domain.Sku;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class OrderReports {

    // Build catalog index: When duplicate SKU, policy is to keep the product that
    // appears later
    public static Map<Sku, Product> buildCatalogIndex(List<Product> products) {
        return products.stream()
                .collect(Collectors.toMap(
                        Product::sku,
                        product -> product,
                        (existing, replacement) -> replacement // Duplicate SKU policy: replacement wins
                ));
    }

    // 1. Total units across a list of orders
    public static long totalUnits(List<Order> orders) {
        return orders.stream()
                .flatMap(o -> o.lines().stream())
                .mapToLong(OrderLine::quantity)
                .sum();
    }

    // 2. Orders grouped by status (sử dụng LinkedHashMap để bảo đảm thứ tự ổn định)
    public static Map<OrderStatus, List<Order>> ordersByStatus(List<Order> orders) {
        return orders.stream()
                .collect(Collectors.groupingBy(
                        Order::status,
                        LinkedHashMap::new,
                        Collectors.toList()));
    }

    // 3. A count per status in one pass
    public static Map<OrderStatus, Long> countPerStatus(List<Order> orders) {
        return orders.stream()
                .collect(Collectors.groupingBy(
                        Order::status,
                        LinkedHashMap::new,
                        Collectors.counting()));
    }

    // 4. The distinct SKUs appearing in any order
    public static Set<Sku> distinctSkus(List<Order> orders) {
        return orders.stream()
                .flatMap(o -> o.lines().stream())
                .map(OrderLine::sku)
                .collect(Collectors.toSet());
    }

    // 5. Orders sorted by status, then placed date descending, tie-break by order
    // ID
    public static List<Order> sortOrdersByStatusAndDate(List<Order> orders) {
        Comparator<Order> comparator = Comparator
                .comparing(Order::status)
                .thenComparing(Order::placedAt, Comparator.reverseOrder())
                .thenComparing(Order::id); // Deterministic tie-break by ID

        return orders.stream()
                .sorted(comparator)
                .toList();
    }

    // 6. Top n customers by lifetime value, excluding cancelled orders, tie-break
    // by customerId
    public static List<Map.Entry<String, Money>> topCustomers(List<Order> orders, int n) {
        Map<String, Money> customerLtv = orders.stream()
                .filter(o -> o.status() != OrderStatus.CANCELLED)
                .collect(Collectors.groupingBy(
                        Order::customerId,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    Money sum = Money.of("0", "VND");
                                    for (Order ord : list) {
                                        sum = sum.plus(ord.total());
                                    }
                                    return sum;
                                })));

        Comparator<Map.Entry<String, Money>> comparator = Map.Entry
                .<String, Money>comparingByValue(Comparator.reverseOrder())
                .thenComparing(Map.Entry::getKey); // Deterministic tie-break by customerId

        return customerLtv.entrySet().stream()
                .sorted(comparator)
                .limit(n)
                .toList();
    }

    // Safe removal of cancelled orders
    public static void dropCancelledOrders(List<Order> mutableOrders) {
        mutableOrders.removeIf(order -> order.status() == OrderStatus.CANCELLED);
    }
}
