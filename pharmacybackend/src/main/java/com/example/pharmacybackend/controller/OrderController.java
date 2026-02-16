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
   // private final PharmacyRepository pharmacyRepository;

    public OrderController(OrderService orderService,
                           CurrentUserService currentUserService) {
        this.orderService = orderService;
        this.currentUserService = currentUserService;
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

    @PatchMapping("/orders/{orderId}/status")
    @PreAuthorize("hasRole('PHARMACY') or hasRole('ADMIN')")
    public OrderSummaryDTO updateStatus(@PathVariable Long orderId,
                                        @Valid @RequestBody UpdateOrderStatusRequest req) {

        User current = currentUserService.getCurrentUser();

        // PHARMACY users: must have pharmacy assigned
        if (current.getRole() == Role.PHARMACY) {

            Pharmacy pharmacy = current.getPharmacy();
            if (pharmacy == null) {
                throw new NotFoundException("No pharmacy assigned to this account. Contact admin.");
            }

            // Update order
            OrderSummaryDTO updated = orderService.updateStatus(orderId, req);

            // Security check: order must belong to this pharmacy
            if (!pharmacy.getId().equals(updated.getPharmacyId())) {
                throw new ForbiddenException("Forbidden");
            }

            return updated;
        }

        // ADMIN can update any order
        return orderService.updateStatus(orderId, req);
    }

}
