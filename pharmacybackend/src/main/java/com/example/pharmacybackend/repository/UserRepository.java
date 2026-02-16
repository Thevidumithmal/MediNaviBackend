package com.example.pharmacybackend.repository;

import com.example.pharmacybackend.entity.Role;
import com.example.pharmacybackend.entity.User;
import com.example.pharmacybackend.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // ✅ admin needs these
    long countByStatus(UserStatus status);
    long countByRole(Role role);
    List<User> findByRole(Role role);

    boolean existsByUsername(String username);
}
