package com.example.pharmacybackend.controller;

import com.example.pharmacybackend.dto.auth.ChangePasswordRequest;
import com.example.pharmacybackend.dto.auth.UpdateProfileRequest;
import com.example.pharmacybackend.dto.auth.UserResponse;
import com.example.pharmacybackend.dto.auth.UserResponseMapper;
import com.example.pharmacybackend.entity.User;
import com.example.pharmacybackend.exception.BadRequestException;
import com.example.pharmacybackend.repository.UserRepository;
import com.example.pharmacybackend.security.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserProfileController {

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserProfileController(CurrentUserService currentUserService,
                                 UserRepository userRepository,
                                 PasswordEncoder passwordEncoder) {
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // GET current profile
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me() {
        User u = currentUserService.getCurrentUser();
        return ResponseEntity.ok(UserResponseMapper.toUserResponse(u));
    }

    // UPDATE current profile (username + phone ONLY)
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMe(@RequestBody UpdateProfileRequest req) {

        User u = currentUserService.getCurrentUser();

        // ----------------------------
        // Username validation & update
        // ----------------------------
        if (req.getUsername() != null) {
            String newUsername = req.getUsername().trim();

            // allow empty -> treat as null (optional)
            if (newUsername.isEmpty()) {
                newUsername = null;
            }

            // if changed
            String currentUsername = u.getUsername();
            boolean changed = (newUsername == null && currentUsername != null)
                    || (newUsername != null && !newUsername.equals(currentUsername));

            if (changed) {
                if (newUsername != null) {
                    // format check: 3-50, alphanumeric + underscore
                    if (!newUsername.matches("^[a-zA-Z0-9_]{3,50}$")) {
                        throw new BadRequestException("Invalid username format");
                    }

                    // unique check
                    if (userRepository.existsByUsername(newUsername)) {
                        throw new BadRequestException("Username already exists");
                    }
                }
                u.setUsername(newUsername);
            }
        }

        // ----------------------------
        // Phone validation & update
        // ----------------------------
        if (req.getPhone() != null) {
            String phone = req.getPhone().trim();

            // allow empty -> null
            if (phone.isEmpty()) {
                phone = null;
            }

            // validate only if not null
            if (phone != null) {
                // Sri Lanka-friendly pattern (matches +94XXXXXXXXX or 0XXXXXXXXX)
                if (!phone.matches("^(\\+94|0)?[0-9]{9,10}$")) {
                    throw new BadRequestException("Invalid phone number format");
                }
            }

            u.setPhone(phone);
        }

        // save changes
        u = userRepository.save(u);

        // return full updated profile (with pharmacyName if exists)
        return ResponseEntity.ok(UserResponseMapper.toUserResponse(u));
    }

    // CHANGE PASSWORD (currentPassword + newPassword)
    @PutMapping("/me/password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest req) {

        User user = currentUserService.getCurrentUser();

        if (req.getCurrentPassword() == null || req.getCurrentPassword().isBlank()) {
            throw new BadRequestException("Current password is required");
        }

        if (req.getNewPassword() == null || req.getNewPassword().length() < 6) {
            throw new BadRequestException("Password must be at least 6 characters");
        }

        // Verify current password
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Prevent reuse of same password
        if (passwordEncoder.matches(req.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
}
