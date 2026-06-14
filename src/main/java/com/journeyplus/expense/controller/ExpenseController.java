package com.journeyplus.expense.controller;

import com.journeyplus.expense.entity.ExpenseClaim;
import com.journeyplus.expense.entity.ExpenseLine;
import com.journeyplus.expense.entity.ExpenseStatus;
import com.journeyplus.expense.entity.Reimbursement;
import com.journeyplus.expense.service.ExpenseService;
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
@RequestMapping("/api/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private TripService tripService;

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ExpenseClaim> createExpenseClaim(
            @RequestParam Long tripRequestId,
            @RequestBody ExpenseClaim claim,
            @AuthenticationPrincipal User employee) {
        
        TripRequest trip = tripService.getTripRequest(tripRequestId);
        if (!trip.getEmployee().getId().equals(employee.getId())) {
            throw new IllegalArgumentException("Trip request does not belong to the authenticated employee");
        }

        claim.setTripRequest(trip);
        claim.setEmployee(employee);
        return ResponseEntity.ok(expenseService.createExpenseClaim(claim));
    }

    @PostMapping("/{claimId}/lines")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ExpenseLine> addExpenseLine(
            @PathVariable Long claimId,
            @RequestBody ExpenseLine line) {
        return ResponseEntity.ok(expenseService.addExpenseLine(claimId, line));
    }

    @PostMapping("/{claimId}/submit")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ExpenseClaim> submitExpenseClaim(@PathVariable Long claimId) {
        return ResponseEntity.ok(expenseService.submitExpenseClaim(claimId));
    }

    @PostMapping("/{claimId}/approve")
    @PreAuthorize("hasRole('APPROVING_MANAGER')")
    public ResponseEntity<ExpenseClaim> approveExpenseClaim(
            @PathVariable Long claimId,
            @RequestParam(required = false) String comments,
            @AuthenticationPrincipal User manager) {
        return ResponseEntity.ok(expenseService.approveOrRejectExpenseClaim(claimId, ExpenseStatus.APPROVED, comments, manager));
    }

    @PostMapping("/{claimId}/reject")
    @PreAuthorize("hasRole('APPROVING_MANAGER')")
    public ResponseEntity<ExpenseClaim> rejectExpenseClaim(
            @PathVariable Long claimId,
            @RequestParam(required = false) String comments,
            @AuthenticationPrincipal User manager) {
        return ResponseEntity.ok(expenseService.approveOrRejectExpenseClaim(claimId, ExpenseStatus.REJECTED, comments, manager));
    }

    @PostMapping("/{claimId}/reimburse")
    @PreAuthorize("hasRole('FINANCE_EXECUTIVE')")
    public ResponseEntity<ExpenseClaim> disburseReimbursement(
            @PathVariable Long claimId,
            @RequestBody Reimbursement reimbursement) {
        return ResponseEntity.ok(expenseService.payReimbursement(claimId, reimbursement));
    }

    @GetMapping("/my-claims")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<ExpenseClaim>> getMyClaims(@AuthenticationPrincipal User employee) {
        return ResponseEntity.ok(expenseService.getClaimsByEmployee(employee.getId()));
    }

    @GetMapping("/{claimId}")
    public ResponseEntity<ExpenseClaim> getExpenseClaim(@PathVariable Long claimId, @AuthenticationPrincipal User user) {
        ExpenseClaim claim = expenseService.getExpenseClaim(claimId);
        if (claim == null) throw new IllegalArgumentException("Expense claim not found");
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            for (org.springframework.security.core.GrantedAuthority a : auth.getAuthorities()) {
                String r = a.getAuthority();
                if (r != null && (r.endsWith("TRAVEL_ADMIN") || r.endsWith("COMPLIANCE_OFFICER") || r.endsWith("FINANCE_EXECUTIVE"))) {
                    return ResponseEntity.ok(claim);
                }
            }
        }
        if (claim.getEmployee() != null && user != null && claim.getEmployee().getId().equals(user.getId())) {
            return ResponseEntity.ok(claim);
        }
        if (claim.getTripRequest() != null && claim.getTripRequest().getApprovingManager() != null && user != null && claim.getTripRequest().getApprovingManager().getId().equals(user.getId())) {
            return ResponseEntity.ok(claim);
        }
        throw new org.springframework.security.access.AccessDeniedException("You are not authorized to view this expense claim");
    }

    @GetMapping("/{claimId}/lines")
    public ResponseEntity<List<ExpenseLine>> getExpenseLines(@PathVariable Long claimId, @AuthenticationPrincipal User user) {
        ExpenseClaim claim = expenseService.getExpenseClaim(claimId);
        if (claim == null) throw new IllegalArgumentException("Expense claim not found");
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            for (org.springframework.security.core.GrantedAuthority a : auth.getAuthorities()) {
                String r = a.getAuthority();
                if (r != null && (r.endsWith("TRAVEL_ADMIN") || r.endsWith("COMPLIANCE_OFFICER") || r.endsWith("FINANCE_EXECUTIVE"))) {
                    return ResponseEntity.ok(expenseService.getLinesByClaim(claimId));
                }
            }
        }
        if (claim.getEmployee() != null && user != null && claim.getEmployee().getId().equals(user.getId())) {
            return ResponseEntity.ok(expenseService.getLinesByClaim(claimId));
        }
        if (claim.getTripRequest() != null && claim.getTripRequest().getApprovingManager() != null && user != null && claim.getTripRequest().getApprovingManager().getId().equals(user.getId())) {
            return ResponseEntity.ok(expenseService.getLinesByClaim(claimId));
        }
        throw new org.springframework.security.access.AccessDeniedException("You are not authorized to view expense lines");
    }
}
