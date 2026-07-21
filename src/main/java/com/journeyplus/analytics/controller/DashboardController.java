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
import com.journeyplus.trip.entity.ItineraryLeg;
import com.journeyplus.trip.entity.TripRequest;
import com.journeyplus.trip.entity.TripStatus;
import com.journeyplus.trip.entity.VisaRequirement;
import com.journeyplus.trip.repository.ItineraryLegRepository;
import com.journeyplus.trip.repository.TripRequestRepository;
import com.journeyplus.trip.repository.VisaRequirementRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private User getCurrentUser(User principal) {
        if (principal != null) {
            return principal;
        }
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof User) {
                return (User) auth.getPrincipal();
            }
            if (auth != null && auth.getName() != null) {
                return userRepository.findByUsername(auth.getName()).orElse(null);
            }
        } catch (Exception ignored) {}
        return null;
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAdminDashboard() {
        List<User> allUsers = userRepository.findAll();
        List<TripRequest> allTrips = tripRequestRepository.findAll();
        List<ExpenseClaim> allClaims = expenseClaimRepository.findAll();
        List<AdvanceRequest> allAdvances = advanceRequestRepository.findAll();

        BigDecimal totalClaimAmt = allClaims.stream()
                .map(c -> c.getUsdEquivalent() != null ? c.getUsdEquivalent() : (c.getTotalAmount() != null ? c.getTotalAmount() : BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalUsers", allUsers.size());
        summary.put("activeUsers", allUsers.stream().filter(User::isActive).count());
        summary.put("inactiveUsers", allUsers.stream().filter(u -> !u.isActive()).count());
        summary.put("pendingUserApprovals", allUsers.stream().filter(u -> u.getApprovalStatus() != null && "PENDING".equalsIgnoreCase(u.getApprovalStatus())).count());

        summary.put("totalTrips", allTrips.size());
        summary.put("draftTrips", allTrips.stream().filter(t -> t.getStatus() == TripStatus.DRAFT).count());
        summary.put("submittedTrips", allTrips.stream().filter(t -> t.getStatus() == TripStatus.SUBMITTED).count());
        summary.put("approvedTrips", allTrips.stream().filter(t -> t.getStatus() == TripStatus.APPROVED).count());
        summary.put("rejectedTrips", allTrips.stream().filter(t -> t.getStatus() == TripStatus.REJECTED).count());
        summary.put("completedTrips", allTrips.stream().filter(t -> t.getStatus() == TripStatus.COMPLETED).count());
        summary.put("cancelledTrips", allTrips.stream().filter(t -> t.getStatus() == TripStatus.CANCELLED).count());

        summary.put("totalExpenseClaims", allClaims.size());
        summary.put("pendingExpenseClaims", allClaims.stream().filter(c -> c.getStatus() == ExpenseStatus.SUBMITTED).count());
        summary.put("approvedExpenseClaims", allClaims.stream().filter(c -> c.getStatus() == ExpenseStatus.APPROVED).count());
        summary.put("rejectedExpenseClaims", allClaims.stream().filter(c -> c.getStatus() == ExpenseStatus.REJECTED).count());
        summary.put("paidClaims", allClaims.stream().filter(c -> c.getStatus() == ExpenseStatus.PAID).count());
        summary.put("totalExpenseAmount", totalClaimAmt);

        summary.put("totalAdvances", allAdvances.size());
        summary.put("pendingAdvances", allAdvances.stream().filter(a -> a.getStatus() == AdvanceStatus.REQUESTED).count());
        summary.put("approvedAdvances", allAdvances.stream().filter(a -> a.getStatus() == AdvanceStatus.APPROVED).count());
        summary.put("disbursedAdvances", allAdvances.stream().filter(a -> a.getStatus() == AdvanceStatus.DISBURSED).count());
        summary.put("settledAdvances", allAdvances.stream().filter(a -> a.getStatus() == AdvanceStatus.SETTLED).count());

        summary.put("totalPolicies", travelPolicyRepository.count());
        summary.put("activePolicies", travelPolicyRepository.count());
        summary.put("unreadNotifications", 0);
        summary.put("totalComplianceCases", policyExceptionRepository.count());

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/employee")
    public ResponseEntity<Map<String, Object>> getEmployeeDashboard(@AuthenticationPrincipal User user) {
        User currentUser = getCurrentUser(user);
        Long userId = currentUser != null ? currentUser.getId() : 1L;

        List<TripRequest> myTrips = userId != null ? tripRequestRepository.findByEmployee_Id(userId) : List.of();
        List<ExpenseClaim> myClaims = userId != null ? expenseClaimRepository.findByEmployee_Id(userId) : List.of();
        List<AdvanceRequest> myAdvances = userId != null ? advanceRequestRepository.findByEmployee_Id(userId) : List.of();

        BigDecimal activeAdvSum = myAdvances.stream()
                .filter(a -> a.getStatus() == AdvanceStatus.DISBURSED)
                .map(a -> a.getUsdEquivalent() != null ? a.getUsdEquivalent() : (a.getRequestedAmount() != null ? a.getRequestedAmount() : BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> summary = new HashMap<>();
        summary.put("myTrips", myTrips.size());
        summary.put("upcomingTrips", myTrips.stream().filter(t -> t.getStatus() == TripStatus.APPROVED).count());
        summary.put("pendingTrips", myTrips.stream().filter(t -> t.getStatus() == TripStatus.SUBMITTED).count());
        summary.put("approvedTrips", myTrips.stream().filter(t -> t.getStatus() == TripStatus.APPROVED).count());
        summary.put("rejectedTrips", myTrips.stream().filter(t -> t.getStatus() == TripStatus.REJECTED).count());
        summary.put("completedTrips", myTrips.stream().filter(t -> t.getStatus() == TripStatus.COMPLETED).count());

        summary.put("myExpenseClaims", myClaims.size());
        summary.put("pendingClaims", myClaims.stream().filter(c -> c.getStatus() == ExpenseStatus.SUBMITTED).count());
        summary.put("approvedClaims", myClaims.stream().filter(c -> c.getStatus() == ExpenseStatus.APPROVED).count());
        summary.put("rejectedClaims", myClaims.stream().filter(c -> c.getStatus() == ExpenseStatus.REJECTED).count());
        summary.put("paidClaims", myClaims.stream().filter(c -> c.getStatus() == ExpenseStatus.PAID).count());

        summary.put("myAdvances", myAdvances.size());
        summary.put("pendingAdvances", myAdvances.stream().filter(a -> a.getStatus() == AdvanceStatus.REQUESTED).count());
        summary.put("approvedAdvances", myAdvances.stream().filter(a -> a.getStatus() == AdvanceStatus.APPROVED).count());
        summary.put("activeCashAdvanceAmount", activeAdvSum);

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/manager")
    public ResponseEntity<Map<String, Object>> getManagerDashboard(@AuthenticationPrincipal User user) {
        User currentUser = getCurrentUser(user);
        Long managerId = currentUser != null ? currentUser.getId() : 1L;
        List<TripRequest> managerTrips = managerId != null ? tripRequestRepository.findByApprover_Id(managerId) : List.of();
        List<ExpenseClaim> allClaims = expenseClaimRepository.findAll();
        List<AdvanceRequest> allAdvances = advanceRequestRepository.findAll();

        BigDecimal teamExpenseAmt = allClaims.stream()
                .filter(c -> c.getStatus() == ExpenseStatus.APPROVED || c.getStatus() == ExpenseStatus.PAID)
                .map(c -> c.getUsdEquivalent() != null ? c.getUsdEquivalent() : (c.getTotalAmount() != null ? c.getTotalAmount() : BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> summary = new HashMap<>();
        summary.put("tripsAwaitingApproval", managerTrips.stream().filter(t -> t.getStatus() == TripStatus.SUBMITTED).count());
        summary.put("expenseClaimsAwaitingApproval", allClaims.stream().filter(c -> c.getStatus() == ExpenseStatus.SUBMITTED).count());
        summary.put("advanceRequestsAwaitingApproval", allAdvances.stream().filter(a -> a.getStatus() == AdvanceStatus.REQUESTED).count());
        summary.put("teamTripCount", managerTrips.size());
        summary.put("teamExpenseAmount", teamExpenseAmt);
        summary.put("employeesCurrentlyTravelling", managerTrips.stream().filter(t -> t.getStatus() == TripStatus.APPROVED).count());
        summary.put("recentApprovals", managerTrips.stream().filter(t -> t.getStatus() == TripStatus.APPROVED || t.getStatus() == TripStatus.REJECTED).count());

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/finance")
    public ResponseEntity<Map<String, Object>> getFinanceDashboard() {
        List<ExpenseClaim> allClaims = expenseClaimRepository.findAll();
        List<AdvanceRequest> allAdvances = advanceRequestRepository.findAll();

        BigDecimal monthlyReimbursementAmt = allClaims.stream()
                .filter(c -> c.getStatus() == ExpenseStatus.PAID)
                .map(c -> c.getUsdEquivalent() != null ? c.getUsdEquivalent() : (c.getTotalAmount() != null ? c.getTotalAmount() : BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal monthlyAdvAmt = allAdvances.stream()
                .filter(a -> a.getStatus() == AdvanceStatus.DISBURSED || a.getStatus() == AdvanceStatus.SETTLED)
                .map(a -> a.getUsdEquivalent() != null ? a.getUsdEquivalent() : (a.getRequestedAmount() != null ? a.getRequestedAmount() : BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> summary = new HashMap<>();
        summary.put("pendingReimbursements", allClaims.stream().filter(c -> c.getStatus() == ExpenseStatus.APPROVED).count());
        summary.put("pendingDisbursements", allAdvances.stream().filter(a -> a.getStatus() == AdvanceStatus.APPROVED).count());
        summary.put("processedPayments", allClaims.stream().filter(c -> c.getStatus() == ExpenseStatus.PAID).count());
        summary.put("failedPayments", 0);
        summary.put("monthlyReimbursementAmount", monthlyReimbursementAmt);
        summary.put("monthlyAdvanceAmount", monthlyAdvAmt);
        summary.put("totalBudgetAllocated", new BigDecimal("1000000.00"));
        summary.put("totalDisbursedAdvances", monthlyAdvAmt);

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/traveldesk")
    public ResponseEntity<Map<String, Object>> getTravelDeskDashboard() {
        List<TripRequest> allTrips = tripRequestRepository.findAll();
        List<TripRequest> approvedTrips = allTrips.stream()
                .filter(t -> t.getStatus() == TripStatus.APPROVED)
                .collect(Collectors.toList());

        List<ItineraryLeg> allLegs = itineraryLegRepository.findAll();
        List<VisaRequirement> allVisas = visaRequirementRepository.findAll();

        Map<Long, List<ItineraryLeg>> legsByTrip = allLegs.stream()
                .filter(l -> l.getTripRequest() != null)
                .collect(Collectors.groupingBy(l -> l.getTripRequest().getId()));

        Map<Long, List<VisaRequirement>> visasByTrip = allVisas.stream()
                .filter(v -> v.getTripRequest() != null)
                .collect(Collectors.groupingBy(v -> v.getTripRequest().getId()));

        long pendingBookings = 0;
        long completedItineraries = 0;
        long visaRequests = 0;
        long flightBookings = 0;
        long hotelBookings = 0;

        for (TripRequest t : approvedTrips) {
            List<ItineraryLeg> legs = legsByTrip.getOrDefault(t.getId(), java.util.Collections.emptyList());
            boolean hasLegs = !legs.isEmpty();
            boolean allBooked = hasLegs && legs.stream().allMatch(l -> l.getStatus() == com.journeyplus.trip.entity.ItineraryStatus.CONFIRMED);

            String bookingStatus = t.getBookingStatus();
            boolean isPendingBooking = bookingStatus == null || "PENDING_BOOKING".equalsIgnoreCase(bookingStatus) || "PENDING".equalsIgnoreCase(bookingStatus);

            if (!hasLegs || isPendingBooking || !allBooked) {
                pendingBookings++;
            }

            if (allBooked) {
                completedItineraries++;
            }

            if (hasLegs) {
                for (ItineraryLeg leg : legs) {
                    if (leg.getLegType() == com.journeyplus.trip.entity.LegType.HOTEL) {
                        hotelBookings++;
                    } else {
                        flightBookings++;
                    }
                }
            }

            if ("INTERNATIONAL".equalsIgnoreCase(t.getTravelType())) {
                List<VisaRequirement> visas = visasByTrip.getOrDefault(t.getId(), java.util.Collections.emptyList());
                boolean visaApproved = !visas.isEmpty() && visas.stream().allMatch(v -> 
                    v.getStatus() == com.journeyplus.trip.dto.VisaStatus.GRANTED
                );
                if (!visaApproved) {
                    visaRequests++;
                }
            }
        }

        long alreadyCompletedTrips = allTrips.stream().filter(t -> t.getStatus() == TripStatus.COMPLETED).count();

        Map<String, Object> summary = new HashMap<>();
        summary.put("pendingBookings", pendingBookings);
        summary.put("upcomingTrips", approvedTrips.size());
        summary.put("completedItineraries", completedItineraries + alreadyCompletedTrips);
        summary.put("visaRequests", visaRequests);
        summary.put("travelRequests", allTrips.size());
        summary.put("flightBookings", flightBookings);
        summary.put("hotelBookings", hotelBookings);

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/compliance")
    public ResponseEntity<Map<String, Object>> getComplianceDashboard() {
        List<PolicyException> exceptions = policyExceptionRepository.findAll();

        Map<String, Object> summary = new HashMap<>();
        summary.put("openExceptions", exceptions.stream().filter(e -> "PENDING".equalsIgnoreCase(e.getApprovalStatus())).count());
        summary.put("closedExceptions", exceptions.stream().filter(e -> e.getApprovalStatus() != null && !"PENDING".equalsIgnoreCase(e.getApprovalStatus())).count());
        summary.put("escalatedExceptions", exceptions.stream().filter(e -> "REJECTED".equalsIgnoreCase(e.getApprovalStatus())).count());
        summary.put("policyViolations", exceptions.size());
        summary.put("highValueClaims", expenseClaimRepository.findAll().stream().filter(c -> c.getTotalAmount() != null && c.getTotalAmount().compareTo(new BigDecimal("5000")) >= 0).count());
        summary.put("auditSummaryCount", complianceAuditRepository.count());

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getDashboardSummary(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Long userId,
            @AuthenticationPrincipal User user) {

        User currentUser = getCurrentUser(user);
        Role effectiveRole = role != null ? role : (currentUser != null ? currentUser.getRole() : Role.ADMIN);
        switch (effectiveRole) {
            case ADMIN:
                return getAdminDashboard();
            case EMPLOYEE:
                return getEmployeeDashboard(currentUser);
            case APPROVING_MANAGER:
                return getManagerDashboard(currentUser);
            case FINANCE:
                return getFinanceDashboard();
            case TRAVEL_DESK:
                return getTravelDeskDashboard();
            case COMPLIANCE:
                return getComplianceDashboard();
            default:
                return getAdminDashboard();
        }
    }
}
