package com.example.kiosk_backend.repository;

import com.example.kiosk_backend.entity.CartItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCartId(Long cartId);

    Optional<CartItem> findByCartIdAndMenuId(Long cartId, Long menuId);

    void deleteByCartId(Long cartId);
}
