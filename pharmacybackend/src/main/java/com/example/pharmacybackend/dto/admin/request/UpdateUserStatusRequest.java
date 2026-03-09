package com.example.pharmacybackend.dto.admin.request;

public class UpdateUserStatusRequest {
    private String status; // ACTIVE | INACTIVE
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
