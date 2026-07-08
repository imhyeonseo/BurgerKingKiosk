package com.example.kiosk_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoryUpdateRequest(

        @NotBlank(message = "name은 필수입니다.")
        String name,

        @NotNull(message = "displayOrder는 필수입니다.")
        Integer displayOrder,

        @NotNull(message = "isActive는 필수입니다.")
        Boolean isActive
) {
}
