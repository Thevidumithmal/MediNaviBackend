package com.example.pharmacybackend.service;

import com.example.pharmacybackend.dto.invoice.*;
import com.example.pharmacybackend.entity.*;
import com.example.pharmacybackend.exception.BadRequestException;
import com.example.pharmacybackend.exception.ConflictException;
import com.example.pharmacybackend.exception.ForbiddenException;
import com.example.pharmacybackend.exception.NotFoundException;
import com.example.pharmacybackend.repository.*;
import com.example.pharmacybackend.security.CurrentUserService;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.time.Year;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepo;
    private final PharmacyRepository pharmacyRepo;
    private final MedicineRepository medicineRepo;
    private final PharmacyStockRepository stockRepo;
    private final CurrentUserService currentUserService;

    public InvoiceService(
            InvoiceRepository invoiceRepo,
            PharmacyRepository pharmacyRepo,
            MedicineRepository medicineRepo,
            PharmacyStockRepository stockRepo,
            CurrentUserService currentUserService) {

        this.invoiceRepo = invoiceRepo;
        this.pharmacyRepo = pharmacyRepo;
        this.medicineRepo = medicineRepo;
        this.stockRepo = stockRepo;
        this.currentUserService = currentUserService;
    }

    // ------------------------------------------------------------------
    // CREATE INVOICE (UNCHANGED – POS)
    // ------------------------------------------------------------------
    @Transactional
    public InvoiceDTO createInvoice(CreateInvoiceRequest req) {

        if (req.getPharmacyId() == null) throw new BadRequestException("pharmacyId required");
        if (req.getCashierName() == null || req.getCashierName().trim().isEmpty())
            throw new BadRequestException("cashierName required");
        if (req.getItems() == null || req.getItems().isEmpty())
            throw new BadRequestException("items required");

        User current = currentUserService.getCurrentUser();
        if (current.getRole() == Role.PHARMACY) {
            Pharmacy my = pharmacyRepo.findByOwnerId(current.getId())
                    .orElseThrow(() -> new NotFoundException("Pharmacy not found for current user"));
            if (!my.getId().equals(req.getPharmacyId())) throw new ForbiddenException("Forbidden");
        }

        Pharmacy pharmacy = pharmacyRepo.findById(req.getPharmacyId())
                .orElseThrow(() -> new NotFoundException("Pharmacy not found"));

        Invoice invoice = new Invoice();
        invoice.setPharmacy(pharmacy);
        invoice.setCashierName(req.getCashierName().trim());
        invoice.setCustomerName(req.getCustomerName());
        invoice.setCustomerPhone(req.getCustomerPhone());
        invoice.setCreatedAt(LocalDateTime.now());

        double subtotal = 0.0;
        List<InvoiceItem> invoiceItems = new ArrayList<>();

        for (InvoiceItemReq itemReq : req.getItems()) {

            if (itemReq.getMedicineId() == null) throw new BadRequestException("medicineId required");
            if (itemReq.getQuantity() == null || itemReq.getQuantity() <= 0)
                throw new BadRequestException("quantity must be > 0");
            if (itemReq.getUnitPrice() == null || itemReq.getUnitPrice() < 0)
                throw new BadRequestException("unitPrice must be >= 0");

            Medicine medicine = medicineRepo.findById(itemReq.getMedicineId())
                    .orElseThrow(() -> new NotFoundException("Medicine not found"));

            PharmacyStock stock = stockRepo.findForUpdate(pharmacy.getId(), medicine.getId())
                    .orElseThrow(() -> new NotFoundException("Medicine not in pharmacy stock"));

            if (stock.getQuantity() < itemReq.getQuantity())
                throw new ConflictException("Insufficient stock for: " + medicine.getName());

            stock.setQuantity(stock.getQuantity() - itemReq.getQuantity());
            stockRepo.save(stock);

            InvoiceItem ii = new InvoiceItem();
            ii.setInvoice(invoice);
            ii.setMedicine(medicine);
            ii.setQuantity(itemReq.getQuantity());
            ii.setUnitPrice(itemReq.getUnitPrice());

            double lineTotal = itemReq.getQuantity() * itemReq.getUnitPrice();
            ii.setLineTotal(lineTotal);

            invoiceItems.add(ii);
            subtotal += lineTotal;
        }

        double taxAmount = req.getTaxAmount() == null ? 0.0 : req.getTaxAmount();
        double discountAmount = req.getDiscountAmount() == null ? 0.0 : req.getDiscountAmount();

        double totalAmount = subtotal + taxAmount - discountAmount;
        if (totalAmount < 0) totalAmount = 0;

        invoice.setSubtotal(round2(subtotal));
        invoice.setTaxAmount(round2(taxAmount));
        invoice.setDiscountAmount(round2(discountAmount));
        invoice.setTotalAmount(round2(totalAmount));
        invoice.getItems().addAll(invoiceItems);

        Invoice saved = invoiceRepo.save(invoice);

        String year = String.valueOf(Year.now().getValue());
        String seq = String.format("%06d", saved.getId());
        saved.setInvoiceNumber("INV-" + year + "-" + seq);

        saved = invoiceRepo.save(saved);
        return toInvoiceDTO(saved);
    }

    // ------------------------------------------------------------------
    // LIST INVOICES (Invoice History Page)
    // ------------------------------------------------------------------
    public Page<InvoiceSummaryDTO> listInvoices(
            Long pharmacyId,
            String q,
            String dateFrom,
            String dateTo,
            int page,
            int size,
            String sort) {

        enforcePharmacyAccess(pharmacyId);

        Pageable pageable = buildPageable(page, size, sort);

        LocalDateTime fromDT = parseDateStart(dateFrom);
        LocalDateTime toDT = parseDateEnd(dateTo);

        Specification<Invoice> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("pharmacy").get("id"), pharmacyId));

            if (q != null && !q.trim().isEmpty()) {
                String like = "%" + q.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("customerName")), like),
                        cb.like(cb.lower(root.get("customerPhone")), like)
                ));
            }

            if (fromDT != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromDT));
            if (toDT != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), toDT));

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Invoice> pageResult = invoiceRepo.findAll(spec, pageable);

        return pageResult.map(inv -> {
            InvoiceSummaryDTO dto = new InvoiceSummaryDTO();
            dto.setId(inv.getId());
            dto.setInvoiceNumber(inv.getInvoiceNumber());
            dto.setPharmacyId(inv.getPharmacy().getId());
            dto.setCustomerName(inv.getCustomerName());
            dto.setCustomerPhone(inv.getCustomerPhone());
            dto.setItemCount(inv.getItems() == null ? 0 : inv.getItems().size());
            dto.setTotalAmount(inv.getTotalAmount());
            dto.setCreatedAt(inv.getCreatedAt().toString());
            return dto;
        });
    }

    // ------------------------------------------------------------------
    // INVOICE DETAIL (Modal)
    // ------------------------------------------------------------------
    public InvoiceDTO getInvoice(Long pharmacyId, Long invoiceId) {

        enforcePharmacyAccess(pharmacyId);

        Invoice inv = invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException("Invoice not found"));

        if (!inv.getPharmacy().getId().equals(pharmacyId))
            throw new ForbiddenException("Forbidden");

        return toInvoiceDTO(inv);
    }

    // ------------------------------------------------------------------
    // HELPERS
    // ------------------------------------------------------------------
    private void enforcePharmacyAccess(Long pharmacyId) {
        User current = currentUserService.getCurrentUser();

        if (current.getRole() == Role.ADMIN) return;

        if (current.getRole() == Role.PHARMACY) {
            Pharmacy my = pharmacyRepo.findByOwnerId(current.getId())
                    .orElseThrow(() -> new NotFoundException("Pharmacy not found"));
            if (!my.getId().equals(pharmacyId)) throw new ForbiddenException("Forbidden");
            return;
        }

        throw new ForbiddenException("Forbidden");
    }

    private Pageable buildPageable(int page, int size, String sort) {
        if (sort == null || sort.isBlank()) {
            return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        String[] p = sort.split(",");
        Sort.Direction dir = (p.length > 1 && "asc".equalsIgnoreCase(p[1]))
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(dir, p[0]));
    }

    private LocalDateTime parseDateStart(String iso) {
        if (iso == null || iso.isBlank()) return null;
        try {
            return LocalDate.parse(iso).atStartOfDay();
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Invalid dateFrom (YYYY-MM-DD)");
        }
    }

    private LocalDateTime parseDateEnd(String iso) {
        if (iso == null || iso.isBlank()) return null;
        try {
            return LocalDate.parse(iso).atTime(LocalTime.MAX);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Invalid dateTo (YYYY-MM-DD)");
        }
    }

    private InvoiceDTO toInvoiceDTO(Invoice inv) {
        InvoiceDTO dto = new InvoiceDTO();
        dto.setId(inv.getId());
        dto.setInvoiceNumber(inv.getInvoiceNumber());
        dto.setPharmacyId(inv.getPharmacy().getId());
        dto.setCashierName(inv.getCashierName());
        dto.setCustomerName(inv.getCustomerName());
        dto.setCustomerPhone(inv.getCustomerPhone());
        dto.setSubtotal(inv.getSubtotal());
        dto.setTaxAmount(inv.getTaxAmount());
        dto.setDiscountAmount(inv.getDiscountAmount());
        dto.setTotalAmount(inv.getTotalAmount());
        dto.setCreatedAt(inv.getCreatedAt().toString());

        List<InvoiceItemDTO> items = new ArrayList<>();
        for (InvoiceItem ii : inv.getItems()) {
            InvoiceItemDTO i = new InvoiceItemDTO();
            i.setMedicineId(ii.getMedicine().getId());
            i.setMedicineName(ii.getMedicine().getName());
            i.setQuantity(ii.getQuantity());
            i.setUnitPrice(ii.getUnitPrice());
            i.setLineTotal(ii.getLineTotal());
            items.add(i);
        }
        dto.setItems(items);
        return dto;
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
