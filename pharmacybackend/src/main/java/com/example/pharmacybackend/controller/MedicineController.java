package com.example.pharmacybackend.controller;

import com.example.pharmacybackend.dto.medicine.PharmacyMedicineResult;
import com.example.pharmacybackend.service.MedicineService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/medicines")
public class MedicineController {

    private final MedicineService medicineService;

    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    @GetMapping("/search")
    public List<PharmacyMedicineResult> search(
            @RequestParam String name,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon
    ) {
        return medicineService.search(name, lat, lon);
    }
}

