package com.journeyplus.advance.controller;

import com.journeyplus.advance.dto.*;
import com.journeyplus.advance.entity.AdvanceRequest;
import com.journeyplus.advance.entity.AdvanceSettlement;
import com.journeyplus.advance.entity.AdvanceStatus;
import com.journeyplus.advance.service.AdvanceService;
import com.journeyplus.iam.entity.User;
import com.journeyplus.trip.entity.TripRequest;
import com.journeyplus.trip.service.TripService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class AdvanceController {

    private final AdvanceService advanceService;
    private final TripService tripService;

    public AdvanceController(AdvanceService advanceService, TripService tripService) {
        this.advanceService = advanceService;
        this.tripService = tripService;
    }

    // ==========================================
    // ADVANCE REQUEST ENDPOINTS
    // ==========================================

    @PostMapping("/api/advances")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<AdvanceResponse> createAdvanceRequest(
            @Valid @RequestBody AdvanceRequestInput input,
            @AuthenticationPrincipal User employee) {

        TripRequest trip = tripService.getTripRequest(input.getTripRequestId());
        AdvanceRequest request = new AdvanceRequest();
        request.setTripRequest(trip);
        request.setEmployee(employee);
        request.setRequestedAmount(input.getRequestedAmount());
        request.setCurrency(input.getCurrency());
        request.setPurposeDetails(input.getPurposeDetails());
        if (input.getUsdEquivalent() != null) {
            request.setUsdEquivalent(input.getUsdEquivalent());
        } else {
            request.setUsdEquivalent(input.getRequestedAmount()); // default equivalent
        }

        AdvanceRequest saved = advanceService.createAdvanceRequest(request);
        return ResponseEntity.ok(toAdvanceResponse(saved));
    }

    @PutMapping("/api/advances/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<AdvanceResponse> updateAdvanceRequest(
            @PathVariable Long id,
            @Valid @RequestBody AdvanceRequestInput input,
            @AuthenticationPrincipal User employee) {

        AdvanceRequest updated = new AdvanceRequest();
        updated.setRequestedAmount(input.getRequestedAmount());
        updated.setCurrency(input.getCurrency());
        updated.setPurposeDetails(input.getPurposeDetails());
        if (input.getUsdEquivalent() != null) {
            updated.setUsdEquivalent(input.getUsdEquivalent());
        } else {
            updated.setUsdEquivalent(input.getRequestedAmount());
        }

        AdvanceRequest saved = advanceService.updateAdvanceRequest(id, updated, employee);
        return ResponseEntity.ok(toAdvanceResponse(saved));
    }

    @GetMapping("/api/advances")
    public ResponseEntity<List<AdvanceResponse>> getAllAdvances(
            @RequestParam(required = false) AdvanceStatus status,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long tripId,
            @RequestParam(required = false) String currency,
            @AuthenticationPrincipal User user) {

        // Non-finance/non-admin users can only view their own advances
        boolean isPrivileged = user.getRole() == com.journeyplus.iam.entity.Role.ADMIN ||
                              user.getRole() == com.journeyplus.iam.entity.Role.FINANCE ||
                              user.getRole() == com.journeyplus.iam.entity.Role.COMPLIANCE;

        Long targetEmployeeId = employeeId;
        if (!isPrivileged) {
            targetEmployeeId = user.getId();
        }

        List<AdvanceRequest> advances = advanceService.filterAdvances(status, targetEmployeeId, tripId, currency);
        List<AdvanceResponse> dto = advances.stream().map(this::toAdvanceResponse).collect(Collectors.toList());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/api/advances/{id}")
    public ResponseEntity<AdvanceResponse> getAdvanceRequest(@PathVariable Long id, @AuthenticationPrincipal User user) {
        AdvanceRequest ar = advanceService.getAdvanceRequest(id);
        validateAdvanceViewAuthorization(ar, user);
        return ResponseEntity.ok(toAdvanceResponse(ar));
    }

    @PostMapping("/api/advances/{id}/approve")
    @PreAuthorize("hasRole('APPROVING_MANAGER')")
    public ResponseEntity<AdvanceResponse> approveAdvanceRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal User manager) {
        AdvanceRequest saved = advanceService.approveAdvanceRequest(id, manager);
        return ResponseEntity.ok(toAdvanceResponse(saved));
    }

    @PostMapping("/api/advances/{id}/disburse")
    @PreAuthorize("hasRole('FINANCE')")
    public ResponseEntity<AdvanceResponse> disburseAdvanceRequest(@PathVariable Long id) {
        AdvanceRequest saved = advanceService.disburseAdvanceRequest(id);
        return ResponseEntity.ok(toAdvanceResponse(saved));
    }

    @PostMapping("/api/advances/{id}/forfeit")
    @PreAuthorize("hasRole('FINANCE')")
    public ResponseEntity<AdvanceResponse> forfeitAdvanceRequest(@PathVariable Long id) {
        AdvanceRequest saved = advanceService.forfeitAdvanceRequest(id);
        return ResponseEntity.ok(toAdvanceResponse(saved));
    }

    // ==========================================
    // ADVANCE SETTLEMENT ENDPOINTS
    // ==========================================

    @PostMapping("/api/advances/{advanceId}/settlements")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'FINANCE')")
    public ResponseEntity<AdvanceSettlementResponse> addSettlement(
            @PathVariable Long advanceId,
            @Valid @RequestBody AdvanceSettlementInput input,
            @AuthenticationPrincipal User user) {

        AdvanceRequest request = advanceService.getAdvanceRequest(advanceId);
        // Enforce that employee can only settle their own advance
        if (user.getRole() == com.journeyplus.iam.entity.Role.EMPLOYEE && !request.getEmployee().getId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Only the Advance Owner or Finance can settle this advance");
        }

        AdvanceSettlement settlement = new AdvanceSettlement();
        settlement.setAmountUtilised(input.getAmountUtilised());
        settlement.setAmountReturned(input.getAmountReturned());
        settlement.setRemarks(input.getRemarks());

        AdvanceSettlement saved = advanceService.addSettlement(advanceId, settlement);
        return ResponseEntity.ok(toSettlementResponse(saved));
    }

    // Alias for backward compatibility
    @PostMapping("/api/advances/{id}/settle")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'FINANCE')")
    public ResponseEntity<AdvanceResponse> settleAdvanceRequest(
            @PathVariable Long id,
            @Valid @RequestBody AdvanceSettlementInput input,
            @AuthenticationPrincipal User user) {
        addSettlement(id, input, user);
        return ResponseEntity.ok(toAdvanceResponse(advanceService.getAdvanceRequest(id)));
    }

    @GetMapping("/api/advances/{advanceId}/settlements")
    public ResponseEntity<List<AdvanceSettlementResponse>> getSettlements(
            @PathVariable Long advanceId,
            @AuthenticationPrincipal User user) {
        AdvanceRequest ar = advanceService.getAdvanceRequest(advanceId);
        validateAdvanceViewAuthorization(ar, user);

        List<AdvanceSettlement> settlements = advanceService.getSettlementsForAdvance(advanceId);
        List<AdvanceSettlementResponse> dto = settlements.stream().map(this::toSettlementResponse).collect(Collectors.toList());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/api/settlements/{id}")
    public ResponseEntity<AdvanceSettlementResponse> getSettlement(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        AdvanceSettlement settlement = advanceService.getSettlement(id);
        validateAdvanceViewAuthorization(settlement.getAdvanceRequest(), user);
        return ResponseEntity.ok(toSettlementResponse(settlement));
    }

    @PutMapping("/api/settlements/{id}")
    @PreAuthorize("hasRole('FINANCE')")
    public ResponseEntity<AdvanceSettlementResponse> updateSettlement(
            @PathVariable Long id,
            @Valid @RequestBody AdvanceSettlementInput input) {
        AdvanceSettlement updated = new AdvanceSettlement();
        updated.setAmountUtilised(input.getAmountUtilised());
        updated.setAmountReturned(input.getAmountReturned());
        updated.setRemarks(input.getRemarks());

        AdvanceSettlement saved = advanceService.updateSettlement(id, updated);
        return ResponseEntity.ok(toSettlementResponse(saved));
    }

    // ==========================================
    // ADVANCED QUEUE ENDPOINTS
    // ==========================================

    @GetMapping("/api/advances/my-advances")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<AdvanceResponse>> getMyAdvances(@AuthenticationPrincipal User employee) {
        List<AdvanceRequest> list = advanceService.getAdvancesByEmployee(employee.getId());
        List<AdvanceResponse> dto = list.stream().map(this::toAdvanceResponse).collect(Collectors.toList());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/api/advances/pending-approvals")
    @PreAuthorize("hasRole('APPROVING_MANAGER')")
    public ResponseEntity<List<AdvanceResponse>> getPendingApprovals(@AuthenticationPrincipal User manager) {
        List<AdvanceRequest> list = advanceService.getPendingApprovals(manager.getId());
        List<AdvanceResponse> dto = list.stream().map(this::toAdvanceResponse).collect(Collectors.toList());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/api/advances/pending-disbursements")
    @PreAuthorize("hasRole('FINANCE')")
    public ResponseEntity<List<AdvanceResponse>> getPendingDisbursements() {
        List<AdvanceRequest> list = advanceService.getPendingDisbursements();
        List<AdvanceResponse> dto = list.stream().map(this::toAdvanceResponse).collect(Collectors.toList());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/api/advances/{id}/summary")
    public ResponseEntity<AdvanceSummaryResponse> getAdvanceSummary(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        AdvanceRequest ar = advanceService.getAdvanceRequest(id);
        validateAdvanceViewAuthorization(ar, user);

        List<AdvanceSettlement> settlements = advanceService.getSettlementsForAdvance(id);
        return ResponseEntity.ok(toSummaryResponse(ar, settlements));
    }

    // ==========================================
    // AUTHORIZATION AND MAPPING HELPERS
    // ==========================================

    private void validateAdvanceViewAuthorization(AdvanceRequest ar, User user) {
        if (user == null) {
            throw new org.springframework.security.access.AccessDeniedException("User not authenticated");
        }

        // Admins, Compliance, Finance can view all
        com.journeyplus.iam.entity.Role role = user.getRole();
        if (role == com.journeyplus.iam.entity.Role.ADMIN ||
            role == com.journeyplus.iam.entity.Role.FINANCE ||
            role == com.journeyplus.iam.entity.Role.COMPLIANCE) {
            return;
        }

        // Owner can view
        if (ar.getEmployee() != null && ar.getEmployee().getId().equals(user.getId())) {
            return;
        }

        // Trip assigned manager can view
        if (ar.getTripRequest() != null && ar.getTripRequest().getApprover() != null && ar.getTripRequest().getApprover().getId().equals(user.getId())) {
            return;
        }

        throw new org.springframework.security.access.AccessDeniedException("You are not authorized to view this cash advance");
    }

    private AdvanceResponse toAdvanceResponse(AdvanceRequest a) {
        if (a == null) return null;
        AdvanceResponse r = new AdvanceResponse();
        r.setId(a.getId());
        r.setTripRequestId(a.getTripRequestId());
        r.setEmployeeId(a.getEmployeeId());
        r.setRequestedAmount(a.getRequestedAmount());
        r.setCurrency(a.getCurrency());
        r.setPurposeDetails(a.getPurposeDetails());
        r.setUsdEquivalent(a.getUsdEquivalent());
        r.setStatus(a.getStatus() != null ? a.getStatus().name() : null);
        r.setApprovedById(a.getApprovedBy() != null ? a.getApprovedBy().getId() : null);
        r.setCreatedDate(a.getCreatedDate());
        r.setUpdatedDate(a.getUpdatedDate());
        r.setDisbursementDate(a.getDisbursementDate());
        return r;
    }

    private AdvanceSettlementResponse toSettlementResponse(AdvanceSettlement s) {
        if (s == null) return null;
        AdvanceSettlementResponse r = new AdvanceSettlementResponse();
        r.setId(s.getId());
        r.setAdvanceRequestId(s.getAdvanceRequest() != null ? s.getAdvanceRequest().getId() : null);
        r.setAmountUtilised(s.getAmountUtilised());
        r.setAmountReturned(s.getAmountReturned());
        r.setSettlementDate(s.getSettlementDate());
        r.setStatus(s.getStatus() != null ? s.getStatus().name() : null);
        r.setRemarks(s.getRemarks());
        r.setCreatedDate(s.getCreatedDate());
        r.setUpdatedDate(s.getUpdatedDate());
        return r;
    }

    private AdvanceSummaryResponse toSummaryResponse(AdvanceRequest a, List<AdvanceSettlement> settlements) {
        AdvanceResponse details = toAdvanceResponse(a);
        List<AdvanceSettlementResponse> settlementResponses = settlements.stream()
                .map(this::toSettlementResponse)
                .collect(Collectors.toList());

        BigDecimal totalUtilised = settlements.stream()
                .map(AdvanceSettlement::getAmountUtilised)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalReturned = settlements.stream()
                .map(AdvanceSettlement::getAmountReturned)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Outstanding Amount = AdvanceAmount - (Utilised + Returned)
        BigDecimal outstanding = a.getRequestedAmount().subtract(totalUtilised.add(totalReturned));
        if (totalUtilised.compareTo(a.getRequestedAmount()) >= 0 || outstanding.compareTo(BigDecimal.ZERO) < 0) {
            outstanding = BigDecimal.ZERO;
        }

        return new AdvanceSummaryResponse(
                details,
                settlementResponses,
                totalUtilised,
                totalReturned,
                outstanding,
                a.getStatus() != null ? a.getStatus().name() : null
        );
    }
}
