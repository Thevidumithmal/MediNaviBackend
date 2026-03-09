package com.example.pharmacybackend.dto.auth;

public class UpdateProfileRequest {
    private String username;
    private String phone;

    public UpdateProfileRequest() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
