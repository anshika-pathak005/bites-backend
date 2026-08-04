package com.bites.repository;

import com.bites.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Data access for admin accounts.
public interface AdminRepository extends JpaRepository<Admin, Long> {
    // Used during login to find the admin account matching the entered email.
    Optional<Admin> findByEmail(String email);
    // Used when creating a new admin to check the email isn't already taken.
    boolean existsByEmail(String email);
}
