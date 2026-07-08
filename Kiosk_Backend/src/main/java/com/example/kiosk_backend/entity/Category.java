package com.example.kiosk_backend.entity;

import com.example.kiosk_backend.common.util.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "categories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 50, nullable = false, unique = true)
    private String name;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    public Category(String name, Integer displayOrder) {
        this.name = name;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
        this.isActive = true;
    }

    public void update(String name, Integer displayOrder, Boolean isActive) {
        this.name = name;
        this.displayOrder = displayOrder;
        this.isActive = isActive;
    }
}
