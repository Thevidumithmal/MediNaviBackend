package com.example.pharmacybackend.service;

import com.example.pharmacybackend.dto.order.*;
import com.example.pharmacybackend.entity.*;
import com.example.pharmacybackend.exception.BadRequestException;
import com.example.pharmacybackend.exception.NotFoundException;
import com.example.pharmacybackend.repository.*;
import com.example.pharmacybackend.security.CurrentUserService;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final UserRepository userRepo;
    private final PharmacyRepository pharmacyRepo;
    private final MedicineRepository medicineRepo;
    private final PharmacyStockRepository stockRepo;
    private final OrderRepository orderRepo;
    private final CurrentUserService currentUserService;

    public OrderService(UserRepository userRepo,
                        PharmacyRepository pharmacyRepo,
                        MedicineRepository medicineRepo,
                        PharmacyStockRepository stockRepo,
                        OrderRepository orderRepo,
                        CurrentUserService currentUserService) {
        this.userRepo = userRepo;
        this.pharmacyRepo = pharmacyRepo;
        this.medicineRepo = medicineRepo;
        this.stockRepo = stockRepo;
        this.orderRepo = orderRepo;
        this.currentUserService = currentUserService;
    }

    // EXISTING: create order (updated to enum + statusMessage + updatedAt)
    public CreateOrderResponse create(CreateOrderRequest req) {

        User customer = userRepo.findById(req.getCustomerId())
                .orElseThrow(() -> new NotFoundException("Customer not found"));

        Pharmacy pharmacy = pharmacyRepo.findById(req.getPharmacyId())
                .orElseThrow(() -> new NotFoundException("Pharmacy not found"));

        Order order = new Order();
        order.setCustomer(customer);
        order.setPharmacy(pharmacy);
        order.setStatus(OrderStatus.PENDING);
        order.setStatusMessage(null);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setTotalAmount(0.0);

        double total = 0.0;

        for (OrderItemRequest itemReq : req.getItems()) {
            Medicine med = medicineRepo.findById(itemReq.getMedicineId())
                    .orElseThrow(() -> new NotFoundException("Medicine not found: " + itemReq.getMedicineId()));

            PharmacyStock stock = null;
            List<PharmacyStock> stocks = stockRepo.findByPharmacyId(pharmacy.getId());
            for (PharmacyStock s : stocks) {
                if (s.getMedicine().getId().equals(med.getId())) { stock = s; break; }
            }
            if (stock == null) throw new BadRequestException("Medicine not available in selected pharmacy");

            int qty = itemReq.getQuantity();
            if (qty <= 0) throw new BadRequestException("Quantity must be > 0");
            if (stock.getQuantity() < qty) throw new BadRequestException("Not enough stock for: " + med.getName());

            double price = stock.getPrice();

            stock.setQuantity(stock.getQuantity() - qty);
            stockRepo.save(stock);

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setMedicine(med);
            oi.setQuantity(qty);
            oi.setPrice(price);

            order.getItems().add(oi);
            total += price * qty;
        }

        order.setTotalAmount(total);
        Order saved = orderRepo.save(order);

        return new CreateOrderResponse(saved.getId(), saved.getStatus().name(), saved.getCreatedAt().toString());
    }

    // EXISTING: pharmacy orders list (now returns OrderSummaryDTO with statusMessage/items)
    public List<OrderSummaryDTO> listForPharmacy(Long pharmacyId) {
        List<Order> orders = orderRepo.findByPharmacyIdOrderByCreatedAtDesc(pharmacyId);
        List<OrderSummaryDTO> out = new ArrayList<>();
        for (Order o : orders) out.add(toSummary(o));
        return out;
    }

    // NEW: customer orders with pagination + optional status filter + security (self or admin checked in controller)
    public Page<OrderSummaryDTO> listForCustomer(Long customerId, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Order> orders;
        if (status == null || status.isBlank()) {
            orders = orderRepo.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable);
        } else {
            OrderStatus st;
            try {
                st = OrderStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid status");
            }
            orders = orderRepo.findByCustomerIdAndStatusOrderByCreatedAtDesc(customerId, st, pageable);
        }

        return orders.map(this::toSummary);
    }

    // NEW: update order status (pharmacy owns the order OR admin)
    public OrderSummaryDTO updateStatus(Long orderId, UpdateOrderStatusRequest req) {

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        OrderStatus newStatus = req.getStatus();
        if (newStatus != OrderStatus.READY && newStatus != OrderStatus.REJECTED && newStatus != OrderStatus.COMPLETED) {
            throw new BadRequestException("Status must be READY, REJECTED, or COMPLETED");
        }

        // Rule: REJECTED requires message
        if (newStatus == OrderStatus.REJECTED) {
            if (req.getMessage() == null || req.getMessage().trim().isEmpty()) {
                throw new BadRequestException("REJECTED requires a non-empty message");
            }
        }

        // Ownership check for PHARMACY is done in controller using current user + pharmacyId,
        // but service also enforces transitions.
        enforceTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);

        // set/update status message (optional for READY/COMPLETED, required for REJECTED)
        if (req.getMessage() != null && !req.getMessage().trim().isEmpty()) {
            order.setStatusMessage(req.getMessage().trim());
        } else if (newStatus == OrderStatus.READY) {
            // allow READY with empty message - keep previous message or null
        }

        order.setUpdatedAt(LocalDateTime.now());

        Order saved = orderRepo.save(order);
        return toSummary(saved);
    }

    private void enforceTransition(OrderStatus oldStatus, OrderStatus newStatus) {
        // Allowed transitions:
        // PENDING → READY | REJECTED
        // READY → COMPLETED
        if (oldStatus == OrderStatus.PENDING) {
            if (newStatus == OrderStatus.READY || newStatus == OrderStatus.REJECTED) return;
        } else if (oldStatus == OrderStatus.READY) {
            if (newStatus == OrderStatus.COMPLETED) return;
        }
        // everything else is conflict
        throw new BadRequestException("Invalid status transition");
    }

    private OrderSummaryDTO toSummary(Order o) {
        OrderSummaryDTO dto = new OrderSummaryDTO();
        dto.setId(o.getId());

        dto.setCustomerId(o.getCustomer().getId());
        dto.setCustomerName(o.getCustomer().getName());

        dto.setPharmacyId(o.getPharmacy().getId());
        dto.setPharmacyName(o.getPharmacy().getName());
        dto.setPharmacyPhone(o.getPharmacy().getPhone());


        dto.setStatus(o.getStatus());
        dto.setStatusMessage(o.getStatusMessage());

        dto.setTotalAmount(o.getTotalAmount());
        dto.setCreatedAt(o.getCreatedAt() == null ? null : o.getCreatedAt().toString());
        dto.setUpdatedAt(o.getUpdatedAt() == null ? null : o.getUpdatedAt().toString());

        List<OrderItemDTO> items = new ArrayList<>();
        for (OrderItem oi : o.getItems()) {
            items.add(new OrderItemDTO(
                    oi.getMedicine().getId(),
                    oi.getMedicine().getName(),
                    oi.getQuantity(),
                    oi.getPrice()
            ));
        }
        dto.setItems(items);
        return dto;
    }
}
