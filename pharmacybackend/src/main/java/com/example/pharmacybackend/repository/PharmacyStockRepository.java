package com.example.pharmacybackend.repository;

import com.example.pharmacybackend.entity.PharmacyStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

}
