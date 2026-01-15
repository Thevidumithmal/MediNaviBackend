package com.example.pharmacybackend.service;

import com.example.pharmacybackend.dto.stock.*;
import com.example.pharmacybackend.entity.*;
import com.example.pharmacybackend.exception.NotFoundException;
import com.example.pharmacybackend.repository.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PharmacyStockService {

    private final PharmacyRepository pharmacyRepo;
    private final MedicineRepository medicineRepo;
    private final PharmacyStockRepository stockRepo;

    public PharmacyStockService(PharmacyRepository pharmacyRepo, MedicineRepository medicineRepo, PharmacyStockRepository stockRepo) {
        this.pharmacyRepo = pharmacyRepo;
        this.medicineRepo = medicineRepo;
        this.stockRepo = stockRepo;
    }

    public List<StockItemResponse> listStock(Long pharmacyId) {
        List<PharmacyStock> list = stockRepo.findByPharmacyId(pharmacyId);
        List<StockItemResponse> out = new ArrayList<>();
        for (PharmacyStock ps : list) {
            out.add(new StockItemResponse(
                    ps.getId(),
                    ps.getMedicine().getId(),
                    ps.getMedicine().getName(),
                    ps.getQuantity(),
                    ps.getPrice()
            ));
        }
        return out;
    }

    public StockItemResponse add(AddStockRequest req) {
        Pharmacy pharmacy = pharmacyRepo.findById(req.getPharmacyId())
                .orElseThrow(() -> new NotFoundException("Pharmacy not found"));

        Medicine medicine;
        if (req.getMedicineId() != null) {
            medicine = medicineRepo.findById(req.getMedicineId())
                    .orElseThrow(() -> new NotFoundException("Medicine not found"));
        } else {
            medicine = medicineRepo.findByNameIgnoreCase(req.getMedicineName())
                    .orElse(null);
            if (medicine == null) {
                medicine = new Medicine();
                medicine.setName(req.getMedicineName());
                medicine = medicineRepo.save(medicine);
            }
        }

        PharmacyStock stock = new PharmacyStock();
        stock.setPharmacy(pharmacy);
        stock.setMedicine(medicine);
        stock.setQuantity(req.getQuantity());
        stock.setPrice(req.getPrice());
        stock = stockRepo.save(stock);

        return new StockItemResponse(stock.getId(), medicine.getId(), medicine.getName(), stock.getQuantity(), stock.getPrice());
    }

    public StockItemResponse update(Long stockId, UpdateStockRequest req) {
        PharmacyStock stock = stockRepo.findById(stockId)
                .orElseThrow(() -> new NotFoundException("Stock item not found"));

        stock.setQuantity(req.getQuantity());
        stock.setPrice(req.getPrice());
        stock = stockRepo.save(stock);

        return new StockItemResponse(stock.getId(), stock.getMedicine().getId(), stock.getMedicine().getName(), stock.getQuantity(), stock.getPrice());
    }
}
