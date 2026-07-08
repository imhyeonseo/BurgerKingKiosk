package com.example.kiosk_backend.dto.response;

import com.example.kiosk_backend.entity.Order;
import com.example.kiosk_backend.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AdminOrderDetailResponse(
        Long id, Integer orderNumber, OrderStatus status, BigDecimal totalPrice,
        String sessionId, LocalDateTime createdAt, List<OrderItemResponse> items
) {
    public static AdminOrderDetailResponse of(Order order, List<OrderItemResponse> items) {
        return new AdminOrderDetailResponse(
                order.getId(), order.getOrderNumber(), order.getStatus(), order.getTotalPrice(),
                order.getSessionId(), order.getCreatedAt(), items
        );
    }
}
