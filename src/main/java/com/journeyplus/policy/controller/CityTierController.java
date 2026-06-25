package com.journeyplus.policy.controller;

import com.journeyplus.policy.dto.CityCostDetailsResponse;
import com.journeyplus.policy.dto.CityTierRequest;
import com.journeyplus.policy.entity.CityTier;
import com.journeyplus.policy.entity.CityTierType;
import com.journeyplus.policy.service.PolicyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/city-tiers")
public class CityTierController {

    @Autowired
    private PolicyService policyService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CityTier> createCityTier(@Valid @RequestBody CityTierRequest request) {
        CityTier created = policyService.createCityTier(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CityTier> updateCityTier(
            @PathVariable Long id,
            @Valid @RequestBody CityTierRequest request
    ) {
        CityTier updated = policyService.updateCityTier(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCityTier(@PathVariable Long id) {
        policyService.deleteCityTier(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CityTier> getCityTierById(@PathVariable Long id) {
        CityTier cityTier = policyService.getCityTierById(id);
        return ResponseEntity.ok(cityTier);
    }

    @GetMapping
    public ResponseEntity<List<CityTier>> getAllCityTiers(
            @RequestParam(required = false) CityTierType tier,
            @RequestParam(required = false) String country
    ) {
        List<CityTier> cityTiers = policyService.searchCityTiers(tier, country);
        return ResponseEntity.ok(cityTiers);
    }

    @GetMapping("/cost-details")
    public ResponseEntity<CityCostDetailsResponse> getCostDetails(
            @RequestParam String cityName,
            @RequestParam String country
    ) {
        CityCostDetailsResponse response = policyService.getCostDetails(cityName, country);
        return ResponseEntity.ok(response);
    }
}
