package com.example.kiosk_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record MenuUpdateRequest(

        @NotNull(message = "categoryId는 필수입니다.")
        Long categoryId,

        @NotBlank(message = "name은 필수입니다.")
        String name,

        String description,

        @NotNull(message = "price는 필수입니다.")
        @Positive(message = "price는 0보다 커야 합니다.")
        BigDecimal price,

        String imageUrl,

        @NotNull(message = "isActive는 필수입니다.")
        Boolean isActive
) {
}
