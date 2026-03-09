package com.example.pharmacybackend.dto.admin;

import java.util.List;

public class AdminStatsDTO {
    private long totalUsers;
    private long activeUsers;
    private long totalPharmacies;
    private long totalMedicines;

    private List<UsersByRoleDTO> usersByRole;
    private List<PharmaciesByRegionDTO> pharmaciesByRegion;
    private List<TopMedicineDTO> topMedicines;

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public long getActiveUsers() { return activeUsers; }
    public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }

    public long getTotalPharmacies() { return totalPharmacies; }
    public void setTotalPharmacies(long totalPharmacies) { this.totalPharmacies = totalPharmacies; }

    public long getTotalMedicines() { return totalMedicines; }
    public void setTotalMedicines(long totalMedicines) { this.totalMedicines = totalMedicines; }

    public List<UsersByRoleDTO> getUsersByRole() { return usersByRole; }
    public void setUsersByRole(List<UsersByRoleDTO> usersByRole) { this.usersByRole = usersByRole; }

    public List<PharmaciesByRegionDTO> getPharmaciesByRegion() { return pharmaciesByRegion; }
    public void setPharmaciesByRegion(List<PharmaciesByRegionDTO> pharmaciesByRegion) { this.pharmaciesByRegion = pharmaciesByRegion; }

    public List<TopMedicineDTO> getTopMedicines() { return topMedicines; }
    public void setTopMedicines(List<TopMedicineDTO> topMedicines) { this.topMedicines = topMedicines; }
}

