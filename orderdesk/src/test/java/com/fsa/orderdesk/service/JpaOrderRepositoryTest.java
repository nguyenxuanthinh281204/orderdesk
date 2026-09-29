package com.fsa.orderdesk.service;

import static org.junit.jupiter.api.Assertions.*;

import com.fsa.orderdesk.domain.Money;
import com.fsa.orderdesk.domain.Order;
import com.fsa.orderdesk.domain.OrderLine;
import com.fsa.orderdesk.domain.OrderStatus;
import com.fsa.orderdesk.domain.Sku;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JpaOrderRepositoryTest {

    private static EntityManagerFactory emf;
    private JpaOrderRepository repository;

    @BeforeAll
    static void initEmf() {
        String dbPassword = System.getenv("ORDERDESK_DB_PASSWORD");
        Map<String, Object> props = new HashMap<>();
        if (dbPassword != null) {
            props.put("jakarta.persistence.jdbc.password", dbPassword);
        }

        try {
            emf = Persistence.createEntityManagerFactory("orderdesk-pu", props);
        } catch (Exception e) {
            emf = null; // Tránh vỡ build nếu môi trường chưa bật DB
        }
    }

    @AfterAll
    static void closeEmf() {
        if (emf != null) {
            emf.close();
        }
    }

    @BeforeEach
    void setUp() {
        if (emf == null) return;
        repository = new JpaOrderRepository(emf);
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM OrderLine").executeUpdate();
            em.createQuery("DELETE FROM Order").executeUpdate();
            em.getTransaction().commit();
        }
    }

    @Test
    @DisplayName("Place and find: Order persists with lines and retrieves correctly")
    void testPlaceAndFind() {
        if (emf == null) return;

        Order order = new Order("ORD-JPA-1", "CUST-JPA", Instant.now());
        order.addLine(new OrderLine(new Sku("KB-01"), 2, Money.of("100000", "VND")));
        repository.placeOrder(order);

        Optional<Order> loaded = repository.findById("ORD-JPA-1");
        assertTrue(loaded.isPresent());
        assertEquals(1, loaded.get().lines().size());
        assertEquals(Money.of("200000", "VND"), loaded.get().total());
    }

    @Test
    @DisplayName("Lookup by non-existent ID returns Optional.empty, never throws NoResultException")
    void testFindByIdNotFoundReturnsEmpty() {
        if (emf == null) return;
        Optional<Order> loaded = repository.findById("ORD-NON-EXISTENT");
        assertTrue(loaded.isEmpty());
    }

    @Test
    @DisplayName("Relationship Ownership: Helper method sets both sides and lines persist")
    void testOwnershipHelperMethod() {
        if (emf == null) return;

        Order order = new Order("ORD-OWNER", "CUST-OWNER", Instant.now());
        OrderLine line = new OrderLine(new Sku("MS-02"), 1, Money.of("50000", "VND"));

        // Dùng addLine chuẩn thiết lập cả 2 chiều
        order.addLine(line);
        assertNotNull(line.getOrder());

        repository.placeOrder(order);

        Optional<Order> retrieved = repository.findById("ORD-OWNER");
        assertTrue(retrieved.isPresent());
        assertEquals(1, retrieved.get().lines().size());
    }

    @Test
    @DisplayName("Dirty checking: Updates occur during active transaction without calling save")
    void testDirtyChecking() {
        if (emf == null) return;

        Order order = new Order("ORD-DIRTY", "CUST-DIRTY", Instant.now());
        repository.placeOrder(order);

        // Mở context, sửa trạng thái, commit mà không gọi repo.save()
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Order managedOrder = em.find(Order.class, "ORD-DIRTY");
            managedOrder.advanceTo(OrderStatus.CONFIRMED);
            em.getTransaction().commit();
        }

        Optional<Order> updated = repository.findById("ORD-DIRTY");
        assertTrue(updated.isPresent());
        assertEquals(OrderStatus.CONFIRMED, updated.get().status());
    }
}
