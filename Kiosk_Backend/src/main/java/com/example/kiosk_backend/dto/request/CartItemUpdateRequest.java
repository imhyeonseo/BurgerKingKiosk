package com.example.kiosk_backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartItemUpdateRequest(

        @NotNull(message = "quantity는 필수입니다.")
        @Min(value = 1, message = "quantity는 1 이상이어야 합니다.")
        Integer quantity
) {
}
