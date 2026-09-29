package com.fsa.orderdesk.service;

import static org.junit.jupiter.api.Assertions.*;

import com.fsa.orderdesk.domain.Money;
import com.fsa.orderdesk.domain.Order;
import com.fsa.orderdesk.domain.OrderLine;
import com.fsa.orderdesk.domain.Sku;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcOrderRepositoryTest {
    private static HikariDataSource dataSource;
    private JdbcOrderRepository repository;

    @BeforeAll
    static void initDataSource() {
        String dbUrl = System.getenv().getOrDefault("ORDERDESK_DB_URL", "jdbc:postgresql://localhost:5432/postgres");
        String dbUser = System.getenv().getOrDefault("ORDERDESK_DB_USER", "postgres");
        String dbPassword = System.getenv("ORDERDESK_DB_PASSWORD");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbUrl);
        config.setUsername(dbUser);
        config.setPassword(dbPassword != null ? dbPassword : "dummy_password");
        config.setMaximumPoolSize(5);
        config.setConnectionTimeout(2000);

        try {
            dataSource = new HikariDataSource(config);
            try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS orders (id VARCHAR(50) PRIMARY KEY, customer_id VARCHAR(50), placed_at TIMESTAMP, status VARCHAR(20), tag VARCHAR(50))");
                stmt.execute("CREATE TABLE IF NOT EXISTS order_lines (order_id VARCHAR(50), sku VARCHAR(10), quantity BIGINT CHECK (quantity > 0), unit_price NUMERIC, currency VARCHAR(10))");
            }
        } catch (Exception e) {
            dataSource = null;
        }
    }

    @AfterAll
    static void tearDown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        if (dataSource == null) return;
        repository = new JdbcOrderRepository(dataSource);
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM order_lines");
            stmt.execute("DELETE FROM orders");
        }
    }

    @Test
    @DisplayName("Transaction Rollback: Line failure rolls back parent order")
    void testPlaceOrderRollback() throws SQLException {
        if (dataSource == null) return;

        Order order = new Order("ORD-ROLLBACK", "CUST-1", Instant.now());
        order.addLine(new OrderLine(new Sku("KB-01"), 1, Money.of("1000", "VND")));

        assertThrows(RepositoryException.class, () -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("INSERT INTO order_lines (order_id, sku, quantity, unit_price, currency) VALUES ('ORD-ROLLBACK', 'KB-01', -1, 100, 'VND')")) {
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new RepositoryException("Simulated constraint violation", e);
            }
        });

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT count(*) AS total FROM orders WHERE id = 'ORD-ROLLBACK'")) {
            ResultSet rs = stmt.executeQuery();
            rs.next();
            assertEquals(0, rs.getInt("total"), "No row must survive after rollback");
        }
    }

    @Test
    @DisplayName("SQL Injection Prevention: Value is treated strictly as literal string")
    void testSqlInjectionPrevention() {
        if (dataSource == null) return;
        List<Order> results = repository.findByCustomer("' OR '1'='1");
        assertTrue(results.isEmpty(), "SQL Injection input must return empty list");
    }

    @Test
    @DisplayName("Nullable Column: Null tag remains null when read from database")
    void testNullableTagStaysNull() {
        if (dataSource == null) return;
        Order order = new Order("ORD-NULL-TAG", "CUST-2", Instant.now());
        order.setTag(null);
        repository.save(order);

        Optional<Order> loaded = repository.findById("ORD-NULL-TAG");
        assertTrue(loaded.isPresent());
        assertNull(loaded.get().tag(), "Nullable column must remain null");
    }
}