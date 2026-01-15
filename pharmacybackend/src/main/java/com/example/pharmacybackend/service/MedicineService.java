package com.example.pharmacybackend.service;

import com.example.pharmacybackend.dto.medicine.PharmacyMedicineResult;
import com.example.pharmacybackend.entity.Pharmacy;
import com.example.pharmacybackend.entity.PharmacyStock;
import com.example.pharmacybackend.repository.PharmacyStockRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MedicineService {

    private final PharmacyStockRepository stockRepo;
    private final GeoService geoService;

    private static final double NEARBY_RADIUS_KM = 10.0;

    public MedicineService(PharmacyStockRepository stockRepo, GeoService geoService) {
        this.stockRepo = stockRepo;
        this.geoService = geoService;
    }

    public List<PharmacyMedicineResult> search(String name, Double lat, Double lon) {

        // ✅ FIX 1: FETCH ONLY STOCK > 0
        List<PharmacyStock> matches =
                stockRepo.searchByMedicineNameWithStock(name);

        if (matches.isEmpty()) {
            return new ArrayList<>();
        }

        // If no location → return all (no nearby logic)
        if (lat == null || lon == null) {
            List<PharmacyMedicineResult> out = new ArrayList<>();
            for (PharmacyStock ps : matches) {
                out.add(map(ps, null));
            }
            return out;
        }

        List<PharmacyMedicineResult> nearby = new ArrayList<>();
        List<PharmacyMedicineResult> all = new ArrayList<>();

        for (PharmacyStock ps : matches) {
            PharmacyMedicineResult r = map(ps, new double[]{lat, lon});
            all.add(r);

            if (r.getDistanceKm() != null && r.getDistanceKm() <= NEARBY_RADIUS_KM) {
                nearby.add(r);
            }
        }

        Comparator<PharmacyMedicineResult> byDistance =
                Comparator.comparing(
                        r -> r.getDistanceKm() == null ? Double.MAX_VALUE : r.getDistanceKm()
                );

        nearby.sort(byDistance);
        all.sort(byDistance);

        // ✅ FIX 2: nearby-first ONLY AFTER stock filtering
        if (!nearby.isEmpty()) {
            return nearby;
        }

        return all;
    }

    private PharmacyMedicineResult map(PharmacyStock ps, double[] userLatLon) {

        Pharmacy p = ps.getPharmacy();
        Double distance = null;

        if (userLatLon != null
                && p.getLatitude() != null
                && p.getLongitude() != null) {

            distance = geoService.haversineKm(
                    userLatLon[0],
                    userLatLon[1],
                    p.getLatitude(),
                    p.getLongitude()
            );

            // ✅ TEMP DEBUG LOG (ADD HERE)
            System.out.println(
                    "[DIST-DEBUG] USER(" + userLatLon[0] + "," + userLatLon[1] + ") " +
                            "PHARMACY(" + p.getLatitude() + "," + p.getLongitude() + ") " +
                            "=> distanceKm=" + distance + " | pharmacyId=" + p.getId() +
                            " | pharmacyName=" + p.getName()
            );
        }

        PharmacyMedicineResult r = new PharmacyMedicineResult();
        r.setPharmacyId(p.getId());
        r.setPharmacyName(p.getName());
        r.setAddress(p.getAddress());
        r.setDistanceKm(distance);

        r.setStockId(ps.getId());
        r.setMedicineId(ps.getMedicine().getId());
        r.setMedicineName(ps.getMedicine().getName());
        r.setQuantity(ps.getQuantity());
        r.setPrice(ps.getPrice());

        return r;
    }

}
