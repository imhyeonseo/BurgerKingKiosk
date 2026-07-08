package com.example.kiosk_backend.dto.response;

import com.example.kiosk_backend.entity.Menu;
import java.math.BigDecimal;
import java.util.List;

public record MenuDetailResponse(
        Long id, String name, String description, BigDecimal price, String imageUrl,
        Boolean isSet, boolean isSoldOut, List<SetComponentResponse> setComponents
) {

    public static MenuDetailResponse of(Menu menu, List<SetComponentResponse> setComponents) {
        return new MenuDetailResponse(
                menu.getId(), menu.getName(), menu.getDescription(), menu.getPrice(), menu.getImageUrl(),
                menu.getIsSet(), menu.isSoldOut(), setComponents
        );
    }
}
