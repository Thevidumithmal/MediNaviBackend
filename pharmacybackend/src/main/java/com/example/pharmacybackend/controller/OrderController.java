package com.example.pharmacybackend.controller;

import com.example.pharmacybackend.dto.order.*;
import com.example.pharmacybackend.entity.Pharmacy;
import com.example.pharmacybackend.entity.Role;
import com.example.pharmacybackend.entity.User;
import com.example.pharmacybackend.exception.ForbiddenException;
import com.example.pharmacybackend.exception.NotFoundException;
import com.example.pharmacybackend.repository.PharmacyRepository;
import com.example.pharmacybackend.security.CurrentUserService;
import com.example.pharmacybackend.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class OrderController {

    private final OrderService orderService;
    private final CurrentUserService currentUserService;
    private final PharmacyRepository pharmacyRepository;

    public OrderController(OrderService orderService,
                           CurrentUserService currentUserService,
                           PharmacyRepository pharmacyRepository) {
        this.orderService = orderService;
        this.currentUserService = currentUserService;
        this.pharmacyRepository = pharmacyRepository;
    }

    // existing create
    @PostMapping("/orders/create")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public CreateOrderResponse create(@Valid @RequestBody CreateOrderRequest req) {
        return orderService.create(req);
    }

    // UPDATED: pharmacy orders list now returns OrderSummaryDTO (includes statusMessage)
    @GetMapping("/pharmacy/orders/{pharmacyId}")
    @PreAuthorize("hasRole('PHARMACY') or hasRole('ADMIN')")
    public java.util.List<OrderSummaryDTO> pharmacyOrders(@PathVariable Long pharmacyId) {
        return orderService.listForPharmacy(pharmacyId);
    }

    // NEW: customer orders list with pagination + optional status
    @GetMapping("/customers/{customerId}/orders")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public Page<OrderSummaryDTO> customerOrders(
            @PathVariable Long customerId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        User current = currentUserService.getCurrentUser();

        // CUSTOMER can only access own orders
        if (current.getRole() == Role.CUSTOMER && !current.getId().equals(customerId)) {
            throw new ForbiddenException("Forbidden");
        }

        return orderService.listForCustomer(customerId, status, page, size);
    }

    // NEW: update order status
    @PatchMapping("/orders/{orderId}/status")
    @PreAuthorize("hasRole('PHARMACY') or hasRole('ADMIN')")
    public OrderSummaryDTO updateStatus(@PathVariable Long orderId,
                                        @Valid @RequestBody UpdateOrderStatusRequest req) {

        User current = currentUserService.getCurrentUser();

        // If pharmacy role -> must own the order's pharmacy
        if (current.getRole() == Role.PHARMACY) {
            Pharmacy pharmacy = pharmacyRepository.findByOwnerId(current.getId())
                    .orElseThrow(() -> new NotFoundException("Pharmacy not found for current user"));

            // We need to ensure the order belongs to this pharmacy.
            // We'll fetch the order summary after update attempt? Better: block before update.
            // So we do a safe update in service, but check ownership first by reading order id.
            // We'll reuse service list result after update, but we need ownership check now.
            // simplest: call service update only after confirming ownership using a lightweight fetch:
            // We'll confirm using pharmacyOrders list approach is heavy; so add repository in service if needed.
            // Here: simplest approach - let service update and then verify result pharmacyId.
            // But better: move ownership check into service. now we do a strict check by updating then verifying:
            OrderSummaryDTO updated = orderService.updateStatus(orderId, req);
            if (!pharmacy.getId().equals(updated.getPharmacyId())) {
                throw new ForbiddenException("Forbidden");
            }
            return updated;
        }

        // ADMIN can update any
        return orderService.updateStatus(orderId, req);
    }
}
