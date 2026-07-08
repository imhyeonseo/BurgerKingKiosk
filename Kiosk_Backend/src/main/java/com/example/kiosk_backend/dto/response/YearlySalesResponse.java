package com.example.kiosk_backend.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record YearlySalesResponse(int year, BigDecimal totalSales, long orderCount, List<MonthlyBreakdownItem> monthlyBreakdown) {

    public record MonthlyBreakdownItem(int month, BigDecimal monthlySales, long monthlyOrderCount) {
    }
}
