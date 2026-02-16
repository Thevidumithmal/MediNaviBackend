package com.example.pharmacybackend.controller;

import com.example.pharmacybackend.repository.MedicineRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/medicines")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class MedicineNamesController {

    private final MedicineRepository medicineRepository;

    public MedicineNamesController(MedicineRepository medicineRepository) {
        this.medicineRepository = medicineRepository;
    }

    @GetMapping("/names")
    public List<String> names() {
        return medicineRepository.findAllNames();
    }
}

