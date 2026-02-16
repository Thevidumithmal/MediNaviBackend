package com.example.pharmacybackend.service;

import com.example.pharmacybackend.dto.auth.*;
import com.example.pharmacybackend.entity.*;
import com.example.pharmacybackend.exception.BadRequestException;
import com.example.pharmacybackend.exception.ForbiddenException;
import com.example.pharmacybackend.repository.UserRepository;
import com.example.pharmacybackend.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepo, PasswordEncoder encoder, JwtService jwtService) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    // Customer self-register only (PHARMACY created by admin)
    public UserResponse register(RegisterRequest req) {
        if (userRepo.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        // ✅ avoid DB crash if username duplicate
        if (req.getUsername() != null && !req.getUsername().isBlank()
                && userRepo.existsByUsername(req.getUsername())) {
            throw new BadRequestException("Username already exists");
        }

        User u = new User();
        u.setName(req.getName());
        u.setEmail(req.getEmail());
        u.setUsername(req.getUsername());
        u.setPhone(req.getPhone());
        u.setPassword(encoder.encode(req.getPassword()));

        // ✅ protect: if someone sends PHARMACY/ADMIN via register, force CUSTOMER
        u.setRole(Role.CUSTOMER);

        u = userRepo.save(u);

        return UserResponseMapper.toUserResponse(u);
    }

    public LoginResponse login(LoginRequest req) {

        User u = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        // ✅ block inactive users
        if (u.getStatus() == UserStatus.INACTIVE) {
            throw new ForbiddenException("Your account is INACTIVE. Please contact the administrator.");
        }

        if (!encoder.matches(req.getPassword(), u.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }

        // ✅ pharmacy users must have pharmacy assigned by admin
        if (u.getRole() == Role.PHARMACY && u.getPharmacy() == null) {
            throw new BadRequestException("No pharmacy assigned. Please contact administrator.");
        }

        String token = jwtService.generateToken(u.getEmail(), u.getRole());

        return new LoginResponse(token, UserResponseMapper.toUserResponse(u));
    }
}
