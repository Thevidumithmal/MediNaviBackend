package com.example.pharmacybackend.repository;

import com.example.pharmacybackend.entity.PharmacyStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;


import java.util.List;
import java.util.Optional;

public interface PharmacyStockRepository extends JpaRepository<PharmacyStock, Long> {
    List<PharmacyStock> findByPharmacyId(Long pharmacyId);

/*    @Query("""
      select ps from PharmacyStock ps
      where lower(ps.medicine.name) like lower(concat('%', :name, '%'))
    """)
    List<PharmacyStock> searchByMedicineName(String name);*/

    @Query("""
   SELECT ps FROM PharmacyStock ps
   WHERE LOWER(ps.medicine.name) LIKE LOWER(CONCAT('%', :name, '%'))
   AND ps.quantity > 0
""")
    List<PharmacyStock> searchByMedicineNameWithStock(@Param("name") String name);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ps FROM PharmacyStock ps WHERE ps.pharmacy.id = :pharmacyId AND ps.medicine.id = :medicineId")
    Optional<PharmacyStock> findForUpdate(@Param("pharmacyId") Long pharmacyId, @Param("medicineId") Long medicineId);

    @Query("""
   SELECT COUNT(DISTINCT ps.medicine.id)
   FROM PharmacyStock ps
   WHERE ps.quantity > 0
""")
    long countDistinctMedicinesWithStock();

    @Query("""
   SELECT new com.example.pharmacybackend.dto.admin.TopMedicineDTO(
     ps.medicine.name,
     SUM(ps.quantity),
     COUNT(DISTINCT ps.pharmacy.id)
   )
   FROM PharmacyStock ps
   WHERE ps.quantity > 0
   GROUP BY ps.medicine.id, ps.medicine.name
   ORDER BY SUM(ps.quantity) DESC
""")
    List<com.example.pharmacybackend.dto.admin.TopMedicineDTO> findTopMedicinesByStock(Pageable pageable);


}
