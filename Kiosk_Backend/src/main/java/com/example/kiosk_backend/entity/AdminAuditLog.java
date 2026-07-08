package com.example.kiosk_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Entity
@Table(name = "admin_audit_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_username", length = 50)
    private String adminUsername;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 50)
    private AdminAction action;

    @Column(name = "target_type", length = 50)
    private String targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "before_value", columnDefinition = "JSON")
    private String beforeValue;

    @Column(name = "after_value", columnDefinition = "JSON")
    private String afterValue;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public AdminAuditLog(String adminUsername, AdminAction action, String targetType, Long targetId,
                          String beforeValue, String afterValue, String ipAddress) {
        this.adminUsername = adminUsername;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.beforeValue = beforeValue;
        this.afterValue = afterValue;
        this.ipAddress = ipAddress;
    }
}
