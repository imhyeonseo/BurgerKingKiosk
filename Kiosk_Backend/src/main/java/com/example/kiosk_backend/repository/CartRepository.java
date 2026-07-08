package com.example.kiosk_backend.repository;

import com.example.kiosk_backend.entity.Cart;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findBySessionId(String sessionId);

    void deleteBySessionId(String sessionId);
}
