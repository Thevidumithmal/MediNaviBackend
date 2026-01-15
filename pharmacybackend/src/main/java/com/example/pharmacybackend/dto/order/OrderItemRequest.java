package com.example.pharmacybackend.dto.order;

import jakarta.validation.constraints.NotNull;

public class OrderItemRequest {
    @NotNull private Long medicineId;
    @NotNull private Integer quantity;
    private Double price;

    public OrderItemRequest() {}

    public Long getMedicineId() { return medicineId; }
    public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}
