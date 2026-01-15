package com.example.pharmacybackend.repository;

import com.example.pharmacybackend.entity.Pharmacy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PharmacyRepository extends JpaRepository<Pharmacy, Long> {
    Optional<Pharmacy> findByOwnerId(Long ownerId);
}
