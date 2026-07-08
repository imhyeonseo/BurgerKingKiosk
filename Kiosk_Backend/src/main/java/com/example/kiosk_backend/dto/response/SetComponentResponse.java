package com.example.kiosk_backend.dto.response;

import com.example.kiosk_backend.entity.SetMenuItem;
import java.math.BigDecimal;

public record SetComponentResponse(Long id, String name, BigDecimal price, Integer quantity) {

    public static SetComponentResponse from(SetMenuItem setMenuItem) {
        return new SetComponentResponse(
                setMenuItem.getComponentMenu().getId(),
                setMenuItem.getComponentMenu().getName(),
                setMenuItem.getComponentMenu().getPrice(),
                setMenuItem.getQuantity()
        );
    }
}
