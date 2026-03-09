package com.example.pharmacybackend.controller;

import com.example.pharmacybackend.dto.admin.*;
import com.example.pharmacybackend.dto.admin.request.*;
import com.example.pharmacybackend.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // ----- STATS -----
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDTO> getStats() {
        return ResponseEntity.ok(adminService.getSystemStats());
    }

    // ----- PHARMACIES -----
    @GetMapping("/pharmacies")
    public ResponseEntity<List<PharmacyDTO>> getAllPharmacies() {
        return ResponseEntity.ok(adminService.getAllPharmacies());
    }

    @PostMapping("/pharmacies")
    public ResponseEntity<PharmacyDTO> createPharmacy(@RequestBody PharmacyDTO dto) {
        return ResponseEntity.ok(adminService.createPharmacy(dto));
    }

    @DeleteMapping("/pharmacies/{pharmacyId}")
    public ResponseEntity<Void> deletePharmacy(@PathVariable Long pharmacyId) {
        adminService.deletePharmacy(pharmacyId);
        return ResponseEntity.noContent().build();
    }

    // ----- USERS -----
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers(@RequestParam(required = false) String role) {
        return ResponseEntity.ok(adminService.getAllUsers(role));
    }

    @PostMapping("/users")
    public ResponseEntity<UserDTO> createUser(@RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(adminService.createUser(request));
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<UserDTO> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody UpdateUserStatusRequest request
    ) {
        return ResponseEntity.ok(adminService.updateUserStatus(userId, request.getStatus()));
    }

    @PutMapping("/users/{userId}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long userId,
            @RequestBody ChangePasswordRequest request
    ) {
        adminService.changeUserPassword(userId, request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    // optional
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}

