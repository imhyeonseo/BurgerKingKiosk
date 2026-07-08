package com.example.kiosk_backend.service;

import com.example.kiosk_backend.common.exception.BusinessException;
import com.example.kiosk_backend.common.exception.ErrorCode;
import com.example.kiosk_backend.common.response.PageResponse;
import com.example.kiosk_backend.dto.request.MenuCreateRequest;
import com.example.kiosk_backend.dto.request.MenuUpdateRequest;
import com.example.kiosk_backend.dto.request.SetComponentAddRequest;
import com.example.kiosk_backend.dto.response.AdminMenuDetailResponse;
import com.example.kiosk_backend.dto.response.AdminMenuListItemResponse;
import com.example.kiosk_backend.dto.response.SetComponentMappingResponse;
import com.example.kiosk_backend.dto.response.SetComponentResponse;
import com.example.kiosk_backend.entity.AdminAction;
import com.example.kiosk_backend.entity.Category;
import com.example.kiosk_backend.entity.Menu;
import com.example.kiosk_backend.entity.SetMenuItem;
import com.example.kiosk_backend.repository.CategoryRepository;
import com.example.kiosk_backend.repository.MenuRepository;
import com.example.kiosk_backend.repository.SetMenuItemRepository;
import com.example.kiosk_backend.repository.spec.MenuSpecifications;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 관리자 메뉴 관리 — /api/admin/menus (단품/세트 CRUD, 세트 구성 관리) */
@Service
@RequiredArgsConstructor
@Transactional
public class AdminMenuService {

    private final MenuRepository menuRepository;
    private final CategoryRepository categoryRepository;
    private final SetMenuItemRepository setMenuItemRepository;
    private final AuditLogRecorder auditLogRecorder;

    @Transactional(readOnly = true)
    public PageResponse<AdminMenuListItemResponse> getMenus(Long categoryId, Boolean isSet, Boolean isActive, Pageable pageable) {
        Specification<Menu> spec = Specification
                .where(MenuSpecifications.notDeleted())
                .and(MenuSpecifications.categoryIdEquals(categoryId))
                .and(MenuSpecifications.isSetEquals(isSet))
                .and(MenuSpecifications.isActiveEquals(isActive));

        Page<AdminMenuListItemResponse> page = menuRepository.findAll(spec, pageable).map(AdminMenuListItemResponse::from);
        return PageResponse.of(page);
    }

    @Transactional(readOnly = true)
    public AdminMenuDetailResponse getMenuDetail(Long menuId) {
        Menu menu = getMenuOrThrow(menuId);
        List<SetComponentResponse> setComponents = Boolean.TRUE.equals(menu.getIsSet())
                ? setMenuItemRepository.findBySetMenuId(menuId).stream().map(SetComponentResponse::from).toList()
                : null;
        return AdminMenuDetailResponse.of(menu, setComponents);
    }

    public AdminMenuDetailResponse createMenu(MenuCreateRequest request) {
        return createMenu(request, false);
    }

    public AdminMenuDetailResponse createSetMenu(MenuCreateRequest request) {
        return createMenu(request, true);
    }

    private AdminMenuDetailResponse createMenu(MenuCreateRequest request, boolean isSet) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        Menu menu = menuRepository.save(new Menu(
                category, request.name(), request.description(), request.price(),
                request.imageUrl(), isSet, request.quantity()
        ));

        AdminMenuDetailResponse response = AdminMenuDetailResponse.of(menu, isSet ? List.of() : null);
        auditLogRecorder.record(AdminAction.MENU_CREATE, "menu", menu.getId(), null, response);
        return response;
    }

    public SetComponentMappingResponse addSetComponent(Long setMenuId, SetComponentAddRequest request) {
        Menu setMenu = getMenuOrThrow(setMenuId);
        if (!Boolean.TRUE.equals(setMenu.getIsSet())) {
            throw new BusinessException(ErrorCode.NOT_A_SET_MENU);
        }
        Menu componentMenu = getMenuOrThrow(request.componentMenuId());
        if (Boolean.TRUE.equals(componentMenu.getIsSet())) {
            throw new BusinessException(ErrorCode.COMPONENT_MUST_BE_SINGLE_ITEM);
        }
        if (setMenuId.equals(request.componentMenuId())) {
            throw new BusinessException(ErrorCode.SELF_REFERENCE_NOT_ALLOWED);
        }
        if (setMenuItemRepository.existsBySetMenuIdAndComponentMenuId(setMenuId, request.componentMenuId())) {
            throw new BusinessException(ErrorCode.SET_COMPONENT_DUPLICATE);
        }

        SetMenuItem setMenuItem = setMenuItemRepository.save(new SetMenuItem(setMenu, componentMenu, request.quantity()));

        SetComponentMappingResponse response = SetComponentMappingResponse.from(setMenuItem);
        auditLogRecorder.record(AdminAction.SET_COMPONENT_ADD, "set_menu_items", setMenuItem.getId(), null, response);
        return response;
    }

    public void removeSetComponent(Long setMenuId, Long componentMenuId) {
        SetMenuItem setMenuItem = setMenuItemRepository.findBySetMenuIdAndComponentMenuId(setMenuId, componentMenuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SET_COMPONENT_NOT_FOUND));

        SetComponentMappingResponse before = SetComponentMappingResponse.from(setMenuItem);
        setMenuItemRepository.delete(setMenuItem);
        auditLogRecorder.record(AdminAction.SET_COMPONENT_REMOVE, "set_menu_items", before.id(), before, null);
    }

    public AdminMenuDetailResponse updateMenu(Long menuId, MenuUpdateRequest request) {
        Menu menu = getMenuOrThrow(menuId);
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        AdminMenuDetailResponse before = getMenuDetail(menuId);
        menu.update(category, request.name(), request.description(), request.price(), request.imageUrl(), request.isActive());
        AdminMenuDetailResponse after = getMenuDetail(menuId);

        auditLogRecorder.record(AdminAction.MENU_UPDATE, "menu", menuId, before, after);
        return after;
    }

    public void deleteMenu(Long menuId) {
        Menu menu = getMenuOrThrow(menuId);

        List<SetMenuItem> usages = setMenuItemRepository.findByComponentMenuId(menuId);
        if (!usages.isEmpty()) {
            String setMenuNames = usages.stream().map(u -> u.getSetMenu().getName()).distinct()
                    .reduce((a, b) -> a + ", " + b).orElse("");
            throw new BusinessException(ErrorCode.MENU_IN_USE_AS_SET_COMPONENT,
                    "다음 세트 메뉴에서 사용 중입니다: " + setMenuNames);
        }

        AdminMenuDetailResponse before = getMenuDetail(menuId);
        menu.softDelete();
        auditLogRecorder.record(AdminAction.MENU_DELETE, "menu", menuId, before, null);
    }

    private Menu getMenuOrThrow(Long menuId) {
        return menuRepository.findById(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));
    }
}
