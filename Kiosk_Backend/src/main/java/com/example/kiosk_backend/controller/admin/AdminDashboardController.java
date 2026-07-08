package com.example.kiosk_backend.controller.admin;

import com.example.kiosk_backend.common.response.ApiResponse;
import com.example.kiosk_backend.dto.response.DashboardResponse;
import com.example.kiosk_backend.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping
    public ApiResponse<DashboardResponse> getDashboard() {
        return ApiResponse.success(adminDashboardService.getDashboard());
    }
}
