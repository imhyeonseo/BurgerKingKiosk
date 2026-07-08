package com.example.kiosk_backend.repository.spec;

import com.example.kiosk_backend.entity.Menu;
import org.springframework.data.jpa.domain.Specification;

public final class MenuSpecifications {

    private MenuSpecifications() {
    }

    public static Specification<Menu> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<Menu> categoryIdEquals(Long categoryId) {
        return (root, query, cb) -> categoryId == null ? null : cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Menu> isSetEquals(Boolean isSet) {
        return (root, query, cb) -> isSet == null ? null : cb.equal(root.get("isSet"), isSet);
    }

    public static Specification<Menu> isActiveEquals(Boolean isActive) {
        return (root, query, cb) -> isActive == null ? null : cb.equal(root.get("isActive"), isActive);
    }
}
