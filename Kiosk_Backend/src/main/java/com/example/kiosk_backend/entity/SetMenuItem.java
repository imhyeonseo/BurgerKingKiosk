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
@Table(name = "set_menu_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SetMenuItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "set_menu_id", nullable = false)
    private Menu setMenu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_menu_id", nullable = false)
    private Menu componentMenu;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    public SetMenuItem(Menu setMenu, Menu componentMenu, Integer quantity) {
        this.setMenu = setMenu;
        this.componentMenu = componentMenu;
        this.quantity = quantity;
    }
}
