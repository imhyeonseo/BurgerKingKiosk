package com.example.kiosk_backend.dto.response;

import com.example.kiosk_backend.entity.Menu;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AdminMenuDetailResponse(
        Long id, Long categoryId, String name, String description, BigDecimal price, String imageUrl,
        Boolean isSet, Integer quantity, Boolean isActive, LocalDateTime deletedAt,
        LocalDateTime createdAt, LocalDateTime updatedAt, List<SetComponentResponse> setComponents
) {
    public static AdminMenuDetailResponse of(Menu menu, List<SetComponentResponse> setComponents) {
        return new AdminMenuDetailResponse(
                menu.getId(), menu.getCategory().getId(), menu.getName(), menu.getDescription(),
                menu.getPrice(), menu.getImageUrl(), menu.getIsSet(), menu.getQuantity(), menu.getIsActive(),
                menu.getDeletedAt(), menu.getCreatedAt(), menu.getUpdatedAt(), setComponents
        );
    }
}
