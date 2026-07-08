package com.example.kiosk_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record MenuCreateRequest(

        @NotNull(message = "categoryId는 필수입니다.")
        Long categoryId,

        @NotBlank(message = "name은 필수입니다.")
        String name,

        String description,

        @NotNull(message = "price는 필수입니다.")
        @jakarta.validation.constraints.Positive(message = "price는 0보다 커야 합니다.")
        BigDecimal price,

        String imageUrl,

        @NotNull(message = "quantity는 필수입니다.")
        @PositiveOrZero(message = "quantity는 0 이상이어야 합니다.")
        Integer quantity
) {
}
