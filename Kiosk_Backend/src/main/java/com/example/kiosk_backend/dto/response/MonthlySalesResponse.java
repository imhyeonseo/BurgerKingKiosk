package com.example.kiosk_backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record MonthlySalesResponse(int year, int month, BigDecimal totalSales, long orderCount, List<DailyBreakdownItem> dailyBreakdown) {

    public record DailyBreakdownItem(LocalDate date, BigDecimal dailySales, long dailyOrderCount) {
    }
}
