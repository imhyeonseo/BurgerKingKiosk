package com.example.kiosk_backend.security;

import com.example.kiosk_backend.common.exception.BusinessException;
import com.example.kiosk_backend.common.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** JwtAuthenticationFilter가 SecurityContext에 심어둔 현재 로그인 관리자의 username을 꺼내는 헬퍼. */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static String getCurrentAdminUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof String username)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return username;
    }
}
