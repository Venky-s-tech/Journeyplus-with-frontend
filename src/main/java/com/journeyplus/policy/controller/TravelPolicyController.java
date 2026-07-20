package com.journeyplus.policy.controller;

import com.journeyplus.policy.dto.TravelAllowanceCalculationResponse;
import com.journeyplus.policy.dto.TravelPolicyRequest;
import com.journeyplus.policy.dto.TravelPolicyResponse;
import com.journeyplus.policy.entity.PolicyStatus;
import com.journeyplus.policy.entity.TravelType;
import com.journeyplus.policy.service.PolicyService;
import com.journeyplus.iam.entity.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping({"/api/travel-policies", "/api/policies"})
public class TravelPolicyController {

    private final PolicyService policyService;

    public TravelPolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TravelPolicyResponse> createPolicy(
            @Valid @RequestBody TravelPolicyRequest request,
            Principal principal
    ) {
        String username = principal != null ? principal.getName() : "SYSTEM";
        TravelPolicyResponse response = policyService.createPolicy(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TravelPolicyResponse> updatePolicy(
            @PathVariable Long id,
            @Valid @RequestBody TravelPolicyRequest request
    ) {
        TravelPolicyResponse response = policyService.updatePolicy(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TravelPolicyResponse> deactivatePolicy(@PathVariable Long id) {
        TravelPolicyResponse response = policyService.deactivatePolicy(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/effective")
    public ResponseEntity<TravelPolicyResponse> getEffectivePolicyEndpoint(
            @RequestParam(required = false) String gradeId,
            @RequestParam(required = false, defaultValue = "DOMESTIC") TravelType travelType,
            @RequestParam(required = false) String date,
            @AuthenticationPrincipal User user
    ) {
        String effectiveGrade = (gradeId != null && !gradeId.isBlank()) 
                ? gradeId 
                : (user != null && user.getGrade() != null ? user.getGrade().getId() : "G1");
        
        java.time.LocalDateTime tripDate = null;
        if (date != null && !date.isBlank()) {
            try {
                tripDate = java.time.LocalDate.parse(date).atStartOfDay();
            } catch (Exception e) {
                try {
                    tripDate = java.time.LocalDateTime.parse(date);
                } catch (Exception ignored) {}
            }
        }
        
        TravelPolicyResponse response = policyService.getEffectivePolicy(effectiveGrade, travelType, tripDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TravelPolicyResponse> getPolicyById(@PathVariable Long id) {
        TravelPolicyResponse response = policyService.getPolicyById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TravelPolicyResponse>> getAllPolicies(
            @RequestParam(required = false) String gradeId,
            @RequestParam(required = false) TravelType travelType,
            @RequestParam(required = false) PolicyStatus status
    ) {
        List<TravelPolicyResponse> policies = policyService.searchPolicies(gradeId, travelType, status);
        return ResponseEntity.ok(policies);
    }

    @GetMapping("/search")
    public ResponseEntity<TravelPolicyResponse> getEffectivePolicy(
            @RequestParam TravelType travelType,
            @AuthenticationPrincipal User user
    ) {
        String gradeId = (user != null && user.getGrade() != null) ? user.getGrade().getId() : "G1";
        TravelPolicyResponse response = policyService.getEffectivePolicy(gradeId, travelType);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/calculate-allowance")
    public ResponseEntity<TravelAllowanceCalculationResponse> calculateAllowance(
            @RequestParam TravelType travelType,
            @RequestParam String cityName,
            @RequestParam String country,
            @AuthenticationPrincipal User user
    ) {
        String gradeId = (user != null && user.getGrade() != null) ? user.getGrade().getId() : "G1";
        TravelAllowanceCalculationResponse response = policyService.calculateAllowance(gradeId, travelType, cityName, country);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/policy-check")
    public ResponseEntity<java.util.Map<String, Object>> checkPolicy(
            @RequestParam(required = false) String gradeId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String cityName,
            @RequestParam(required = false) java.math.BigDecimal amount,
            @AuthenticationPrincipal User user) {

        String effectiveGrade = (gradeId != null && !gradeId.isBlank()) 
                ? gradeId 
                : (user != null && user.getGrade() != null ? user.getGrade().getId() : "G1");
        
        java.math.BigDecimal limit = new java.math.BigDecimal("500.00");
        try {
            TravelPolicyResponse pol = policyService.getEffectivePolicy(effectiveGrade, TravelType.DOMESTIC);
            if ("ACCOMMODATION".equalsIgnoreCase(category)) {
                limit = pol.getPerDiemRate();
            } else if ("MEALS".equalsIgnoreCase(category)) {
                limit = pol.getPerDiemRate().multiply(new java.math.BigDecimal("0.5"));
            } else if ("TRANSPORT".equalsIgnoreCase(category)) {
                limit = pol.getLocalConveyanceLimit();
            }
        } catch (Exception ignored) {}

        boolean compliant = amount == null || amount.compareTo(limit) <= 0;
        return ResponseEntity.ok(java.util.Map.of(
                "policyCompliant", compliant,
                "limit", limit,
                "claimedAmount", amount != null ? amount : java.math.BigDecimal.ZERO,
                "message", compliant ? "Within policy limit" : "Exceeds allowable entitlement limit of " + limit
        ));
    }
}
