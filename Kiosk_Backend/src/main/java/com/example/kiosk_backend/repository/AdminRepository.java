package com.example.kiosk_backend.repository;

import com.example.kiosk_backend.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, String> {
}
