package com.example.kiosk_backend.controller.admin;

import com.example.kiosk_backend.common.response.ApiResponse;
import com.example.kiosk_backend.common.response.PageResponse;
import com.example.kiosk_backend.dto.response.AuditLogDetailResponse;
import com.example.kiosk_backend.dto.response.AuditLogListItemResponse;
import com.example.kiosk_backend.entity.AdminAction;
import com.example.kiosk_backend.service.AdminAuditLogService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
public class AdminAuditLogController {

    private final AdminAuditLogService adminAuditLogService;

    @GetMapping
    public ApiResponse<PageResponse<AuditLogListItemResponse>> getAuditLogs(
            @RequestParam(required = false) AdminAction action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(
                adminAuditLogService.getAuditLogs(action, startDate, endDate, PageRequest.of(page, size))
        );
    }

    @GetMapping("/{logId}")
    public ApiResponse<AuditLogDetailResponse> getAuditLogDetail(@PathVariable Long logId) {
        return ApiResponse.success(adminAuditLogService.getAuditLogDetail(logId));
    }
}
