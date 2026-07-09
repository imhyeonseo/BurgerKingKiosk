package com.example.kiosk_backend.controller.admin;

import com.example.kiosk_backend.common.response.ApiResponse;
import com.example.kiosk_backend.common.response.PageResponse;
import com.example.kiosk_backend.dto.request.MenuCreateRequest;
import com.example.kiosk_backend.dto.request.MenuUpdateRequest;
import com.example.kiosk_backend.dto.request.SetComponentAddRequest;
import com.example.kiosk_backend.dto.response.AdminMenuDetailResponse;
import com.example.kiosk_backend.dto.response.AdminMenuListItemResponse;
import com.example.kiosk_backend.dto.response.SetComponentMappingResponse;
import com.example.kiosk_backend.service.AdminMenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/menus")
@RequiredArgsConstructor
public class AdminMenuController {

    private final AdminMenuService adminMenuService;

    @GetMapping
    public ApiResponse<PageResponse<AdminMenuListItemResponse>> getMenus(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean isSet,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(
                adminMenuService.getMenus(categoryId, isSet, isActive, PageRequest.of(page, size))
        );
    }

    @GetMapping("/{menuId}")
    public ApiResponse<AdminMenuDetailResponse> getMenuDetail(@PathVariable Long menuId) {
        return ApiResponse.success(adminMenuService.getMenuDetail(menuId));
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<AdminMenuDetailResponse>> createMenu(
            @Valid @RequestPart("request") MenuCreateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(adminMenuService.createMenu(request, image)));
    }

    @PostMapping(value = "/sets", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<AdminMenuDetailResponse>> createSetMenu(
            @Valid @RequestPart("request") MenuCreateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(adminMenuService.createSetMenu(request, image)));
    }

    @PostMapping("/sets/{setMenuId}/components")
    public ResponseEntity<ApiResponse<SetComponentMappingResponse>> addSetComponent(
            @PathVariable Long setMenuId, @Valid @RequestBody SetComponentAddRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(adminMenuService.addSetComponent(setMenuId, request)));
    }

    @DeleteMapping("/sets/{setMenuId}/components/{componentMenuId}")
    public ResponseEntity<Void> removeSetComponent(@PathVariable Long setMenuId, @PathVariable Long componentMenuId) {
        adminMenuService.removeSetComponent(setMenuId, componentMenuId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{menuId}")
    public ApiResponse<AdminMenuDetailResponse> updateMenu(@PathVariable Long menuId, @Valid @RequestBody MenuUpdateRequest request) {
        return ApiResponse.success(adminMenuService.updateMenu(menuId, request));
    }

    @DeleteMapping("/{menuId}")
    public ApiResponse<Void> deleteMenu(@PathVariable Long menuId) {
        adminMenuService.deleteMenu(menuId);
        return ApiResponse.success(null, "메뉴가 삭제되었습니다.");
    }
}
