package com.example.kiosk_backend.service;

import com.example.kiosk_backend.common.exception.BusinessException;
import com.example.kiosk_backend.common.exception.ErrorCode;
import com.example.kiosk_backend.dto.response.OrderCreateResponse;
import com.example.kiosk_backend.dto.response.OrderItemResponse;
import com.example.kiosk_backend.entity.Cart;
import com.example.kiosk_backend.entity.CartItem;
import com.example.kiosk_backend.entity.Menu;
import com.example.kiosk_backend.entity.Order;
import com.example.kiosk_backend.entity.OrderItem;
import com.example.kiosk_backend.repository.CartItemRepository;
import com.example.kiosk_backend.repository.CartRepository;
import com.example.kiosk_backend.repository.MenuRepository;
import com.example.kiosk_backend.repository.OrderItemRepository;
import com.example.kiosk_backend.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 고객 주문 생성 서비스 — /api/orders (Backend.md 3.4.1 처리 순서 그대로 구현) */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderNumberSequenceService orderNumberSequenceService;

    @Transactional
    public OrderCreateResponse createOrder(String sessionId) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_EMPTY));
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new BusinessException(ErrorCode.CART_EMPTY);
        }

        // 1) 각 메뉴 재검증 (품절/비활성 메뉴가 있으면 전량 실패)
        for (CartItem cartItem : cartItems) {
            Menu menu = cartItem.getMenu();
            if (!menu.isOnSale() || menu.getQuantity() < cartItem.getQuantity()) {
                throw new BusinessException(ErrorCode.MENU_UNAVAILABLE,
                        "주문할 수 없는 메뉴가 포함되어 있습니다: " + menu.getName());
            }
        }

        // 2) 원자적 주문번호 채번
        int orderNumber = orderNumberSequenceService.issueOrderNumber();

        // 3) 주문 총액 계산 및 orders INSERT
        BigDecimal totalPrice = cartItems.stream()
                .map(item -> item.getMenu().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Order order = orderRepository.save(new Order(orderNumber, totalPrice, sessionId));

        // 4) order_items INSERT(스냅샷) + 재고 원자적 차감
        List<OrderItemResponse> itemResponses = cartItems.stream().map(cartItem -> {
            Menu menu = cartItem.getMenu();
            OrderItem orderItem = orderItemRepository.save(
                    new OrderItem(order, menu, menu.getName(), menu.getPrice(), cartItem.getQuantity())
            );

            int updatedRows = menuRepository.decreaseStock(menu.getId(), cartItem.getQuantity());
            if (updatedRows == 0) {
                throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK, "재고가 부족합니다: " + menu.getName());
            }

            return OrderItemResponse.from(orderItem);
        }).toList();

        // 5) 장바구니 비우기
        cartItemRepository.deleteByCartId(cart.getId());
        cartRepository.delete(cart);

        return OrderCreateResponse.of(order, itemResponses);
    }
}
