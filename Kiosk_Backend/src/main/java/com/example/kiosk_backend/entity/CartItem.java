package com.example.kiosk_backend.entity;

import com.example.kiosk_backend.common.util.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "cart_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    public CartItem(Cart cart, Menu menu, Integer quantity) {
        this.cart = cart;
        this.menu = menu;
        this.quantity = quantity;
    }

    public void increaseQuantity(int amount) {
        this.quantity += amount;
    }

    public void changeQuantity(int quantity) {
        this.quantity = quantity;
    }
}
