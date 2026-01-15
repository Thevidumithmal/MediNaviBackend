package com.example.pharmacybackend.dto.stock;

import jakarta.validation.constraints.NotNull;

public class UpdateStockRequest {
    @NotNull private Integer quantity;
    @NotNull private Double price;

    public UpdateStockRequest() {}

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}
