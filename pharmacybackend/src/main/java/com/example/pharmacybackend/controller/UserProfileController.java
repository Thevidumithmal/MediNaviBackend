package com.example.pharmacybackend.controller;

import com.example.pharmacybackend.dto.auth.UpdateProfileRequest;
import com.example.pharmacybackend.dto.auth.UserResponse;
import com.example.pharmacybackend.dto.auth.UserResponseMapper;
import com.example.pharmacybackend.entity.User;
import com.example.pharmacybackend.exception.BadRequestException;
import com.example.pharmacybackend.repository.UserRepository;
import com.example.pharmacybackend.security.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserProfileController {

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;

    public UserProfileController(CurrentUserService currentUserService,
                                 UserRepository userRepository) {
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
    }

    // ✅ GET current profile
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me() {
        User u = currentUserService.getCurrentUser();
        return ResponseEntity.ok(UserResponseMapper.toUserResponse(u));
    }

    // ✅ UPDATE current profile (username + phone ONLY)
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
}
