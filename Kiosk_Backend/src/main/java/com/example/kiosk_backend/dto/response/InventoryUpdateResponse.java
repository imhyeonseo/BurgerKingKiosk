package com.example.kiosk_backend.dto.response;

import com.example.kiosk_backend.entity.Menu;

public record InventoryUpdateResponse(Long menuId, String menuName, Integer quantity) {

    public static InventoryUpdateResponse from(Menu menu) {
        return new InventoryUpdateResponse(menu.getId(), menu.getName(), menu.getQuantity());
    }
}
