package com.example.kiosk_backend.service;

import com.example.kiosk_backend.common.exception.BusinessException;
import com.example.kiosk_backend.common.exception.ErrorCode;
import com.example.kiosk_backend.dto.request.CategoryCreateRequest;
import com.example.kiosk_backend.dto.request.CategoryUpdateRequest;
import com.example.kiosk_backend.dto.response.AdminCategoryDetailResponse;
import com.example.kiosk_backend.dto.response.AdminCategoryResponse;
import com.example.kiosk_backend.entity.AdminAction;
import com.example.kiosk_backend.entity.Category;
import com.example.kiosk_backend.repository.CategoryRepository;
import com.example.kiosk_backend.repository.MenuRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 관리자 카테고리 관리 — /api/admin/categories */
@Service
@RequiredArgsConstructor
@Transactional
public class AdminCategoryService {

    private final CategoryRepository categoryRepository;
    private final MenuRepository menuRepository;
    private final AuditLogRecorder auditLogRecorder;

    @Transactional(readOnly = true)
    public List<AdminCategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream().map(AdminCategoryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public AdminCategoryDetailResponse getCategoryDetail(Long categoryId) {
        Category category = getCategoryOrThrow(categoryId);
        long menuCount = menuRepository.countByCategoryIdAndDeletedAtIsNull(categoryId);
        return AdminCategoryDetailResponse.of(category, menuCount);
    }

    public AdminCategoryResponse createCategory(CategoryCreateRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new BusinessException(ErrorCode.CATEGORY_NAME_DUPLICATE);
        }
        Category category = categoryRepository.save(new Category(request.name(), request.displayOrder()));

        AdminCategoryResponse response = AdminCategoryResponse.from(category);
        auditLogRecorder.record(AdminAction.CATEGORY_CREATE, "category", category.getId(), null, response);
        return response;
    }

    public AdminCategoryResponse updateCategory(Long categoryId, CategoryUpdateRequest request) {
        Category category = getCategoryOrThrow(categoryId);
        if (categoryRepository.existsByNameAndIdNot(request.name(), categoryId)) {
            throw new BusinessException(ErrorCode.CATEGORY_NAME_DUPLICATE);
        }

        AdminCategoryResponse before = AdminCategoryResponse.from(category);
        category.update(request.name(), request.displayOrder(), request.isActive());
        AdminCategoryResponse after = AdminCategoryResponse.from(category);

        auditLogRecorder.record(AdminAction.CATEGORY_UPDATE, "category", categoryId, before, after);
        return after;
    }

    public void deleteCategory(Long categoryId) {
        Category category = getCategoryOrThrow(categoryId);
        long menuCount = menuRepository.countByCategoryIdAndDeletedAtIsNull(categoryId);
        if (menuCount > 0) {
            throw new BusinessException(ErrorCode.CATEGORY_HAS_MENUS,
                    "소속된 메뉴가 있어 삭제할 수 없습니다. (" + menuCount + "건)");
        }

        AdminCategoryResponse before = AdminCategoryResponse.from(category);
        categoryRepository.delete(category);
        auditLogRecorder.record(AdminAction.CATEGORY_DELETE, "category", categoryId, before, null);
    }

    private Category getCategoryOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
    }
}
