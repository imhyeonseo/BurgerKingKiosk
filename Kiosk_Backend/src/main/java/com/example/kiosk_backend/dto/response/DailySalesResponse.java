package com.example.kiosk_backend.dto.response;

import com.example.kiosk_backend.entity.Order;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record DailySalesResponse(LocalDate date, BigDecimal totalSales, long orderCount, List<SalesOrderSummary> orders) {

    public record SalesOrderSummary(Integer orderNumber, BigDecimal totalPrice, LocalDateTime createdAt) {
        public static SalesOrderSummary from(Order order) {
            return new SalesOrderSummary(order.getOrderNumber(), order.getTotalPrice(), order.getCreatedAt());
        }
    }
}
