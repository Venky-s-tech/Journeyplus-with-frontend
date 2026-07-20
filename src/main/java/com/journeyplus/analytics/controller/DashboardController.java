package com.journeyplus.analytics.controller;

import com.journeyplus.advance.entity.AdvanceRequest;
import com.journeyplus.advance.repository.AdvanceRequestRepository;
import com.journeyplus.compliance.repository.PolicyExceptionRepository;
import com.journeyplus.expense.entity.ExpenseClaim;
import com.journeyplus.expense.repository.ExpenseClaimRepository;
import com.journeyplus.iam.entity.Role;
import com.journeyplus.iam.entity.User;
import com.journeyplus.trip.entity.TripRequest;
import com.journeyplus.trip.repository.TripRequestRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("isAuthenticated()")
public class DashboardController {

    private final TripRequestRepository tripRequestRepository;
    private final ExpenseClaimRepository expenseClaimRepository;
    private final AdvanceRequestRepository advanceRequestRepository;
    private final PolicyExceptionRepository policyExceptionRepository;

    public DashboardController(
            TripRequestRepository tripRequestRepository,
            ExpenseClaimRepository expenseClaimRepository,
            AdvanceRequestRepository advanceRequestRepository,
            PolicyExceptionRepository policyExceptionRepository) {
        this.tripRequestRepository = tripRequestRepository;
        this.expenseClaimRepository = expenseClaimRepository;
        this.advanceRequestRepository = advanceRequestRepository;
        this.policyExceptionRepository = policyExceptionRepository;
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getDashboardSummary(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Long userId,
            @AuthenticationPrincipal User user) {

        Role effectiveRole = role != null ? role : user.getRole();
        Long effectiveUserId = userId != null ? userId : user.getId();

        Map<String, Object> summary = new HashMap<>();
        summary.put("role", effectiveRole.name());
        summary.put("userId", effectiveUserId);

        switch (effectiveRole) {
            case EMPLOYEE:
                List<TripRequest> myTrips = tripRequestRepository.findByEmployee_Id(effectiveUserId);
                List<ExpenseClaim> myClaims = expenseClaimRepository.findByEmployee_Id(effectiveUserId);
                List<AdvanceRequest> myAdvances = advanceRequestRepository.findByEmployee_Id(effectiveUserId);
                summary.put("myTripsCount", myTrips.size());
                summary.put("myActiveTripsCount", myTrips.stream().filter(t -> t.getStatus() == com.journeyplus.trip.entity.TripStatus.APPROVED || t.getStatus() == com.journeyplus.trip.entity.TripStatus.SUBMITTED).count());
                summary.put("myClaimsCount", myClaims.size());
                summary.put("myAdvancesCount", myAdvances.size());
                break;

            case APPROVING_MANAGER:
                List<TripRequest> pendingTrips = tripRequestRepository.findByApprover_Id(effectiveUserId).stream()
                        .filter(t -> t.getStatus() == com.journeyplus.trip.entity.TripStatus.SUBMITTED)
                        .collect(Collectors.toList());
                List<ExpenseClaim> pendingClaims = expenseClaimRepository.findAll().stream()
                        .filter(c -> c.getStatus() == com.journeyplus.expense.entity.ExpenseStatus.SUBMITTED)
                        .collect(Collectors.toList());
                summary.put("pendingTripApprovalsCount", pendingTrips.size());
                summary.put("pendingExpenseApprovalsCount", pendingClaims.size());
                break;

            case FINANCE:
                List<AdvanceRequest> pendingDisbursements = advanceRequestRepository.findAll().stream()
                        .filter(a -> a.getStatus() == com.journeyplus.advance.entity.AdvanceStatus.APPROVED)
                        .collect(Collectors.toList());
                List<ExpenseClaim> pendingReimbursements = expenseClaimRepository.findAll().stream()
                        .filter(c -> c.getStatus() == com.journeyplus.expense.entity.ExpenseStatus.APPROVED)
                        .collect(Collectors.toList());
                summary.put("pendingDisbursementsCount", pendingDisbursements.size());
                summary.put("pendingReimbursementsCount", pendingReimbursements.size());
                break;

            case COMPLIANCE:
                long openExceptions = policyExceptionRepository.findByApprovalStatus("PENDING").size();
                summary.put("openExceptionsCount", openExceptions);
                summary.put("totalExceptionsCount", policyExceptionRepository.findAll().size());
                break;

            case TRAVEL_DESK:
                long approvedTripsNeedingLegs = tripRequestRepository.findAll().stream()
                        .filter(t -> t.getStatus() == com.journeyplus.trip.entity.TripStatus.APPROVED).count();
                summary.put("approvedTripsCount", approvedTripsNeedingLegs);
                break;

            case ADMIN:
            default:
                summary.put("totalUsersCount", 100);
                summary.put("totalTripsCount", tripRequestRepository.count());
                summary.put("totalClaimsCount", expenseClaimRepository.count());
                summary.put("openExceptionsCount", policyExceptionRepository.findByApprovalStatus("PENDING").size());
                break;
        }

        return ResponseEntity.ok(summary);
    }
}
