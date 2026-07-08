package com.example.kiosk_backend.dto.response;

import com.example.kiosk_backend.entity.SetMenuItem;

public record SetComponentMappingResponse(Long id, Long setMenuId, Long componentMenuId, Integer quantity) {

    public static SetComponentMappingResponse from(SetMenuItem setMenuItem) {
        return new SetComponentMappingResponse(
                setMenuItem.getId(), setMenuItem.getSetMenu().getId(),
                setMenuItem.getComponentMenu().getId(), setMenuItem.getQuantity()
        );
    }
}
