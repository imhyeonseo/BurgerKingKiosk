package com.example.kiosk_backend.dto.response;

public record LoginResponse(String accessToken, long expiresIn) {
}
