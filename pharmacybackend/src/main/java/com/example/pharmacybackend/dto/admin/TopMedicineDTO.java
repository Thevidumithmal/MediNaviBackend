package com.example.pharmacybackend.dto.admin;

public class TopMedicineDTO {
    private String name;
    private long totalStock;
    private long pharmacyCount;

    public TopMedicineDTO(String name, long totalStock, long pharmacyCount) {
        this.name = name;
        this.totalStock = totalStock;
        this.pharmacyCount = pharmacyCount;
    }

    public String getName() { return name; }
    public long getTotalStock() { return totalStock; }
    public long getPharmacyCount() { return pharmacyCount; }
}

