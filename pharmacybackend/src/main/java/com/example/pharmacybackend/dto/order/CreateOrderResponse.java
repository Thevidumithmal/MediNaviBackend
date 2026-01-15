package com.example.pharmacybackend.dto.order;

public class CreateOrderResponse {
    private Long id;
    private String status;
    private String createdAt;

    public CreateOrderResponse() {}

    public CreateOrderResponse(Long id, String status, String createdAt) {
        this.id = id; this.status = status; this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}

