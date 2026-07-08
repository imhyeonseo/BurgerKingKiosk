package com.example.kiosk_backend.dto.response;

/** 장바구니 담기 결과 + 이번 요청에서 확정된(또는 새로 발급된) 세션 ID */
public record CartAddResult(String sessionId, CartItemMutationResponse item) {
}
