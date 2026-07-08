package com.example.kiosk_backend.dto.response;

import com.example.kiosk_backend.entity.AdminAuditLog;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;

public record AuditLogDetailResponse(
        Long id, String adminUsername, String action, String targetType, Long targetId,
        JsonNode beforeValue, JsonNode afterValue, String ipAddress, LocalDateTime createdAt
) {
    public static AuditLogDetailResponse from(AdminAuditLog log, ObjectMapper objectMapper) {
        return new AuditLogDetailResponse(
                log.getId(), log.getAdminUsername(), log.getAction().name(), log.getTargetType(), log.getTargetId(),
                readJson(objectMapper, log.getBeforeValue()), readJson(objectMapper, log.getAfterValue()),
                log.getIpAddress(), log.getCreatedAt()
        );
    }

    private static JsonNode readJson(ObjectMapper objectMapper, String value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.readTree(value);
        } catch (Exception e) {
            return null;
        }
    }
}
