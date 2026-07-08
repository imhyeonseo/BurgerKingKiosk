package com.example.kiosk_backend.repository;

import com.example.kiosk_backend.entity.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByIsActiveTrueOrderByDisplayOrderAsc();

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    Optional<Category> findByName(String name);
}
