package com.example.kiosk_backend.service;

import com.example.kiosk_backend.common.exception.BusinessException;
import com.example.kiosk_backend.common.exception.ErrorCode;
import com.example.kiosk_backend.common.response.PageResponse;
import com.example.kiosk_backend.dto.response.AuditLogDetailResponse;
import com.example.kiosk_backend.dto.response.AuditLogListItemResponse;
import com.example.kiosk_backend.entity.AdminAction;
import com.example.kiosk_backend.repository.AdminAuditLogRepository;
import com.example.kiosk_backend.repository.spec.AdminAuditLogSpecifications;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 감사 로그 조회 — /api/admin/audit-logs */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAuditLogService {

    private final AdminAuditLogRepository adminAuditLogRepository;
    private final ObjectMapper objectMapper;

    public PageResponse<AuditLogListItemResponse> getAuditLogs(AdminAction action, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Specification<com.example.kiosk_backend.entity.AdminAuditLog> spec = Specification
                .where(AdminAuditLogSpecifications.actionEquals(action))
                .and(AdminAuditLogSpecifications.createdAtFrom(startDate != null ? startDate.atStartOfDay() : null))
                .and(AdminAuditLogSpecifications.createdAtTo(endDate != null ? endToInclusive(endDate) : null));

        Pageable sorted = pageable.getSort().isSorted()
                ? pageable
                : org.springframework.data.domain.PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<AuditLogListItemResponse> page = adminAuditLogRepository.findAll(spec, sorted)
                .map(AuditLogListItemResponse::from);
        return PageResponse.of(page);
    }

    public AuditLogDetailResponse getAuditLogDetail(Long logId) {
        var log = adminAuditLogRepository.findById(logId)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUDIT_LOG_NOT_FOUND));
        return AuditLogDetailResponse.from(log, objectMapper);
    }

    private LocalDateTime endToInclusive(LocalDate endDate) {
        return endDate.atTime(23, 59, 59);
    }
}
