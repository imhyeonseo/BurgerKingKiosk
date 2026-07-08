package com.example.kiosk_backend.dto.response;

import com.example.kiosk_backend.entity.Menu;
import java.math.BigDecimal;

public record MenuSearchResponse(
        Long id, String name, BigDecimal price, String imageUrl, Boolean isSet, String categoryName, boolean isSoldOut
) {

    public static MenuSearchResponse from(Menu menu) {
        return new MenuSearchResponse(
                menu.getId(), menu.getName(), menu.getPrice(), menu.getImageUrl(),
                menu.getIsSet(), menu.getCategory().getName(), menu.isSoldOut()
        );
    }
}
