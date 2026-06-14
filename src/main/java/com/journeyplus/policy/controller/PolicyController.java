package com.journeyplus.policy.controller;

import com.journeyplus.iam.entity.Role;
import com.journeyplus.policy.entity.CityTier;
import com.journeyplus.policy.entity.TravelPolicy;
import com.journeyplus.policy.service.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/policies")
public class PolicyController {

    @Autowired
    private PolicyService policyService;

    @PostMapping
    @PreAuthorize("hasRole('TRAVEL_ADMIN')")
    public ResponseEntity<TravelPolicy> createPolicy(@RequestBody TravelPolicy policy, Principal principal) {
        policy.setCreatedBy(principal.getName());
        return ResponseEntity.ok(policyService.createPolicy(policy));
    }

    @PostMapping("/city-tiers")
    @PreAuthorize("hasRole('TRAVEL_ADMIN')")
    public ResponseEntity<CityTier> createCityTier(@RequestBody CityTier cityTier) {
        return ResponseEntity.ok(policyService.createCityTier(cityTier));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TRAVEL_ADMIN', 'COMPLIANCE_OFFICER', 'FINANCE_EXECUTIVE', 'EMPLOYEE')")
    public ResponseEntity<List<TravelPolicy>> getAllPolicies() {
        return ResponseEntity.ok(policyService.getAllPolicies());
    }

    @GetMapping("/city-tiers")
    @PreAuthorize("hasAnyRole('TRAVEL_ADMIN', 'EMPLOYEE', 'TRAVEL_DESK_COORDINATOR')")
    public ResponseEntity<List<CityTier>> getAllCityTiers() {
        return ResponseEntity.ok(policyService.getAllCityTiers());
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasAnyRole('TRAVEL_ADMIN','COMPLIANCE_OFFICER','APPROVING_MANAGER')")
    public ResponseEntity<TravelPolicy> getPolicyByRole(@PathVariable Role role) {
        return ResponseEntity.ok(policyService.getPolicyByRole(role));
    }
}
