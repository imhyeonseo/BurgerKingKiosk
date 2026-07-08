package com.example.kiosk_backend.dto.response;

import com.example.kiosk_backend.entity.OrderItem;
import java.math.BigDecimal;

public record OrderItemResponse(String menuName, BigDecimal menuPrice, Integer quantity, BigDecimal subtotal) {

    public static OrderItemResponse from(OrderItem orderItem) {
        return new OrderItemResponse(
                orderItem.getMenuName(), orderItem.getMenuPrice(), orderItem.getQuantity(), orderItem.getSubtotal()
        );
    }
}
