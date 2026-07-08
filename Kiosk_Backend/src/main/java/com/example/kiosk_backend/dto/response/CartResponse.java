package com.example.kiosk_backend.dto.response;

import com.example.kiosk_backend.entity.CartItem;
import java.math.BigDecimal;
import java.util.List;

public record CartResponse(Long cartId, String sessionId, List<CartLineItemResponse> items, BigDecimal totalPrice) {

    public static CartResponse empty(String sessionId) {
        return new CartResponse(null, sessionId, List.of(), BigDecimal.ZERO);
    }

    public static CartResponse of(Long cartId, String sessionId, List<CartItem> cartItems) {
        List<CartLineItemResponse> items = cartItems.stream().map(CartLineItemResponse::from).toList();
        BigDecimal totalPrice = items.stream()
                .map(CartLineItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(cartId, sessionId, items, totalPrice);
    }

    public record CartLineItemResponse(
            Long cartItemId, Long menuId, String menuName, BigDecimal price, String imageUrl,
            Integer quantity, BigDecimal subtotal, boolean isSoldOut
    ) {
        public static CartLineItemResponse from(CartItem cartItem) {
            var menu = cartItem.getMenu();
            BigDecimal subtotal = menu.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            return new CartLineItemResponse(
                    cartItem.getId(), menu.getId(), menu.getName(), menu.getPrice(), menu.getImageUrl(),
                    cartItem.getQuantity(), subtotal, menu.isSoldOut()
            );
        }
    }
}
