package com.example.pharmacybackend.service;

import com.example.pharmacybackend.dto.auth.*;
import com.example.pharmacybackend.entity.*;
import com.example.pharmacybackend.exception.BadRequestException;
import com.example.pharmacybackend.exception.ForbiddenException;
import com.example.pharmacybackend.exception.NotFoundException;
import com.example.pharmacybackend.repository.UserRepository;
import com.example.pharmacybackend.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    public AuthService(UserRepository userRepo,
                       PasswordEncoder encoder,
                       JwtService jwtService,
                       EmailService emailService) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    // ✅ Customer self-register only (PHARMACY created by admin)
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

    // ✅ Forgot Password (Public)
    public void forgotPassword(ForgotPasswordRequest req) {

        if (req.getUsername() == null || req.getUsername().isBlank()
                || req.getEmail() == null || req.getEmail().isBlank()) {
            throw new BadRequestException("Username and email are required");
        }

        String username = req.getUsername().trim(); // case-sensitive
        String email = req.getEmail().trim(); // ignore-case handled by repository method

        User user = userRepo.findByUsernameAndEmailIgnoreCase(username, email)
                .orElseThrow(() -> new NotFoundException("No user found with the provided username and email"));

        // ✅ Generate temp password (8 chars)
        String tempPassword = generateTempPassword(8);

        // ✅ Save hashed temp password
        user.setPassword(encoder.encode(tempPassword));
        userRepo.save(user);

        // ✅ Send email
        try {
            emailService.sendTemporaryPasswordEmail(user.getEmail(), user.getName(), tempPassword);
        } catch (Exception e) {
            // If email sending fails, return clean message
            throw new RuntimeException("Failed to send email. Please try again later");
        }
    }

    private String generateTempPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}