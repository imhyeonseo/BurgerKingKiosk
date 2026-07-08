package com.example.kiosk_backend.repository;

import com.example.kiosk_backend.entity.Menu;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MenuRepository extends JpaRepository<Menu, Long>, JpaSpecificationExecutor<Menu> {

    List<Menu> findByCategoryIdAndIsActiveTrueAndDeletedAtIsNull(Long categoryId);

    Optional<Menu> findByIdAndIsActiveTrueAndDeletedAtIsNull(Long id);

    List<Menu> findByNameContainingIgnoreCaseAndIsActiveTrueAndDeletedAtIsNull(String keyword);

    long countByDeletedAtIsNullAndIsActiveTrue();

    long countByCategoryIdAndDeletedAtIsNull(Long categoryId);

    long countByDeletedAtIsNullAndIsActiveTrueAndQuantity(Integer quantity);

    Page<Menu> findByDeletedAtIsNull(Pageable pageable);

    Page<Menu> findByDeletedAtIsNullAndQuantity(Integer quantity, Pageable pageable);

    Page<Menu> findByDeletedAtIsNullAndQuantityGreaterThan(Integer quantity, Pageable pageable);

    /**
     * 재고를 조건부로 원자적으로 차감한다. 반환값이 0이면 재고 부족(동시성 실패)으로 간주한다.
     */
    @Modifying
    @Query("UPDATE Menu m SET m.quantity = m.quantity - :qty WHERE m.id = :menuId AND m.quantity >= :qty")
    int decreaseStock(@Param("menuId") Long menuId, @Param("qty") int qty);
}
