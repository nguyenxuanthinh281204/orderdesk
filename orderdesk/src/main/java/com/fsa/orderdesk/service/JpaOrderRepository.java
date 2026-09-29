package com.fsa.orderdesk.service;


import com.fsa.orderdesk.domain.Order;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public class JpaOrderRepository implements OrderRepository {

    private final EntityManagerFactory emf;

    public JpaOrderRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Optional<Order> findById(String id) {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<Order> query = em.createQuery(
                    "SELECT o FROM Order o LEFT JOIN FETCH o.lines WHERE o.id = :id", Order.class);
            query.setParameter("id", id);
            return Optional.ofNullable(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty(); // Tuyệt đối không để NoResultException lộ ra ngoài
        } catch (Exception e) {
            throw new RepositoryException("Failed to find order by id: " + id, e);
        }
    }

    @Override
    public List<Order> findByCustomer(String customerId) {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<Order> query = em.createQuery(
                    "SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.lines WHERE o.customerId = :customerId ORDER BY o.placedAt DESC", Order.class);
            query.setParameter("customerId", customerId);
            return query.getResultList();
        } catch (Exception e) {
            throw new RepositoryException("Failed to find orders by customer: " + customerId, e);
        }
    }

    @Override
    public void save(Order order) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(order);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RepositoryException("Failed to save order: " + order.id(), e);
        } finally {
            em.close();
        }
    }

    @Override
    public void placeOrder(Order order) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(order);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RepositoryException("Failed to place order: " + order.id(), e);
        } finally {
            em.close();
        }
    }
}
