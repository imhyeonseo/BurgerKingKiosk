package com.example.kiosk_backend.dto.response;

import com.example.kiosk_backend.entity.CartItem;
import java.math.BigDecimal;

public record CartItemMutationResponse(Long cartItemId, Long menuId, String menuName, Integer quantity, BigDecimal subtotal) {

    public static CartItemMutationResponse from(CartItem cartItem) {
        var menu = cartItem.getMenu();
        BigDecimal subtotal = menu.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        return new CartItemMutationResponse(cartItem.getId(), menu.getId(), menu.getName(), cartItem.getQuantity(), subtotal);
    }
}
