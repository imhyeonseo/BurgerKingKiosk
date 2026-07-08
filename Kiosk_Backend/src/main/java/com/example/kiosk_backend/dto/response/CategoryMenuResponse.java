package com.example.kiosk_backend.dto.response;

import com.example.kiosk_backend.entity.Menu;
import java.math.BigDecimal;

public record CategoryMenuResponse(
        Long id, String name, BigDecimal price, String imageUrl, Boolean isSet, boolean isSoldOut
) {

    public static CategoryMenuResponse from(Menu menu) {
        return new CategoryMenuResponse(
                menu.getId(), menu.getName(), menu.getPrice(), menu.getImageUrl(),
                menu.getIsSet(), menu.isSoldOut()
        );
    }
}
