package com.example.pharmacybackend.dto.admin;

public class UsersByRoleDTO {
    private String role;
    private long count;

    public UsersByRoleDTO(String role, long count) {
        this.role = role;
        this.count = count;
    }

    public String getRole() { return role; }
    public long getCount() { return count; }
}

