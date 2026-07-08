package com.example.kiosk_backend.controller;

import com.example.kiosk_backend.common.response.ApiResponse;
import com.example.kiosk_backend.dto.request.CartItemAddRequest;
import com.example.kiosk_backend.dto.request.CartItemUpdateRequest;
import com.example.kiosk_backend.dto.response.CartAddResult;
import com.example.kiosk_backend.dto.response.CartItemMutationResponse;
import com.example.kiosk_backend.dto.response.CartResponse;
import com.example.kiosk_backend.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private static final String SESSION_HEADER = "X-Session-Id";

    private final CartService cartService;

    @GetMapping
    public ApiResponse<CartResponse> getCart(@RequestHeader(SESSION_HEADER) String sessionId) {
        return ApiResponse.success(cartService.getCart(sessionId));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartItemMutationResponse>> addItem(
            @RequestHeader(value = SESSION_HEADER, required = false) String sessionId,
            @Valid @RequestBody CartItemAddRequest request
    ) {
        CartAddResult result = cartService.addItem(sessionId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(SESSION_HEADER, result.sessionId())
                .body(ApiResponse.success(result.item()));
    }

    @PatchMapping("/items/{cartItemId}")
    public ApiResponse<CartItemMutationResponse> updateItemQuantity(
            @RequestHeader(SESSION_HEADER) String sessionId,
            @PathVariable Long cartItemId,
            @Valid @RequestBody CartItemUpdateRequest request
    ) {
        return ApiResponse.success(cartService.updateItemQuantity(sessionId, cartItemId, request));
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Void> deleteItem(
            @RequestHeader(SESSION_HEADER) String sessionId,
            @PathVariable Long cartItemId
    ) {
        cartService.deleteItem(sessionId, cartItemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@RequestHeader(SESSION_HEADER) String sessionId) {
        cartService.clearCart(sessionId);
        return ResponseEntity.noContent().build();
    }
}
