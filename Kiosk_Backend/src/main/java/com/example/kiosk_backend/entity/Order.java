package com.example.kiosk_backend.entity;

import com.example.kiosk_backend.common.util.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true)
    private Integer orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 0)
    private BigDecimal totalPrice;

    @Column(name = "session_id", nullable = false, length = 64)
    private String sessionId;

    public Order(Integer orderNumber, BigDecimal totalPrice, String sessionId) {
        this.orderNumber = orderNumber;
        this.status = OrderStatus.COMPLETED;
        this.totalPrice = totalPrice;
        this.sessionId = sessionId;
    }

    public void cancel() {
        this.status = OrderStatus.CANCELLED;
    }

    public boolean isCancelled() {
        return this.status == OrderStatus.CANCELLED;
    }
}
