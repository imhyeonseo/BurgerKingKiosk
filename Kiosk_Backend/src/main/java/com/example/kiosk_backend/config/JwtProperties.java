package com.example.kiosk_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, long accessTokenExpiration) {
}
