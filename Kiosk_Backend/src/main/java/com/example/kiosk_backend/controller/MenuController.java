package com.example.kiosk_backend.controller;

import com.example.kiosk_backend.common.response.ApiResponse;
import com.example.kiosk_backend.dto.response.MenuDetailResponse;
import com.example.kiosk_backend.dto.response.MenuSearchResponse;
import com.example.kiosk_backend.service.MenuService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/{menuId}")
    public ApiResponse<MenuDetailResponse> getMenuDetail(@PathVariable Long menuId) {
        return ApiResponse.success(menuService.getMenuDetail(menuId));
    }

    @GetMapping("/search")
    public ApiResponse<List<MenuSearchResponse>> searchMenus(@RequestParam String keyword) {
        return ApiResponse.success(menuService.searchMenus(keyword));
    }
}
