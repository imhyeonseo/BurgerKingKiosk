package com.example.kiosk_backend.repository.spec;

import com.example.kiosk_backend.entity.AdminAction;
import com.example.kiosk_backend.entity.AdminAuditLog;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;

public final class AdminAuditLogSpecifications {

    private AdminAuditLogSpecifications() {
    }

    public static Specification<AdminAuditLog> actionEquals(AdminAction action) {
        return (root, query, cb) -> action == null ? null : cb.equal(root.get("action"), action);
    }

    public static Specification<AdminAuditLog> createdAtFrom(LocalDateTime from) {
        return (root, query, cb) -> from == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<AdminAuditLog> createdAtTo(LocalDateTime to) {
        return (root, query, cb) -> to == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }
}
