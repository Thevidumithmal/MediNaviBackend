package com.example.pharmacybackend.repository;

import com.example.pharmacybackend.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;

public interface InvoiceRepository extends JpaRepository<Invoice, Long>, JpaSpecificationExecutor<Invoice> {

    Page<Invoice> findByPharmacyIdOrderByCreatedAtDesc(Long pharmacyId, Pageable pageable);

    Page<Invoice> findByPharmacyIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long pharmacyId, LocalDateTime from, LocalDateTime to, Pageable pageable
    );
}
