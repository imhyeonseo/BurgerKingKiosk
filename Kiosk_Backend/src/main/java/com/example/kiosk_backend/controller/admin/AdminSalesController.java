package com.example.kiosk_backend.controller.admin;

import com.example.kiosk_backend.common.response.ApiResponse;
import com.example.kiosk_backend.dto.response.DailySalesResponse;
import com.example.kiosk_backend.dto.response.MonthlySalesResponse;
import com.example.kiosk_backend.dto.response.YearlySalesResponse;
import com.example.kiosk_backend.service.SalesService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/sales")
@RequiredArgsConstructor
public class AdminSalesController {

    private final SalesService salesService;

    @GetMapping("/daily")
    public ApiResponse<DailySalesResponse> getDailySales(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.success(salesService.getDailySales(date));
    }

    @GetMapping("/monthly")
    public ApiResponse<MonthlySalesResponse> getMonthlySales(
            @RequestParam int year, @RequestParam int month
    ) {
        return ApiResponse.success(salesService.getMonthlySales(year, month));
    }

    @GetMapping("/yearly")
    public ApiResponse<YearlySalesResponse> getYearlySales(@RequestParam int year) {
        return ApiResponse.success(salesService.getYearlySales(year));
    }
}
