package com.example.kiosk_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record InventoryUpdateRequest(

        @NotNull(message = "quantity는 필수입니다.")
        @PositiveOrZero(message = "quantity는 0 이상이어야 합니다.")
        Integer quantity
) {
}
