package com.example.kiosk_backend.entity;

import com.example.kiosk_backend.common.util.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 백오피스 관리자 계정. 매장을 운영하는 관리자는 한 명뿐이므로 권한 등급 구분이 없고,
 * 로그인 아이디(username) 자체를 자연키(PK)로 사용한다.
 */
@Getter
@Entity
@Table(name = "admins")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Admin extends BaseTimeEntity {

    @Id
    @Column(name = "username", length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public Admin(String username, String passwordHash, String name) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.name = name;
        this.isActive = true;
    }

    public boolean isUsable() {
        return Boolean.TRUE.equals(isActive) && deletedAt == null;
    }

    public void recordLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }
}
