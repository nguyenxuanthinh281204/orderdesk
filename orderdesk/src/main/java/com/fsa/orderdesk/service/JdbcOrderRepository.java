package com.fsa.orderdesk.service;

import com.fsa.orderdesk.domain.Money;
import com.fsa.orderdesk.domain.Order;
import com.fsa.orderdesk.domain.OrderLine;
import com.fsa.orderdesk.domain.OrderStatus;
import com.fsa.orderdesk.domain.Sku;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

public class JdbcOrderRepository implements OrderRepository {
    private final DataSource dataSource;

    public JdbcOrderRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Order> findById(String id) {
        String orderSql = "SELECT id, customer_id, placed_at, status, tag FROM orders WHERE id = ?";
        String linesSql = "SELECT sku, quantity, unit_price, currency FROM order_lines WHERE order_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(orderSql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                String custId = rs.getString("customer_id");
                Timestamp ts = rs.getTimestamp("placed_at");
                String statusStr = rs.getString("status");
                String tag = rs.getString("tag");

                Order order = new Order(id, custId, ts != null ? ts.toInstant() : null);
                if (statusStr != null) {
                    order.advanceTo(OrderStatus.fromDb(statusStr));
                }
                order.setTag(tag);

                try (PreparedStatement lineStmt = conn.prepareStatement(linesSql)) {
                    lineStmt.setString(1, id);
                    try (ResultSet lrs = lineStmt.executeQuery()) {
                        while (lrs.next()) {
                            Sku sku = new Sku(lrs.getString("sku"));
                            long qty = lrs.getLong("quantity");
                            Money price = new Money(lrs.getBigDecimal("unit_price"), lrs.getString("currency"));
                            order.addLine(new OrderLine(sku, qty, price));
                        }
                    }
                }
                return Optional.of(order);
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find order by id: " + id, e);
        }
    }

    @Override
    public List<Order> findByCustomer(String customerId) {
        String sql = "SELECT id FROM orders WHERE customer_id = ? ORDER BY placed_at DESC";
        List<Order> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    findById(rs.getString("id")).ifPresent(list::add);
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find orders for customer: " + customerId, e);
        }
    }

    @Override
    public void save(Order order) {
        String sql = "INSERT INTO orders (id, customer_id, placed_at, status, tag) VALUES (?, ?, ?, ?, ?) " +
                "ON CONFLICT (id) DO UPDATE SET status = EXCLUDED.status, tag = EXCLUDED.tag";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, order.id());
            stmt.setString(2, order.customerId());
            stmt.setTimestamp(3, Timestamp.from(order.placedAt()));
            stmt.setString(4, order.status().dbValue());
            stmt.setString(5, order.tag());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save order: " + order.id(), e);
        }
    }

    @Override
    public void placeOrder(Order order) {
        String orderSql = "INSERT INTO orders (id, customer_id, placed_at, status, tag) VALUES (?, ?, ?, ?, ?)";
        String lineSql = "INSERT INTO order_lines (order_id, sku, quantity, unit_price, currency) VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement oStmt = conn.prepareStatement(orderSql)) {
                oStmt.setString(1, order.id());
                oStmt.setString(2, order.customerId());
                oStmt.setTimestamp(3, Timestamp.from(order.placedAt()));
                oStmt.setString(4, order.status().dbValue());
                oStmt.setString(5, order.tag());
                oStmt.executeUpdate();
            }

            try (PreparedStatement lStmt = conn.prepareStatement(lineSql)) {
                for (OrderLine line : order.lines()) {
                    lStmt.setString(1, order.id());
                    lStmt.setString(2, line.sku().value());
                    lStmt.setLong(3, line.quantity());
                    lStmt.setBigDecimal(4, line.unitPrice().amount());
                    lStmt.setString(5, line.unitPrice().currency());
                    lStmt.executeUpdate();
                }
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rbEx) {
                    e.addSuppressed(rbEx);
                }
            }
            throw new RepositoryException("Failed to place order transactionally: " + order.id(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
                try {
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }
}
