package com.example.kiosk_backend.service;

import com.example.kiosk_backend.common.exception.BusinessException;
import com.example.kiosk_backend.common.exception.ErrorCode;
import com.example.kiosk_backend.dto.request.LoginRequest;
import com.example.kiosk_backend.dto.response.LoginResponse;
import com.example.kiosk_backend.entity.Admin;
import com.example.kiosk_backend.entity.AdminAction;
import com.example.kiosk_backend.repository.AdminRepository;
import com.example.kiosk_backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 관리자 로그인/로그아웃 — /api/admin/auth */
@Service
@RequiredArgsConstructor
@Transactional
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuditLogRecorder auditLogRecorder;

    public LoginResponse login(LoginRequest request) {
        Admin admin = adminRepository.findById(request.username())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!admin.isUsable()) {
            throw new BusinessException(ErrorCode.ACCOUNT_INACTIVE);
        }
        if (!passwordEncoder.matches(request.password(), admin.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtTokenProvider.createAccessToken(admin.getUsername());
        admin.recordLogin();

        // 로그인 시점에는 SecurityContext에 인증 정보가 아직 없으므로 username을 직접 전달한다.
        auditLogRecorder.recordAs(admin.getUsername(), AdminAction.LOGIN, "admin", null, null, null);

        return new LoginResponse(accessToken, jwtTokenProvider.getAccessTokenExpirationSeconds());
    }

    public void logout() {
        auditLogRecorder.record(AdminAction.LOGOUT, "admin", null, null, null);
    }
}
