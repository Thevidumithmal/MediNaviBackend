package com.example.pharmacybackend.controller;

import com.example.pharmacybackend.dto.invoice.*;
import com.example.pharmacybackend.service.InvoiceService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pharmacy/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    // --------------------------------------------------
    // CREATE INVOICE (POS)
    // --------------------------------------------------
    @PostMapping("/create")
    @PreAuthorize("hasRole('PHARMACY') or hasRole('ADMIN')")
    public InvoiceDTO create(@RequestBody CreateInvoiceRequest req) {
        return invoiceService.createInvoice(req);
    }

    // --------------------------------------------------
    // LIST INVOICES (Invoice History Page)
    // --------------------------------------------------
    @GetMapping("/{pharmacyId}")
    @PreAuthorize("hasRole('PHARMACY') or hasRole('ADMIN')")
    public Page<InvoiceSummaryDTO> list(
            @PathVariable Long pharmacyId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String dateFrom, // YYYY-MM-DD
            @RequestParam(required = false) String dateTo,   // YYYY-MM-DD
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort      // createdAt,desc
    ) {
        return invoiceService.listInvoices(
                pharmacyId, q, dateFrom, dateTo, page, size, sort
        );
    }

    // --------------------------------------------------
    // INVOICE DETAIL (Modal)
    // --------------------------------------------------
    @GetMapping("/{pharmacyId}/{invoiceId}")
    @PreAuthorize("hasRole('PHARMACY') or hasRole('ADMIN')")
    public InvoiceDTO detail(
            @PathVariable Long pharmacyId,
            @PathVariable Long invoiceId
    ) {
        return invoiceService.getInvoice(pharmacyId, invoiceId);
    }
}
