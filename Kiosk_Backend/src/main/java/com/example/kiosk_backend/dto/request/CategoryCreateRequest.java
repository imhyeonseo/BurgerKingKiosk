package com.example.kiosk_backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CategoryCreateRequest(

        @NotBlank(message = "name은 필수입니다.")
        String name,

        Integer displayOrder
) {
}
