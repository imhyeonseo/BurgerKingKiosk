package com.example.kiosk_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 메뉴 이미지 업로드 저장 경로 설정.
 * dir: 서버 로컬 디스크에 파일을 저장할 실제 경로
 * urlPrefix: 저장된 파일에 접근하는 공개 URL 경로(정적 리소스 매핑과 1:1로 대응)
 */
@ConfigurationProperties(prefix = "app.upload")
public record FileStorageProperties(String dir, String urlPrefix) {
}
