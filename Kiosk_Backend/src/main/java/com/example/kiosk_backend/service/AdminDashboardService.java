package com.example.kiosk_backend.service;

import com.example.kiosk_backend.dto.response.DashboardResponse;
import com.example.kiosk_backend.dto.response.DashboardResponse.RecentOrderResponse;
import com.example.kiosk_backend.repository.MenuRepository;
import com.example.kiosk_backend.repository.OrderRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 관리자 대시보드 요약 — /api/admin/dashboard */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final OrderRepository orderRepository;
    private final MenuRepository menuRepository;

    public DashboardResponse getDashboard() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        var todaySales = orderRepository.sumCompletedSales(todayStart, todayEnd);
        var todayOrderCount = orderRepository.countCompletedOrders(todayStart, todayEnd);
        var monthSales = orderRepository.sumCompletedSales(monthStart, now.plusSeconds(1));

        long totalMenuCount = menuRepository.countByDeletedAtIsNullAndIsActiveTrue();
        long soldOutMenuCount = menuRepository.countByDeletedAtIsNullAndIsActiveTrueAndQuantity(0);

        var recentOrders = orderRepository.findTop5ByOrderByCreatedAtDesc().stream()
                .map(RecentOrderResponse::from)
                .toList();

        return new DashboardResponse(todaySales, todayOrderCount, monthSales, totalMenuCount, soldOutMenuCount, recentOrders);
    }
}
