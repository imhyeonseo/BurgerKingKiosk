package com.example.kiosk_backend.controller.admin;

import com.example.kiosk_backend.common.response.ApiResponse;
import com.example.kiosk_backend.dto.request.LoginRequest;
import com.example.kiosk_backend.dto.response.LoginResponse;
import com.example.kiosk_backend.service.AdminAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(adminAuthService.login(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        adminAuthService.logout();
        return ApiResponse.success(null, "로그아웃 되었습니다.");
    }
}
