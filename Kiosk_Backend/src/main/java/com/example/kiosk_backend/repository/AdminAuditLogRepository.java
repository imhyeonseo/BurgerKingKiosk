package com.example.kiosk_backend.repository;

import com.example.kiosk_backend.entity.AdminAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AdminAuditLogRepository
        extends JpaRepository<AdminAuditLog, Long>, JpaSpecificationExecutor<AdminAuditLog> {
}
