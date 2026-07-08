package com.example.kiosk_backend.service;

import com.example.kiosk_backend.common.exception.BusinessException;
import com.example.kiosk_backend.common.exception.ErrorCode;
import com.example.kiosk_backend.common.response.PageResponse;
import com.example.kiosk_backend.dto.request.InventoryUpdateRequest;
import com.example.kiosk_backend.dto.response.InventoryItemResponse;
import com.example.kiosk_backend.dto.response.InventoryUpdateResponse;
import com.example.kiosk_backend.entity.AdminAction;
import com.example.kiosk_backend.entity.Menu;
import com.example.kiosk_backend.repository.MenuRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 재고 전용 관리 — /api/admin/inventory (일반 메뉴 수정 API와 분리) */
@Service
@RequiredArgsConstructor
@Transactional
public class InventoryService {

    private final MenuRepository menuRepository;
    private final AuditLogRecorder auditLogRecorder;

    @Transactional(readOnly = true)
    public PageResponse<InventoryItemResponse> getInventory(Boolean isSoldOut, Pageable pageable) {
        Page<Menu> page;
        if (Boolean.TRUE.equals(isSoldOut)) {
            page = menuRepository.findByDeletedAtIsNullAndQuantity(0, pageable);
        } else if (Boolean.FALSE.equals(isSoldOut)) {
            page = menuRepository.findByDeletedAtIsNullAndQuantityGreaterThan(0, pageable);
        } else {
            page = menuRepository.findByDeletedAtIsNull(pageable);
        }
        return PageResponse.of(page.map(InventoryItemResponse::from));
    }

    public InventoryUpdateResponse updateQuantity(Long menuId, InventoryUpdateRequest request) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));

        int before = menu.getQuantity();
        menu.changeQuantity(request.quantity());

        auditLogRecorder.record(AdminAction.INVENTORY_UPDATE, "menu", menuId,
                Map.of("quantity", before), Map.of("quantity", request.quantity()));

        return InventoryUpdateResponse.from(menu);
    }
}
