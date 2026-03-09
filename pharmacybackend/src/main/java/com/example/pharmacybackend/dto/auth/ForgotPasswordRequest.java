package com.example.pharmacybackend.dto.auth;

public class ForgotPasswordRequest {
    private String username;
    private String email;

    public ForgotPasswordRequest() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
