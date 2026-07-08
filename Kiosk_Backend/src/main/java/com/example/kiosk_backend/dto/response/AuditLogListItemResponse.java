package com.example.kiosk_backend.dto.response;

import com.example.kiosk_backend.entity.AdminAuditLog;
import java.time.LocalDateTime;

public record AuditLogListItemResponse(
        Long id, String adminUsername, String action, String targetType, Long targetId, String ipAddress, LocalDateTime createdAt
) {
    public static AuditLogListItemResponse from(AdminAuditLog log) {
        return new AuditLogListItemResponse(
                log.getId(), log.getAdminUsername(), log.getAction().name(), log.getTargetType(),
                log.getTargetId(), log.getIpAddress(), log.getCreatedAt()
        );
    }
}
