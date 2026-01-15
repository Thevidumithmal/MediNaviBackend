package com.example.pharmacybackend.repository;

import com.example.pharmacybackend.entity.Order;
import com.example.pharmacybackend.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByPharmacyIdOrderByCreatedAtDesc(Long pharmacyId);

    Page<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId, Pageable pageable);

    Page<Order> findByCustomerIdAndStatusOrderByCreatedAtDesc(Long customerId, OrderStatus status, Pageable pageable);
}
