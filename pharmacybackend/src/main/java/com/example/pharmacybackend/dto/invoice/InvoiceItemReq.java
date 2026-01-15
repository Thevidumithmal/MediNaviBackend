package com.example.pharmacybackend.dto.invoice;

public class InvoiceItemReq {
    private Long medicineId;
    private Integer quantity;
    private Double unitPrice;

    public InvoiceItemReq() {}

    public Long getMedicineId() { return medicineId; }
    public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }
}

