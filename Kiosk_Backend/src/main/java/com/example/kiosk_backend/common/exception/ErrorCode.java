package com.example.kiosk_backend.common.exception;

import org.springframework.http.HttpStatus;

/** Backend.md 4장 "에러 코드 정의"에 대응하는 코드 목록 */
public enum ErrorCode {

    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),
    SESSION_ID_REQUIRED(HttpStatus.BAD_REQUEST, "X-Session-Id 헤더가 필요합니다."),
    MENU_INACTIVE(HttpStatus.BAD_REQUEST, "판매 중이 아닌 메뉴입니다."),
    MENU_SOLD_OUT(HttpStatus.BAD_REQUEST, "품절된 메뉴입니다."),
    MENU_UNAVAILABLE(HttpStatus.BAD_REQUEST, "주문할 수 없는 메뉴가 포함되어 있습니다."),
    CART_EMPTY(HttpStatus.BAD_REQUEST, "장바구니가 비어 있습니다."),
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "재고가 부족합니다."),
    NOT_A_SET_MENU(HttpStatus.BAD_REQUEST, "세트 메뉴가 아닙니다."),
    COMPONENT_MUST_BE_SINGLE_ITEM(HttpStatus.BAD_REQUEST, "세트 구성품은 단품 메뉴여야 합니다."),
    SELF_REFERENCE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "자기 자신을 구성품으로 추가할 수 없습니다."),
    EMPTY_FILE(HttpStatus.BAD_REQUEST, "업로드할 파일이 비어 있습니다."),
    INVALID_IMAGE_FILE(HttpStatus.BAD_REQUEST, "이미지 파일(JPG, PNG, WEBP)만 업로드할 수 있습니다."),
    FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "파일 크기는 5MB를 초과할 수 없습니다."),

    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),

    ACCOUNT_INACTIVE(HttpStatus.FORBIDDEN, "비활성화된 관리자 계정입니다."),
    CART_ACCESS_FORBIDDEN(HttpStatus.FORBIDDEN, "본인 세션의 장바구니만 접근할 수 있습니다."),

    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다."),
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "메뉴를 찾을 수 없습니다."),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니 항목을 찾을 수 없습니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
    SET_COMPONENT_NOT_FOUND(HttpStatus.NOT_FOUND, "세트 구성품을 찾을 수 없습니다."),
    AUDIT_LOG_NOT_FOUND(HttpStatus.NOT_FOUND, "감사 로그를 찾을 수 없습니다."),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "이미지를 찾을 수 없습니다."),

    CATEGORY_NAME_DUPLICATE(HttpStatus.CONFLICT, "이미 존재하는 카테고리명입니다."),
    CATEGORY_HAS_MENUS(HttpStatus.CONFLICT, "소속된 메뉴가 있어 삭제할 수 없습니다."),
    SET_COMPONENT_DUPLICATE(HttpStatus.CONFLICT, "이미 추가된 구성품입니다."),
    MENU_IN_USE_AS_SET_COMPONENT(HttpStatus.CONFLICT, "세트 메뉴에서 사용 중인 메뉴는 삭제할 수 없습니다."),
    ORDER_ALREADY_CANCELLED(HttpStatus.CONFLICT, "이미 취소된 주문입니다."),

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "일시적인 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
