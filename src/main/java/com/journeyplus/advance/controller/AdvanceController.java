package com.journeyplus.advance.controller;

import com.journeyplus.advance.entity.AdvanceRequest;
import com.journeyplus.advance.entity.AdvanceSettlement;
import com.journeyplus.advance.service.AdvanceService;
import com.journeyplus.iam.entity.User;
import com.journeyplus.trip.entity.TripRequest;
import com.journeyplus.trip.service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/advances")
public class AdvanceController {

    @Autowired
    private AdvanceService advanceService;

    @Autowired
    private TripService tripService;

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<AdvanceRequest> createAdvanceRequest(
            @RequestParam Long tripRequestId,
            @RequestBody AdvanceRequest advanceRequest,
            @AuthenticationPrincipal User employee) {
        
        TripRequest trip = tripService.getTripRequest(tripRequestId);
        if (!trip.getEmployee().getId().equals(employee.getId())) {
            throw new IllegalArgumentException("Trip request does not belong to the authenticated employee");
        }

        advanceRequest.setTripRequest(trip);
        advanceRequest.setEmployee(employee);
        return ResponseEntity.ok(advanceService.createAdvanceRequest(advanceRequest));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('APPROVING_MANAGER')")
    public ResponseEntity<AdvanceRequest> approveAdvanceRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal User manager) {
        return ResponseEntity.ok(advanceService.approveAdvanceRequest(id, manager));
    }

    @PostMapping("/{id}/disburse")
    @PreAuthorize("hasRole('FINANCE_EXECUTIVE')")
    public ResponseEntity<AdvanceRequest> disburseAdvanceRequest(@PathVariable Long id) {
        return ResponseEntity.ok(advanceService.disburseAdvanceRequest(id));
    }

    @PostMapping("/{id}/settle")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'FINANCE_EXECUTIVE')")
    public ResponseEntity<AdvanceRequest> settleAdvanceRequest(
            @PathVariable Long id,
            @RequestBody AdvanceSettlement settlement) {
        return ResponseEntity.ok(advanceService.settleAdvanceRequest(id, settlement));
    }

    @PostMapping("/{id}/forfeit")
    @PreAuthorize("hasRole('FINANCE_EXECUTIVE')")
    public ResponseEntity<AdvanceRequest> forfeitAdvanceRequest(@PathVariable Long id) {
        return ResponseEntity.ok(advanceService.forfeitAdvanceRequest(id));
    }

    @GetMapping("/my-advances")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<AdvanceRequest>> getMyAdvances(@AuthenticationPrincipal User employee) {
        return ResponseEntity.ok(advanceService.getAdvancesByEmployee(employee.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdvanceRequest> getAdvanceRequest(@PathVariable Long id, @AuthenticationPrincipal User user) {
        AdvanceRequest ar = advanceService.getAdvanceRequest(id);
        if (ar == null) throw new IllegalArgumentException("Advance request not found");
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            for (org.springframework.security.core.GrantedAuthority a : auth.getAuthorities()) {
                String r = a.getAuthority();
                if (r != null && (r.endsWith("TRAVEL_ADMIN") || r.endsWith("COMPLIANCE_OFFICER") || r.endsWith("FINANCE_EXECUTIVE"))) {
                    return ResponseEntity.ok(ar);
                }
            }
        }
        if (ar.getEmployee() != null && user != null && ar.getEmployee().getId().equals(user.getId())) {
            return ResponseEntity.ok(ar);
        }
        // allow approving manager if trip's approving manager matches
        if (ar.getTripRequest() != null && ar.getTripRequest().getApprovingManager() != null && user != null && ar.getTripRequest().getApprovingManager().getId().equals(user.getId())) {
            return ResponseEntity.ok(ar);
        }
        throw new org.springframework.security.access.AccessDeniedException("You are not authorized to view this advance");
    }
}
