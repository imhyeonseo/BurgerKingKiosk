package com.example.kiosk_backend.controller.admin;

import com.example.kiosk_backend.common.response.ApiResponse;
import com.example.kiosk_backend.dto.response.ImageUploadResponse;
import com.example.kiosk_backend.service.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** 메뉴 이미지 업로드 — /api/admin/images (관리자 인증 필요) */
@RestController
@RequestMapping("/api/admin/images")
@RequiredArgsConstructor
public class AdminImageController {

    private final ImageStorageService imageStorageService;

    @PostMapping("/menu")
    public ResponseEntity<ApiResponse<ImageUploadResponse>> uploadMenuImage(@RequestParam("file") MultipartFile file) {
        String imageUrl = imageStorageService.store(file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(new ImageUploadResponse(imageUrl)));
    }
}
