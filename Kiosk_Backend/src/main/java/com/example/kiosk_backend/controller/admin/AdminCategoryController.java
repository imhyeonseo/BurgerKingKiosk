package com.example.kiosk_backend.controller.admin;

import com.example.kiosk_backend.common.response.ApiResponse;
import com.example.kiosk_backend.dto.request.CategoryCreateRequest;
import com.example.kiosk_backend.dto.request.CategoryUpdateRequest;
import com.example.kiosk_backend.dto.response.AdminCategoryDetailResponse;
import com.example.kiosk_backend.dto.response.AdminCategoryResponse;
import com.example.kiosk_backend.service.AdminCategoryService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final AdminCategoryService adminCategoryService;

    @GetMapping
    public ApiResponse<List<AdminCategoryResponse>> getCategories() {
        return ApiResponse.success(adminCategoryService.getAllCategories());
    }

    @GetMapping("/{categoryId}")
    public ApiResponse<AdminCategoryDetailResponse> getCategoryDetail(@PathVariable Long categoryId) {
        return ApiResponse.success(adminCategoryService.getCategoryDetail(categoryId));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminCategoryResponse>> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(adminCategoryService.createCategory(request)));
    }

    @PutMapping("/{categoryId}")
    public ApiResponse<AdminCategoryResponse> updateCategory(
            @PathVariable Long categoryId, @Valid @RequestBody CategoryUpdateRequest request
    ) {
        return ApiResponse.success(adminCategoryService.updateCategory(categoryId, request));
    }

    @DeleteMapping("/{categoryId}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long categoryId) {
        adminCategoryService.deleteCategory(categoryId);
        return ApiResponse.success(null, "카테고리가 삭제되었습니다.");
    }
}
