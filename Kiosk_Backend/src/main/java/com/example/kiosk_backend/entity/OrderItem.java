package com.example.kiosk_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Entity
@Table(name = "order_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(name = "menu_name", length = 100, nullable = false)
    private String menuName;

    @Column(name = "menu_price", nullable = false, precision = 10, scale = 0)
    private BigDecimal menuPrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 0)
    private BigDecimal subtotal;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public OrderItem(Order order, Menu menu, String menuName, BigDecimal menuPrice, Integer quantity) {
        this.order = order;
        this.menu = menu;
        this.menuName = menuName;
        this.menuPrice = menuPrice;
        this.quantity = quantity;
        this.subtotal = menuPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
