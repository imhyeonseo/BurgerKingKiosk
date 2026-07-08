package com.example.kiosk_backend.dto.response;

import com.example.kiosk_backend.entity.Order;
import com.example.kiosk_backend.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminOrderListItemResponse(Long id, Integer orderNumber, OrderStatus status, BigDecimal totalPrice, LocalDateTime createdAt) {

    public static AdminOrderListItemResponse from(Order order) {
        return new AdminOrderListItemResponse(
                order.getId(), order.getOrderNumber(), order.getStatus(), order.getTotalPrice(), order.getCreatedAt()
        );
    }
}
