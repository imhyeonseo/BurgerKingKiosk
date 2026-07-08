package com.example.kiosk_backend.service;

import com.example.kiosk_backend.entity.AdminAction;
import com.example.kiosk_backend.entity.AdminAuditLog;
import com.example.kiosk_backend.repository.AdminAuditLogRepository;
import com.example.kiosk_backend.security.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * admin_audit_logs INSERT 전담 컴포넌트. 각 관리자 쓰기 API의 Service 메서드가
 * 트랜잭션 커밋 직전(같은 트랜잭션 안)에 명시적으로 호출한다(Backend.md 5장 참조).
 */
@Component
@RequiredArgsConstructor
public class AuditLogRecorder {

    private final AdminAuditLogRepository adminAuditLogRepository;
    private final ObjectMapper objectMapper;

    public void record(AdminAction action, String targetType, Long targetId, Object beforeValue, Object afterValue) {
        recordAs(currentUsernameOrNull(), action, targetType, targetId, beforeValue, afterValue);
    }

    /** 로그인처럼 SecurityContext에 아직 인증 정보가 없는 시점을 위한 오버로드. */
    public void recordAs(String adminUsername, AdminAction action, String targetType, Long targetId,
                          Object beforeValue, Object afterValue) {
        AdminAuditLog log = AdminAuditLog.builder()
                .adminUsername(adminUsername)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .beforeValue(toJson(beforeValue))
                .afterValue(toJson(afterValue))
                .ipAddress(currentIpAddress())
                .build();
        adminAuditLogRepository.save(log);
    }

    private String currentUsernameOrNull() {
        try {
            return SecurityUtils.getCurrentAdminUsername();
        } catch (Exception e) {
            return null;
        }
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return null;
        }
    }

    private String currentIpAddress() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
