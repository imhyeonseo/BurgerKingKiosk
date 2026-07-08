package com.example.kiosk_backend.controller;

import com.example.kiosk_backend.common.response.ApiResponse;
import com.example.kiosk_backend.dto.response.CategoryMenuResponse;
import com.example.kiosk_backend.dto.response.CategoryResponse;
import com.example.kiosk_backend.service.CategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ApiResponse<List<CategoryResponse>> getCategories() {
        return ApiResponse.success(categoryService.getActiveCategories());
    }

    @GetMapping("/{categoryId}/menus")
    public ApiResponse<List<CategoryMenuResponse>> getCategoryMenus(@PathVariable Long categoryId) {
        return ApiResponse.success(categoryService.getCategoryMenus(categoryId));
    }
}
