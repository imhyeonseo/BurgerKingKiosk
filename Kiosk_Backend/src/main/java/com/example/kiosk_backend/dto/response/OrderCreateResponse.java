package com.example.kiosk_backend.dto.response;

import com.example.kiosk_backend.entity.Order;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderCreateResponse(
        Integer orderNumber, BigDecimal totalPrice, List<OrderItemResponse> items, LocalDateTime createdAt
) {
    public static OrderCreateResponse of(Order order, List<OrderItemResponse> items) {
        return new OrderCreateResponse(order.getOrderNumber(), order.getTotalPrice(), items, order.getCreatedAt());
    }
}
