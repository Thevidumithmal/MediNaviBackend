package com.example.pharmacybackend.service;

import com.example.pharmacybackend.dto.admin.*;
import com.example.pharmacybackend.dto.admin.request.CreateUserRequest;
import com.example.pharmacybackend.entity.*;
import com.example.pharmacybackend.repository.PharmacyRepository;
import com.example.pharmacybackend.repository.PharmacyStockRepository;
import com.example.pharmacybackend.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.pharmacybackend.exception.BadRequestException;


import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepo;
    private final PharmacyRepository pharmacyRepo;
    private final PharmacyStockRepository stockRepo;
    private final PasswordEncoder passwordEncoder;

    public AdminService(UserRepository userRepo,
                        PharmacyRepository pharmacyRepo,
                        PharmacyStockRepository stockRepo,
                        PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.pharmacyRepo = pharmacyRepo;
        this.stockRepo = stockRepo;
        this.passwordEncoder = passwordEncoder;
    }

    // ---------- STATS ----------
    public AdminStatsDTO getSystemStats() {
        AdminStatsDTO dto = new AdminStatsDTO();

        dto.setTotalUsers(userRepo.count());
        dto.setActiveUsers(userRepo.countByStatus(UserStatus.ACTIVE));
        dto.setTotalPharmacies(pharmacyRepo.count());
        dto.setTotalMedicines(stockRepo.countDistinctMedicinesWithStock());

        // Users by role
        List<UsersByRoleDTO> usersByRole = List.of(
                new UsersByRoleDTO("CUSTOMER", userRepo.countByRole(Role.CUSTOMER)),
                new UsersByRoleDTO("PHARMACY", userRepo.countByRole(Role.PHARMACY)),
                new UsersByRoleDTO("ADMIN", userRepo.countByRole(Role.ADMIN))
        );
        dto.setUsersByRole(usersByRole);

        // Pharmacies by region (extract from address last comma part)
        Map<String, Long> regionCounts = pharmacyRepo.findAll().stream()
                .collect(Collectors.groupingBy(p -> extractRegion(p.getAddress()), Collectors.counting()));

        List<PharmaciesByRegionDTO> pharmaciesByRegion = regionCounts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .map(e -> new PharmaciesByRegionDTO(e.getKey(), e.getValue()))
                .toList();

        dto.setPharmaciesByRegion(pharmaciesByRegion);

        // Top medicines
        dto.setTopMedicines(stockRepo.findTopMedicinesByStock(PageRequest.of(0, 10)));

        return dto;
    }

    private String extractRegion(String address) {
        if (address == null || address.isBlank()) return "Unknown";
        String[] parts = address.split(",");
        String last = parts[parts.length - 1].trim();
        return last.isBlank() ? "Unknown" : last;
    }

    // ---------- PHARMACY CRUD ----------
    public List<PharmacyDTO> getAllPharmacies() {
        return pharmacyRepo.findAll().stream().map(this::toPharmacyDTO).toList();
    }

    public PharmacyDTO createPharmacy(PharmacyDTO req) {
        Pharmacy p = new Pharmacy();
        p.setName(req.getName());
        p.setAddress(req.getAddress());
        p.setPhone(req.getPhone());
        p.setLatitude(req.getLatitude());
        p.setLongitude(req.getLongitude());
        p.setOwner(null); // admin can create without owner
        return toPharmacyDTO(pharmacyRepo.save(p));
    }

    public void deletePharmacy(Long id) {
        // If FK constraints block deletion (stock/orders), you may need soft-delete later.
        pharmacyRepo.deleteById(id);
    }

    // ---------- USER CRUD ----------
    public List<UserDTO> getAllUsers(String roleFilter) {
        List<User> users;
        if (roleFilter == null || roleFilter.isBlank()) {
            users = userRepo.findAll();
        } else {
            Role r = Role.valueOf(roleFilter.toUpperCase());
            users = userRepo.findByRole(r);
        }
        return users.stream().map(this::toUserDTO).toList();
    }

    public UserDTO createUser(CreateUserRequest req) {

        if (req.getEmail() == null || req.getEmail().isBlank()) {
            throw new BadRequestException("Email is required");
        }
        if (req.getPassword() == null || req.getPassword().isBlank()) {
            throw new BadRequestException("Password is required");
        }
        if (req.getRole() == null || req.getRole().isBlank()) {
            throw new BadRequestException("Role is required");
        }

        if (userRepo.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email already exists");
        }
        if (req.getUsername() != null && !req.getUsername().isBlank()
                && userRepo.existsByUsername(req.getUsername())) {
            throw new BadRequestException("Username already exists");
        }

        Role role;
        try {
            role = Role.valueOf(req.getRole().toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("Invalid role: " + req.getRole());
        }

        User u = new User();
        u.setEmail(req.getEmail());
        u.setName(req.getName());
        u.setUsername(req.getUsername());
        u.setPhone(req.getPhone());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setStatus(UserStatus.ACTIVE);
        u.setRole(role);

        // ✅ ONLY admin can create PHARMACY users
        if (role == Role.PHARMACY) {
            if (req.getPharmacyId() == null) {
                throw new BadRequestException("pharmacyId is required for PHARMACY role");
            }

            Pharmacy p = pharmacyRepo.findById(req.getPharmacyId())
                    .orElseThrow(() ->
                            new BadRequestException("Pharmacy not found: " + req.getPharmacyId())
                    );

            u.setPharmacy(p);
        } else {
            u.setPharmacy(null);
        }

        return toUserDTO(userRepo.save(u));
    }



    public UserDTO updateUserStatus(Long userId, String status) {
        User u = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        u.setStatus(UserStatus.valueOf(status.toUpperCase()));
        return toUserDTO(userRepo.save(u));
    }

    public void changeUserPassword(Long userId, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("newPassword is required");
        }

        User u = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        u.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(u);
    }

    public void deleteUser(Long userId) {
        userRepo.deleteById(userId);
    }

    // ---------- MAPPERS ----------
    private PharmacyDTO toPharmacyDTO(Pharmacy p) {
        PharmacyDTO dto = new PharmacyDTO();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setAddress(p.getAddress());
        dto.setPhone(p.getPhone());
        dto.setLatitude(p.getLatitude());
        dto.setLongitude(p.getLongitude());
        return dto;
    }

    private UserDTO toUserDTO(User u) {
        UserDTO dto = new UserDTO();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setName(u.getName());
        dto.setEmail(u.getEmail());
        dto.setPhone(u.getPhone());
        dto.setRole(u.getRole().name());
        dto.setStatus(u.getStatus().name());

        if (u.getPharmacy() != null) {
            dto.setPharmacyId(u.getPharmacy().getId());
            dto.setPharmacyName(u.getPharmacy().getName());
        } else {
            dto.setPharmacyId(null);
            dto.setPharmacyName(null);
        }

        return dto;
    }


}
