package com.example.kiosk_backend.service;

import com.example.kiosk_backend.common.exception.BusinessException;
import com.example.kiosk_backend.common.exception.ErrorCode;
import com.example.kiosk_backend.common.response.PageResponse;
import com.example.kiosk_backend.dto.response.AdminOrderDetailResponse;
import com.example.kiosk_backend.dto.response.AdminOrderListItemResponse;
import com.example.kiosk_backend.dto.response.OrderCancelResponse;
import com.example.kiosk_backend.dto.response.OrderItemResponse;
import com.example.kiosk_backend.entity.AdminAction;
import com.example.kiosk_backend.entity.Order;
import com.example.kiosk_backend.entity.OrderStatus;
import com.example.kiosk_backend.repository.OrderItemRepository;
import com.example.kiosk_backend.repository.OrderRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 관리자 주문 관리 — /api/admin/orders */
@Service
@RequiredArgsConstructor
@Transactional
public class AdminOrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final AuditLogRecorder auditLogRecorder;

    @Transactional(readOnly = true)
    public PageResponse<AdminOrderListItemResponse> getOrders(OrderStatus status, Pageable pageable) {
        Page<Order> page = (status != null)
                ? orderRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                : orderRepository.findAllByOrderByCreatedAtDesc(pageable);
        return PageResponse.of(page.map(AdminOrderListItemResponse::from));
    }

    @Transactional(readOnly = true)
    public AdminOrderDetailResponse getOrderDetail(Long orderId) {
        Order order = getOrderOrThrow(orderId);
        var items = orderItemRepository.findByOrderId(orderId).stream().map(OrderItemResponse::from).toList();
        return AdminOrderDetailResponse.of(order, items);
    }

    public OrderCancelResponse cancelOrder(Long orderId) {
        Order order = getOrderOrThrow(orderId);
        if (order.isCancelled()) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_CANCELLED);
        }

        order.cancel();

        auditLogRecorder.record(AdminAction.ORDER_CANCEL, "order", orderId,
                Map.of("status", "COMPLETED"), Map.of("status", "CANCELLED"));

        return OrderCancelResponse.from(order);
    }

    private Order getOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }
}
