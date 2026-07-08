package com.example.kiosk_backend.dto.response;

import com.example.kiosk_backend.entity.Category;
import java.time.LocalDateTime;

public record AdminCategoryDetailResponse(
        Long id, String name, Integer displayOrder, Boolean isActive, LocalDateTime createdAt, long menuCount
) {
    public static AdminCategoryDetailResponse of(Category category, long menuCount) {
        return new AdminCategoryDetailResponse(
                category.getId(), category.getName(), category.getDisplayOrder(),
                category.getIsActive(), category.getCreatedAt(), menuCount
        );
    }
}
