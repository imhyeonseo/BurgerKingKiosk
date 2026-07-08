package com.example.kiosk_backend.repository;

import com.example.kiosk_backend.entity.SetMenuItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SetMenuItemRepository extends JpaRepository<SetMenuItem, Long> {

    List<SetMenuItem> findBySetMenuId(Long setMenuId);

    Optional<SetMenuItem> findBySetMenuIdAndComponentMenuId(Long setMenuId, Long componentMenuId);

    boolean existsBySetMenuIdAndComponentMenuId(Long setMenuId, Long componentMenuId);

    boolean existsByComponentMenuId(Long componentMenuId);

    List<SetMenuItem> findByComponentMenuId(Long componentMenuId);
}
