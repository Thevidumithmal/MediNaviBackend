package com.example.pharmacybackend.dto.order;

import java.util.List;

public class PharmacyOrderResponse {
    private Long id;
    private String customerName;
    private String status;
    private Double totalAmount;
    private String createdAt;
    private List<PharmacyOrderItemResponse> items;

    public PharmacyOrderResponse() {}

    public PharmacyOrderResponse(Long id, String customerName, String status, Double totalAmount, String createdAt, List<PharmacyOrderItemResponse> items) {
        this.id = id; this.customerName = customerName; this.status = status; this.totalAmount = totalAmount; this.createdAt = createdAt; this.items = items;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public List<PharmacyOrderItemResponse> getItems() { return items; }
    public void setItems(List<PharmacyOrderItemResponse> items) { this.items = items; }
}

