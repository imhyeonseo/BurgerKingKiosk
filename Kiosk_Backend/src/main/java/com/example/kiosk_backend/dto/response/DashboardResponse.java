package com.example.kiosk_backend.dto.response;

import com.example.kiosk_backend.entity.Order;
import com.example.kiosk_backend.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record DashboardResponse(
        BigDecimal todaySales, long todayOrderCount, BigDecimal monthSales,
        long totalMenuCount, long soldOutMenuCount, List<RecentOrderResponse> recentOrders
) {

    public record RecentOrderResponse(Integer orderNumber, BigDecimal totalPrice, OrderStatus status, LocalDateTime createdAt) {
        public static RecentOrderResponse from(Order order) {
            return new RecentOrderResponse(order.getOrderNumber(), order.getTotalPrice(), order.getStatus(), order.getCreatedAt());
        }
    }
}
