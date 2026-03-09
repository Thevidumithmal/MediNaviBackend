package com.example.pharmacybackend.dto.admin;

public class PharmaciesByRegionDTO {
    private String region;
    private long count;

    public PharmaciesByRegionDTO(String region, long count) {
        this.region = region;
        this.count = count;
    }

    public String getRegion() { return region; }
    public long getCount() { return count; }
}

