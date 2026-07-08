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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "menus")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false, precision = 10, scale = 0)
    private BigDecimal price;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_set", nullable = false)
    private Boolean isSet;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public Menu(Category category, String name, String description, BigDecimal price,
                String imageUrl, boolean isSet, Integer quantity) {
        this.category = category;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.isSet = isSet;
        this.quantity = quantity;
        this.isActive = true;
    }

    public boolean isSoldOut() {
        return quantity != null && quantity == 0;
    }

    public boolean isOnSale() {
        return Boolean.TRUE.equals(isActive) && deletedAt == null;
    }

    public void update(Category category, String name, String description, BigDecimal price,
                        String imageUrl, Boolean isActive) {
        this.category = category;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.isActive = isActive;
    }

    public void changeQuantity(int newQuantity) {
        this.quantity = newQuantity;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.isActive = false;
    }

    /** 재고를 조건부로 차감한다. 재고 부족 시 false를 반환해 호출부가 동시성 실패를 감지하도록 한다. */
    public boolean decreaseStock(int amount) {
        if (this.quantity < amount) {
            return false;
        }
        this.quantity -= amount;
        return true;
    }
}
