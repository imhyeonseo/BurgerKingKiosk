package com.example.kiosk_backend.controller.admin;

import com.example.kiosk_backend.common.response.ApiResponse;
import com.example.kiosk_backend.common.response.PageResponse;
import com.example.kiosk_backend.dto.request.InventoryUpdateRequest;
import com.example.kiosk_backend.dto.response.InventoryItemResponse;
import com.example.kiosk_backend.dto.response.InventoryUpdateResponse;
import com.example.kiosk_backend.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/inventory")
@RequiredArgsConstructor
public class AdminInventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ApiResponse<PageResponse<InventoryItemResponse>> getInventory(
            @RequestParam(required = false) Boolean isSoldOut,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(inventoryService.getInventory(isSoldOut, PageRequest.of(page, size)));
    }

    @PatchMapping("/{menuId}")
    public ApiResponse<InventoryUpdateResponse> updateQuantity(
            @PathVariable Long menuId, @Valid @RequestBody InventoryUpdateRequest request
    ) {
        return ApiResponse.success(inventoryService.updateQuantity(menuId, request));
    }
}
