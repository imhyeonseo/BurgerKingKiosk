package com.example.kiosk_backend.service;

import com.example.kiosk_backend.common.exception.BusinessException;
import com.example.kiosk_backend.common.exception.ErrorCode;
import com.example.kiosk_backend.dto.response.DailySalesResponse;
import com.example.kiosk_backend.dto.response.DailySalesResponse.SalesOrderSummary;
import com.example.kiosk_backend.dto.response.MonthlySalesResponse;
import com.example.kiosk_backend.dto.response.MonthlySalesResponse.DailyBreakdownItem;
import com.example.kiosk_backend.dto.response.YearlySalesResponse;
import com.example.kiosk_backend.dto.response.YearlySalesResponse.MonthlyBreakdownItem;
import com.example.kiosk_backend.entity.Order;
import com.example.kiosk_backend.repository.OrderRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 매출 조회 — /api/admin/sales (status=COMPLETED만 집계) */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalesService {

    private final OrderRepository orderRepository;

    public DailySalesResponse getDailySales(LocalDate date) {
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = from.plusDays(1);
        List<Order> orders = orderRepository.findCompletedOrdersBetween(from, to);

        BigDecimal totalSales = sumOf(orders);
        List<SalesOrderSummary> summaries = orders.stream().map(SalesOrderSummary::from).toList();

        return new DailySalesResponse(date, totalSales, orders.size(), summaries);
    }

    public MonthlySalesResponse getMonthlySales(int year, int month) {
        if (month < 1 || month > 12) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "month는 1~12 사이여야 합니다.");
        }
        LocalDateTime from = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime to = from.plusMonths(1);
        List<Order> orders = orderRepository.findCompletedOrdersBetween(from, to);

        Map<LocalDate, List<Order>> byDate = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getCreatedAt().toLocalDate(), TreeMap::new, Collectors.toList()));

        List<DailyBreakdownItem> dailyBreakdown = byDate.entrySet().stream()
                .map(e -> new DailyBreakdownItem(e.getKey(), sumOf(e.getValue()), e.getValue().size()))
                .toList();

        return new MonthlySalesResponse(year, month, sumOf(orders), orders.size(), dailyBreakdown);
    }

    public YearlySalesResponse getYearlySales(int year) {
        LocalDateTime from = LocalDate.of(year, 1, 1).atStartOfDay();
        LocalDateTime to = from.plusYears(1);
        List<Order> orders = orderRepository.findCompletedOrdersBetween(from, to);

        Map<Integer, List<Order>> byMonth = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getCreatedAt().getMonthValue(), TreeMap::new, Collectors.toList()));

        List<MonthlyBreakdownItem> monthlyBreakdown = byMonth.entrySet().stream()
                .map(e -> new MonthlyBreakdownItem(e.getKey(), sumOf(e.getValue()), e.getValue().size()))
                .toList();

        return new YearlySalesResponse(year, sumOf(orders), orders.size(), monthlyBreakdown);
    }

    private BigDecimal sumOf(List<Order> orders) {
        return orders.stream().map(Order::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
