package com.example.pharmacybackend.dto.order;

public class PharmacyOrderItemResponse {
    private Long medicineId;
    private String medicineName;
    private Integer quantity;
    private Double price;

    public PharmacyOrderItemResponse() {}

    public PharmacyOrderItemResponse(Long medicineId, String medicineName, Integer quantity, Double price) {
        this.medicineId = medicineId; this.medicineName = medicineName; this.quantity = quantity; this.price = price;
    }

    public Long getMedicineId() { return medicineId; }
    public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}

