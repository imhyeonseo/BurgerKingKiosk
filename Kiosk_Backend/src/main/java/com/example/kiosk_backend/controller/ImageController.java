package com.example.kiosk_backend.controller;

import com.example.kiosk_backend.service.ImageStorageService;
import com.example.kiosk_backend.service.ImageStorageService.StoredImage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 메뉴 이미지 조회 — 인증 불필요(고객 키오스크/관리자 화면 모두에서 <img src="..."> 로 바로 사용).
 * 응답 Content-Type을 image/*로 명시해 Swagger UI의 "Try it out" 결과창에서 바로 미리보기가 렌더링된다.
 */
@RestController
@RequiredArgsConstructor
public class ImageController {

    private final ImageStorageService imageStorageService;

    @GetMapping(value = "${app.upload.url-prefix}/{filename}",
            produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, "image/webp"})
    public ResponseEntity<byte[]> getMenuImage(@PathVariable String filename) {
        StoredImage image = imageStorageService.load(filename);
        return ResponseEntity.ok()
                .contentType(image.contentType())
                .body(image.content());
    }
}
