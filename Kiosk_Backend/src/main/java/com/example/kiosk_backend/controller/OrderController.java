package com.example.kiosk_backend.controller;

import com.example.kiosk_backend.common.response.ApiResponse;
import com.example.kiosk_backend.dto.response.OrderCreateResponse;
import com.example.kiosk_backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderCreateResponse>> createOrder(@RequestHeader("X-Session-Id") String sessionId) {
        OrderCreateResponse response = orderService.createOrder(sessionId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }
}
