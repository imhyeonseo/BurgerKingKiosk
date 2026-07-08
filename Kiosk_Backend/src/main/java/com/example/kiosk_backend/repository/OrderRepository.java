package com.example.kiosk_backend.repository;

import com.example.kiosk_backend.entity.Order;
import com.example.kiosk_backend.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Order> findTop5ByOrderByCreatedAtDesc();

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o "
            + "WHERE o.status = 'COMPLETED' AND o.createdAt >= :from AND o.createdAt < :to")
    BigDecimal sumCompletedSales(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(o) FROM Order o "
            + "WHERE o.status = 'COMPLETED' AND o.createdAt >= :from AND o.createdAt < :to")
    long countCompletedOrders(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT o FROM Order o "
            + "WHERE o.status = 'COMPLETED' AND o.createdAt >= :from AND o.createdAt < :to "
            + "ORDER BY o.createdAt ASC")
    List<Order> findCompletedOrdersBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
