package com.example.pharmacybackend.controller;

import com.example.pharmacybackend.dto.stock.*;
import com.example.pharmacybackend.service.PharmacyStockService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pharmacy")
public class PharmacyController {

    private final PharmacyStockService stockService;

    public PharmacyController(PharmacyStockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/stock/{pharmacyId}")
    @PreAuthorize("hasRole('PHARMACY') or hasRole('ADMIN')")
    public List<StockItemResponse> stock(@PathVariable Long pharmacyId) {
        return stockService.listStock(pharmacyId);
    }

    @PostMapping("/stock/add")
    @PreAuthorize("hasRole('PHARMACY') or hasRole('ADMIN')")
    public StockItemResponse add(@Valid @RequestBody AddStockRequest req) {
        return stockService.add(req);
    }

    @PutMapping("/stock/update/{id}")
    @PreAuthorize("hasRole('PHARMACY') or hasRole('ADMIN')")
    public StockItemResponse update(@PathVariable Long id, @Valid @RequestBody UpdateStockRequest req) {
        return stockService.update(id, req);
    }
}

