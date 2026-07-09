package com.example.kiosk_backend.service;

import com.example.kiosk_backend.common.exception.BusinessException;
import com.example.kiosk_backend.common.exception.ErrorCode;
import com.example.kiosk_backend.config.FileStorageProperties;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 메뉴 이미지를 서버 로컬 디스크에 저장하고, DB(menus.image_url)에 저장할 공개 URL 경로를 반환한다.
 * DB.md 설계 원칙(서버 파일 업로드 후 URL 경로만 DB에 저장)을 그대로 구현한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final FileStorageProperties fileStorageProperties;

    private Path storageRoot;

    @PostConstruct
    void init() {
        this.storageRoot = Path.of(fileStorageProperties.dir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(storageRoot);
        } catch (IOException e) {
            throw new IllegalStateException("이미지 업로드 디렉터리를 생성할 수 없습니다: " + storageRoot, e);
        }
    }

    /**
     * 업로드된 파일을 저장하고 접근 가능한 URL 경로(예: /images/menu/{uuid}.jpg)를 반환한다.
     */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.EMPTY_FILE);
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_FILE);
        }

        String extension = extensionOf(contentType);
        String storedFilename = UUID.randomUUID() + extension;
        Path targetPath = storageRoot.resolve(storedFilename).normalize();

        if (!targetPath.getParent().equals(storageRoot)) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_FILE);
        }

        try (var inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("이미지 파일 저장 실패: {}", storedFilename, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        String urlPrefix = fileStorageProperties.urlPrefix();
        return (urlPrefix.endsWith("/") ? urlPrefix : urlPrefix + "/") + storedFilename;
    }

    /**
     * 저장된 이미지 파일을 파일명으로 조회해 바이트와 Content-Type을 함께 반환한다.
     * Swagger 등에서 "Try it out"으로 바로 미리보기 렌더링이 가능하도록 실제 컨트롤러 응답에 사용된다.
     */
    public StoredImage load(String filename) {
        if (filename == null || filename.isBlank()
                || filename.contains("/") || filename.contains("\\") || filename.contains("..")) {
            throw new BusinessException(ErrorCode.IMAGE_NOT_FOUND);
        }

        Path targetPath = storageRoot.resolve(filename).normalize();
        if (!targetPath.getParent().equals(storageRoot) || !Files.isRegularFile(targetPath)) {
            throw new BusinessException(ErrorCode.IMAGE_NOT_FOUND);
        }

        try {
            byte[] content = Files.readAllBytes(targetPath);
            return new StoredImage(content, mediaTypeOf(filename));
        } catch (IOException e) {
            log.error("이미지 파일 조회 실패: {}", filename, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private MediaType mediaTypeOf(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }
        if (lower.endsWith(".webp")) {
            return MediaType.valueOf("image/webp");
        }
        return MediaType.IMAGE_JPEG;
    }

    private String extensionOf(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> "";
        };
    }

    /** 조회된 이미지 바이트와 그 Content-Type을 함께 담는 캐리어. */
    public record StoredImage(byte[] content, MediaType contentType) {
    }
}
