package com.journeyplus.analytics.controller;

import com.journeyplus.advance.entity.AdvanceRequest;
import com.journeyplus.advance.entity.AdvanceStatus;
import com.journeyplus.advance.repository.AdvanceRequestRepository;
import com.journeyplus.compliance.entity.PolicyException;
import com.journeyplus.compliance.repository.ComplianceAuditRepository;
import com.journeyplus.compliance.repository.PolicyExceptionRepository;
import com.journeyplus.expense.entity.ExpenseClaim;
import com.journeyplus.expense.entity.ExpenseStatus;
import com.journeyplus.expense.repository.ExpenseClaimRepository;
import com.journeyplus.iam.entity.Role;
import com.journeyplus.iam.entity.User;
import com.journeyplus.iam.repository.GradeRepository;
import com.journeyplus.iam.repository.UserRepository;
import com.journeyplus.policy.repository.TravelPolicyRepository;
import com.journeyplus.trip.entity.TripRequest;
import com.journeyplus.trip.entity.TripStatus;
import com.journeyplus.trip.repository.ItineraryLegRepository;
import com.journeyplus.trip.repository.TripRequestRepository;
import com.journeyplus.trip.repository.VisaRequirementRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
    private final UserRepository userRepository;
    private final GradeRepository gradeRepository;
    private final TravelPolicyRepository travelPolicyRepository;
    private final ItineraryLegRepository itineraryLegRepository;
    private final VisaRequirementRepository visaRequirementRepository;
    private final ComplianceAuditRepository complianceAuditRepository;

    public DashboardController(
            TripRequestRepository tripRequestRepository,
            ExpenseClaimRepository expenseClaimRepository,
            AdvanceRequestRepository advanceRequestRepository,
            PolicyExceptionRepository policyExceptionRepository,
            UserRepository userRepository,
            GradeRepository gradeRepository,
            TravelPolicyRepository travelPolicyRepository,
            ItineraryLegRepository itineraryLegRepository,
            VisaRequirementRepository visaRequirementRepository,
            ComplianceAuditRepository complianceAuditRepository) {
        this.tripRequestRepository = tripRequestRepository;
        this.expenseClaimRepository = expenseClaimRepository;
        this.advanceRequestRepository = advanceRequestRepository;
        this.policyExceptionRepository = policyExceptionRepository;
        this.userRepository = userRepository;
        this.gradeRepository = gradeRepository;
        this.travelPolicyRepository = travelPolicyRepository;
        this.itineraryLegRepository = itineraryLegRepository;
        this.visaRequirementRepository = visaRequirementRepository;
        this.complianceAuditRepository = complianceAuditRepository;
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

                summary.put("totalTrips", myTrips.size());
                summary.put("upcomingTrips", myTrips.stream().filter(t -> t.getStatus() == TripStatus.APPROVED).count());
                summary.put("draftTrips", myTrips.stream().filter(t -> t.getStatus() == TripStatus.DRAFT).count());
                summary.put("pendingApprovalTrips", myTrips.stream().filter(t -> t.getStatus() == TripStatus.SUBMITTED).count());
                summary.put("approvedTrips", myTrips.stream().filter(t -> t.getStatus() == TripStatus.APPROVED).count());
                summary.put("rejectedTrips", myTrips.stream().filter(t -> t.getStatus() == TripStatus.REJECTED).count());
                summary.put("completedTrips", myTrips.stream().filter(t -> t.getStatus() == TripStatus.COMPLETED).count());

                summary.put("totalExpenseClaims", myClaims.size());
                summary.put("draftClaims", myClaims.stream().filter(c -> c.getStatus() == ExpenseStatus.DRAFT).count());
                summary.put("submittedClaims", myClaims.stream().filter(c -> c.getStatus() == ExpenseStatus.SUBMITTED).count());
                summary.put("approvedClaims", myClaims.stream().filter(c -> c.getStatus() == ExpenseStatus.APPROVED).count());
                summary.put("rejectedClaims", myClaims.stream().filter(c -> c.getStatus() == ExpenseStatus.REJECTED).count());
                summary.put("paidClaims", myClaims.stream().filter(c -> c.getStatus() == ExpenseStatus.PAID).count());

                summary.put("advanceRequests", myAdvances.size());
                summary.put("pendingAdvances", myAdvances.stream().filter(a -> a.getStatus() == AdvanceStatus.REQUESTED).count());
                summary.put("approvedAdvances", myAdvances.stream().filter(a -> a.getStatus() == AdvanceStatus.APPROVED).count());
                summary.put("settledAdvances", myAdvances.stream().filter(a -> a.getStatus() == AdvanceStatus.SETTLED).count());
                
                BigDecimal activeAdvanceSum = myAdvances.stream()
                        .filter(a -> a.getStatus() == AdvanceStatus.DISBURSED)
                        .map(a -> a.getUsdEquivalent() != null ? a.getUsdEquivalent() : a.getRequestedAmount())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                summary.put("activeCashAdvanceAmount", activeAdvanceSum);
                break;

            case APPROVING_MANAGER:
                List<TripRequest> managerTrips = tripRequestRepository.findByApprover_Id(effectiveUserId);
                summary.put("pendingTripApprovals", managerTrips.stream().filter(t -> t.getStatus() == TripStatus.SUBMITTED).count());
                
                List<ExpenseClaim> allClaimsManager = expenseClaimRepository.findAll();
                summary.put("pendingExpenseApprovals", allClaimsManager.stream().filter(c -> c.getStatus() == ExpenseStatus.SUBMITTED).count());

                List<AdvanceRequest> allAdvancesManager = advanceRequestRepository.findAll();
                summary.put("pendingAdvanceRequests", allAdvancesManager.stream().filter(a -> a.getStatus() == AdvanceStatus.REQUESTED).count());

                summary.put("teamTravelSummaryCount", managerTrips.size());
                summary.put("teamExpenseSummaryCount", allClaimsManager.size());

                long travellingNow = managerTrips.stream().filter(t -> t.getStatus() == TripStatus.APPROVED).count();
                summary.put("employeesCurrentlyTravelling", travellingNow);
                break;

            case TRAVEL_DESK:
                List<TripRequest> approvedTripsTD = tripRequestRepository.findAll().stream()
                        .filter(t -> t.getStatus() == TripStatus.APPROVED)
                        .collect(Collectors.toList());
                summary.put("pendingBookings", approvedTripsTD.size());
                summary.put("flightBookings", itineraryLegRepository.count());
                summary.put("hotelBookings", itineraryLegRepository.count());
                summary.put("visaRequests", visaRequirementRepository.count());
                summary.put("completedItineraries", tripRequestRepository.findAll().stream().filter(t -> t.getStatus() == TripStatus.COMPLETED).count());
                summary.put("upcomingTravel", approvedTripsTD.size());
                break;

            case FINANCE:
                List<ExpenseClaim> allClaimsFin = expenseClaimRepository.findAll();
                List<AdvanceRequest> allAdvancesFin = advanceRequestRepository.findAll();

                BigDecimal totalSpend = allClaimsFin.stream()
                        .filter(c -> c.getStatus() == ExpenseStatus.APPROVED || c.getStatus() == ExpenseStatus.PAID)
                        .map(c -> c.getUsdEquivalent() != null ? c.getUsdEquivalent() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal disbursedAdv = allAdvancesFin.stream()
                        .filter(a -> a.getStatus() == AdvanceStatus.DISBURSED || a.getStatus() == AdvanceStatus.SETTLED)
                        .map(a -> a.getUsdEquivalent() != null ? a.getUsdEquivalent() : a.getRequestedAmount())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                summary.put("pendingReimbursements", allClaimsFin.stream().filter(c -> c.getStatus() == ExpenseStatus.APPROVED).count());
                summary.put("pendingAdvanceDisbursements", allAdvancesFin.stream().filter(a -> a.getStatus() == AdvanceStatus.APPROVED).count());
                summary.put("processedPayments", allClaimsFin.stream().filter(c -> c.getStatus() == ExpenseStatus.PAID).count());
                summary.put("failedPayments", 0);
                summary.put("totalBudgetAllocated", new BigDecimal("1000000.00"));
                summary.put("totalDisbursedAdvances", disbursedAdv);
                summary.put("totalExpenseSpend", totalSpend);
                break;

            case COMPLIANCE:
                List<PolicyException> exceptions = policyExceptionRepository.findAll();
                summary.put("policyExceptions", exceptions.size());
                summary.put("openExceptions", exceptions.stream().filter(e -> "PENDING".equalsIgnoreCase(e.getApprovalStatus())).count());
                summary.put("highValueClaims", expenseClaimRepository.findAll().stream().filter(c -> c.getTotalAmount() != null && c.getTotalAmount().compareTo(new BigDecimal("5000")) >= 0).count());
                summary.put("auditSummaryCount", complianceAuditRepository.count());
                break;

            case ADMIN:
            default:
                List<User> allUsers = userRepository.findAll();
                summary.put("users", allUsers.size());
                summary.put("activeUsers", allUsers.stream().filter(User::isActive).count());
                summary.put("inactiveUsers", allUsers.stream().filter(u -> !u.isActive()).count());
                summary.put("departments", 5);
                summary.put("roles", Role.values().length);
                summary.put("trips", tripRequestRepository.count());
                summary.put("expenses", expenseClaimRepository.count());
                summary.put("policies", travelPolicyRepository.count());
                summary.put("advances", advanceRequestRepository.count());
                summary.put("complianceCases", policyExceptionRepository.count());
                summary.put("activeGrades", gradeRepository.count());
                break;
        }

        return ResponseEntity.ok(summary);
    }
}
