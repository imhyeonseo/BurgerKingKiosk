package com.example.kiosk_backend.entity;

/** admin_audit_logs.action 코드 정의 (Backend.md 5장 참조) */
public enum AdminAction {
    LOGIN,
    LOGOUT,
    CATEGORY_CREATE,
    CATEGORY_UPDATE,
    CATEGORY_DELETE,
    MENU_CREATE,
    MENU_UPDATE,
    MENU_DELETE,
    SET_COMPONENT_ADD,
    SET_COMPONENT_REMOVE,
    INVENTORY_UPDATE,
    ORDER_CANCEL
}
