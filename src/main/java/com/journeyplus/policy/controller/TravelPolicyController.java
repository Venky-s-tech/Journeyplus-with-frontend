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
@RequestMapping("/api/travel-policies")
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
        // Grade is derived from the authenticated employee's profile, not supplied by the client.
        String gradeId = (user != null && user.getGrade() != null) ? user.getGrade().getId() : null;
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
        // Grade is derived from the authenticated employee's profile, not supplied by the client.
        String gradeId = (user != null && user.getGrade() != null) ? user.getGrade().getId() : null;
        TravelAllowanceCalculationResponse response = policyService.calculateAllowance(gradeId, travelType, cityName, country);
        return ResponseEntity.ok(response);
    }
}
