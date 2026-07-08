package com.example.kiosk_backend.dto.response;

import com.example.kiosk_backend.entity.Menu;
import java.math.BigDecimal;

public record AdminMenuListItemResponse(
        Long id, String name, String categoryName, BigDecimal price, Boolean isSet, Integer quantity, Boolean isActive
) {
    public static AdminMenuListItemResponse from(Menu menu) {
        return new AdminMenuListItemResponse(
                menu.getId(), menu.getName(), menu.getCategory().getName(), menu.getPrice(),
                menu.getIsSet(), menu.getQuantity(), menu.getIsActive()
        );
    }
}
