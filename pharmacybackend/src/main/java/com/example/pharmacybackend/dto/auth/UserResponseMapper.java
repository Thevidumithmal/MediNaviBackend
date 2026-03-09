package com.example.pharmacybackend.dto.auth;

import com.example.pharmacybackend.entity.Pharmacy;
import com.example.pharmacybackend.entity.User;

public class UserResponseMapper {

    private UserResponseMapper() {}

    public static UserResponse toUserResponse(User u) {
        Pharmacy p = u.getPharmacy();

        Long pharmacyId = (p != null) ? p.getId() : null;
        String pharmacyName = (p != null) ? p.getName() : null;

        return new UserResponse(
                u.getId(),
                u.getUsername(),
                u.getName(),
                u.getEmail(),
                u.getPhone(),
                u.getRole(),
                u.getStatus(),
                pharmacyId,
                pharmacyName
        );
    }
}

