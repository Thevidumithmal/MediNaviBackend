package com.example.pharmacybackend.service;

import com.example.pharmacybackend.dto.auth.*;
import com.example.pharmacybackend.entity.*;
import com.example.pharmacybackend.exception.BadRequestException;
import com.example.pharmacybackend.repository.*;
import com.example.pharmacybackend.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final PharmacyRepository pharmacyRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepo, PharmacyRepository pharmacyRepo, PasswordEncoder encoder, JwtService jwtService) {
        this.userRepo = userRepo;
        this.pharmacyRepo = pharmacyRepo;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    public UserResponse register(RegisterRequest req) {
        if (userRepo.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        User u = new User();
        u.setName(req.getName());
        u.setEmail(req.getEmail());
        u.setPassword(encoder.encode(req.getPassword()));
        u.setRole(req.getRole());
        u = userRepo.save(u);

        Long pharmacyId = null;

        if (u.getRole() == Role.PHARMACY) {
            Pharmacy p = new Pharmacy();
            p.setOwner(u);
            p.setName(req.getPharmacyName() != null ? req.getPharmacyName() : u.getName() + " Pharmacy");
            p.setAddress(req.getAddress());
            p.setLatitude(req.getLatitude());
            p.setLongitude(req.getLongitude());
            p = pharmacyRepo.save(p);
            pharmacyId = p.getId();
        }

        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole(), pharmacyId);
    }

    public LoginResponse login(LoginRequest req) {
        User u = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!encoder.matches(req.getPassword(), u.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }

        Long pharmacyId = null;
        if (u.getRole() == Role.PHARMACY) {
            pharmacyId = pharmacyRepo.findByOwnerId(u.getId()).map(Pharmacy::getId).orElse(null);
        }

        String token = jwtService.generateToken(u.getEmail(), u.getRole());
        UserResponse userRes = new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole(), pharmacyId);

        return new LoginResponse(token, userRes);
    }
}

