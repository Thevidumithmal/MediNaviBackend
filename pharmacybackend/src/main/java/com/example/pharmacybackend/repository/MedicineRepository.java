package com.example.pharmacybackend.repository;

import com.example.pharmacybackend.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    Optional<Medicine> findByNameIgnoreCase(String name);

    // ✅ distinct + ordered list to help RapidFuzz and avoid duplicates
    @Query("select distinct m.name from Medicine m where m.name is not null order by m.name asc")
    List<String> findAllNames();

}
