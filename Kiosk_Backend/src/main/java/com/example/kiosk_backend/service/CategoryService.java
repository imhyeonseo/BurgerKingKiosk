package com.example.kiosk_backend.service;

import com.example.kiosk_backend.common.exception.BusinessException;
import com.example.kiosk_backend.common.exception.ErrorCode;
import com.example.kiosk_backend.dto.response.CategoryMenuResponse;
import com.example.kiosk_backend.dto.response.CategoryResponse;
import com.example.kiosk_backend.entity.Category;
import com.example.kiosk_backend.repository.CategoryRepository;
import com.example.kiosk_backend.repository.MenuRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 고객(키오스크) 카테고리 조회 서비스 — /api/categories */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final MenuRepository menuRepository;

    public List<CategoryResponse> getActiveCategories() {
        return categoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc().stream()
                .map(CategoryResponse::from)
                .toList();
    }

    public List<CategoryMenuResponse> getCategoryMenus(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .filter(Category::getIsActive)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        return menuRepository.findByCategoryIdAndIsActiveTrueAndDeletedAtIsNull(category.getId()).stream()
                .map(CategoryMenuResponse::from)
                .toList();
    }
}
