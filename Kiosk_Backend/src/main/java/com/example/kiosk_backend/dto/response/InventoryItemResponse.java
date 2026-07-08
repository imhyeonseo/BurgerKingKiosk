package com.example.kiosk_backend.dto.response;

import com.example.kiosk_backend.entity.Menu;

public record InventoryItemResponse(Long menuId, String menuName, String categoryName, Integer quantity, boolean isSoldOut) {

    public static InventoryItemResponse from(Menu menu) {
        return new InventoryItemResponse(
                menu.getId(), menu.getName(), menu.getCategory().getName(), menu.getQuantity(), menu.isSoldOut()
        );
    }
}
