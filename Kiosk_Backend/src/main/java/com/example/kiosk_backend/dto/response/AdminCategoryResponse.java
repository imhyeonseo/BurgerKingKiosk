package com.example.kiosk_backend.dto.response;

import com.example.kiosk_backend.entity.Category;
import java.time.LocalDateTime;

public record AdminCategoryResponse(Long id, String name, Integer displayOrder, Boolean isActive, LocalDateTime createdAt) {

    public static AdminCategoryResponse from(Category category) {
        return new AdminCategoryResponse(
                category.getId(), category.getName(), category.getDisplayOrder(),
                category.getIsActive(), category.getCreatedAt()
        );
    }
}
