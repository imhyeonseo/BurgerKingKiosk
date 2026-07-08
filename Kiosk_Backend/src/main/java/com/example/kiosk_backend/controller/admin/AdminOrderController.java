package com.example.kiosk_backend.controller.admin;

import com.example.kiosk_backend.common.response.ApiResponse;
import com.example.kiosk_backend.common.response.PageResponse;
import com.example.kiosk_backend.dto.response.AdminOrderDetailResponse;
import com.example.kiosk_backend.dto.response.AdminOrderListItemResponse;
import com.example.kiosk_backend.dto.response.OrderCancelResponse;
import com.example.kiosk_backend.entity.OrderStatus;
import com.example.kiosk_backend.service.AdminOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping
    public ApiResponse<PageResponse<AdminOrderListItemResponse>> getOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(adminOrderService.getOrders(status, PageRequest.of(page, size)));
    }

    @GetMapping("/{orderId}")
    public ApiResponse<AdminOrderDetailResponse> getOrderDetail(@PathVariable Long orderId) {
        return ApiResponse.success(adminOrderService.getOrderDetail(orderId));
    }

    @PatchMapping("/{orderId}/cancel")
    public ApiResponse<OrderCancelResponse> cancelOrder(@PathVariable Long orderId) {
        return ApiResponse.success(adminOrderService.cancelOrder(orderId));
    }
}
