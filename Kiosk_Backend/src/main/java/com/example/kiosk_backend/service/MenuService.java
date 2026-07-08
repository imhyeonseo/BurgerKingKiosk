package com.example.kiosk_backend.service;

import com.example.kiosk_backend.common.exception.BusinessException;
import com.example.kiosk_backend.common.exception.ErrorCode;
import com.example.kiosk_backend.dto.response.MenuDetailResponse;
import com.example.kiosk_backend.dto.response.MenuSearchResponse;
import com.example.kiosk_backend.dto.response.SetComponentResponse;
import com.example.kiosk_backend.entity.Menu;
import com.example.kiosk_backend.repository.MenuRepository;
import com.example.kiosk_backend.repository.SetMenuItemRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** 고객(키오스크) 메뉴 조회 서비스 — /api/menus */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuService {

    private final MenuRepository menuRepository;
    private final SetMenuItemRepository setMenuItemRepository;

    public MenuDetailResponse getMenuDetail(Long menuId) {
        Menu menu = menuRepository.findByIdAndIsActiveTrueAndDeletedAtIsNull(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));

        List<SetComponentResponse> setComponents = Boolean.TRUE.equals(menu.getIsSet())
                ? setMenuItemRepository.findBySetMenuId(menu.getId()).stream().map(SetComponentResponse::from).toList()
                : null;

        return MenuDetailResponse.of(menu, setComponents);
    }

    public List<MenuSearchResponse> searchMenus(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "keyword는 필수입니다.");
        }
        return menuRepository.findByNameContainingIgnoreCaseAndIsActiveTrueAndDeletedAtIsNull(keyword.trim()).stream()
                .map(MenuSearchResponse::from)
                .toList();
    }
}
