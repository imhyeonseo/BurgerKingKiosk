package com.example.kiosk_backend.service;

import com.example.kiosk_backend.common.exception.BusinessException;
import com.example.kiosk_backend.common.exception.ErrorCode;
import com.example.kiosk_backend.dto.request.CartItemAddRequest;
import com.example.kiosk_backend.dto.request.CartItemUpdateRequest;
import com.example.kiosk_backend.dto.response.CartAddResult;
import com.example.kiosk_backend.dto.response.CartItemMutationResponse;
import com.example.kiosk_backend.dto.response.CartResponse;
import com.example.kiosk_backend.entity.Cart;
import com.example.kiosk_backend.entity.CartItem;
import com.example.kiosk_backend.entity.Menu;
import com.example.kiosk_backend.repository.CartItemRepository;
import com.example.kiosk_backend.repository.CartRepository;
import com.example.kiosk_backend.repository.MenuRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** 세션 기반 장바구니 서비스 — /api/carts (carts 헤더 + cart_items 상세) */
@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuRepository menuRepository;

    @Transactional(readOnly = true)
    public CartResponse getCart(String sessionId) {
        return cartRepository.findBySessionId(sessionId)
                .map(cart -> CartResponse.of(cart.getId(), sessionId, cartItemRepository.findByCartId(cart.getId())))
                .orElseGet(() -> CartResponse.empty(sessionId));
    }

    public CartAddResult addItem(String sessionId, CartItemAddRequest request) {
        String resolvedSessionId = StringUtils.hasText(sessionId) ? sessionId : UUID.randomUUID().toString();
        Cart cart = cartRepository.findBySessionId(resolvedSessionId)
                .orElseGet(() -> cartRepository.save(new Cart(resolvedSessionId)));

        Menu menu = menuRepository.findById(request.menuId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));
        if (!menu.isOnSale()) {
            throw new BusinessException(ErrorCode.MENU_INACTIVE);
        }
        if (menu.isSoldOut()) {
            throw new BusinessException(ErrorCode.MENU_SOLD_OUT, "품절된 메뉴입니다: " + menu.getName());
        }

        CartItem cartItem = cartItemRepository.findByCartIdAndMenuId(cart.getId(), menu.getId())
                .map(existing -> {
                    existing.increaseQuantity(request.quantity());
                    return existing;
                })
                .orElseGet(() -> cartItemRepository.save(new CartItem(cart, menu, request.quantity())));

        return new CartAddResult(resolvedSessionId, CartItemMutationResponse.from(cartItem));
    }

    public CartItemMutationResponse updateItemQuantity(String sessionId, Long cartItemId, CartItemUpdateRequest request) {
        CartItem cartItem = getOwnedCartItem(sessionId, cartItemId);
        cartItem.changeQuantity(request.quantity());
        return CartItemMutationResponse.from(cartItem);
    }

    public void deleteItem(String sessionId, Long cartItemId) {
        CartItem cartItem = getOwnedCartItem(sessionId, cartItemId);
        cartItemRepository.delete(cartItem);
    }

    public void clearCart(String sessionId) {
        cartRepository.findBySessionId(sessionId).ifPresent(cart -> {
            cartItemRepository.deleteByCartId(cart.getId());
            cartRepository.delete(cart);
        });
    }

    private CartItem getOwnedCartItem(String sessionId, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ACCESS_FORBIDDEN));
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new BusinessException(ErrorCode.CART_ACCESS_FORBIDDEN);
        }
        return cartItem;
    }
}
