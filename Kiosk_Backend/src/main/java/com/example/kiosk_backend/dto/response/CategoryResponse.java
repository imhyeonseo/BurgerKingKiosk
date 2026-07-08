package com.example.kiosk_backend.dto.response;

import com.example.kiosk_backend.entity.Category;

public record CategoryResponse(Long id, String name, Integer displayOrder) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getDisplayOrder());
    }
}
