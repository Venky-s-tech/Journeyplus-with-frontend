package com.journeyplus.advance.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.journeyplus.advance.entity.AdvanceRequest;
import com.journeyplus.advance.entity.AdvanceSettlement;
import com.journeyplus.advance.entity.AdvanceStatus;
import com.journeyplus.advance.entity.SettlementStatus;
import com.journeyplus.advance.repository.AdvanceRequestRepository;
import com.journeyplus.advance.repository.AdvanceSettlementRepository;
import com.journeyplus.config.AuditAction;
import com.journeyplus.event.StatusChangeEvent;
import com.journeyplus.iam.entity.User;
import com.journeyplus.iam.repository.UserRepository;
import com.journeyplus.trip.entity.TripRequest;
import com.journeyplus.trip.entity.TripStatus;
import com.journeyplus.trip.service.TripService;

@Service
public class AdvanceService {

    private static final Logger log = LoggerFactory.getLogger(AdvanceService.class);

    private final AdvanceRequestRepository advanceRequestRepository;
    private final AdvanceSettlementRepository advanceSettlementRepository;
    private final TripService tripService;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;

    public AdvanceService(
            AdvanceRequestRepository advanceRequestRepository,
            AdvanceSettlementRepository advanceSettlementRepository,
            TripService tripService,
            ApplicationEventPublisher eventPublisher,
            UserRepository userRepository) {
        this.advanceRequestRepository = advanceRequestRepository;
        this.advanceSettlementRepository = advanceSettlementRepository;
        this.tripService = tripService;
        this.eventPublisher = eventPublisher;
        this.userRepository = userRepository;
    }

    @Transactional
    @AuditAction(module = "ADVANCE", action = "CREATE_ADVANCE")
    public AdvanceRequest createAdvanceRequest(AdvanceRequest request) {
        // Enforce: Advance can only be requested against an APPROVED trip
        TripRequest trip = tripService.getTripRequest(request.getTripRequest().getId());
        if (trip.getStatus() != TripStatus.APPROVED) {
            throw new IllegalArgumentException("Advance request can only be created against an APPROVED trip");
        }

        // Enforce: Employee can request advance only for their own trip
        if (!trip.getEmployee().getId().equals(request.getEmployee().getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Employee can request advance only for their own trip");
        }

        // Enforce: RequestedAmount must be positive
        if (request.getRequestedAmount() == null || request.getRequestedAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("RequestedAmount must be positive");
        }

        // Enforce: Currency is mandatory
        if (request.getCurrency() == null || request.getCurrency().isBlank()) {
            throw new IllegalArgumentException("Currency is required");
        }

        // Enforce: PurposeDetails is mandatory
        if (request.getPurposeDetails() == null || request.getPurposeDetails().isBlank()) {
            throw new IllegalArgumentException("PurposeDetails is required");
        }

        request.setStatus(AdvanceStatus.REQUESTED);
        request.setTripRequest(trip);
        AdvanceRequest saved = advanceRequestRepository.save(request);

        // Notify Approving Manager
        if (trip.getApprover() != null) {
            eventPublisher.publishEvent(new StatusChangeEvent(
                trip.getApprover().getId(),
                "New Advance Request",
                "A cash advance of " + request.getRequestedAmount() + " " + request.getCurrency() +
                " has been requested by " + request.getEmployee().getUsername() + ".",
                request.getEmployee() != null ? request.getEmployee().getId() : null,
                request.getEmployee() != null ? request.getEmployee().getUsername() : null,
                com.journeyplus.notification.entity.NotificationCategory.Advance
            ));
        }

        notifyEmployeeAndFinance(saved.getEmployee().getId(), "Advance Request Submitted", "An advance request of " + saved.getRequestedAmount() + " " + saved.getCurrency() + " has been submitted.", null, null);

        return saved;
    }

    @Transactional
    @AuditAction(module = "ADVANCE", action = "UPDATE_ADVANCE")
    public AdvanceRequest updateAdvanceRequest(Long id, AdvanceRequest updatedData, User employee) {
        AdvanceRequest existing = getAdvanceRequest(id);

        // Enforce ownership
        if (!existing.getEmployee().getId().equals(employee.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Only the Advance Owner can edit this request");
        }

        // Enforce only REQUESTED can be edited
        if (existing.getStatus() != AdvanceStatus.REQUESTED) {
            throw new IllegalStateException("Only REQUESTED advance requests can be edited");
        }

        existing.setRequestedAmount(updatedData.getRequestedAmount());
        existing.setCurrency(updatedData.getCurrency());
        existing.setPurposeDetails(updatedData.getPurposeDetails());
        existing.setUsdEquivalent(updatedData.getUsdEquivalent());

        // Validate rules again
        if (existing.getRequestedAmount() == null || existing.getRequestedAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("RequestedAmount must be positive");
        }
        if (existing.getCurrency() == null || existing.getCurrency().isBlank()) {
            throw new IllegalArgumentException("Currency is required");
        }
        if (existing.getPurposeDetails() == null || existing.getPurposeDetails().isBlank()) {
            throw new IllegalArgumentException("PurposeDetails is required");
        }

        return advanceRequestRepository.save(existing);
    }

    @Transactional
    @AuditAction(module = "ADVANCE", action = "APPROVE_ADVANCE")
    public AdvanceRequest approveAdvanceRequest(Long id, User approver) {
        AdvanceRequest request = advanceRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Advance request not found"));

        // Validate Transition REQUESTED -> APPROVED
        if (request.getStatus() != AdvanceStatus.REQUESTED) {
            throw new IllegalStateException("Only REQUESTED advance requests can be approved");
        }

        // Enforce that the approver is the assigned manager of the trip request or their active delegate
        TripRequest trip = request.getTripRequest();
        boolean isAssignedApprover = trip.getApprover() != null && trip.getApprover().getId().equals(approver.getId());
        boolean isDelegateApprover = trip.getApprover() != null && trip.getApprover().getDelegateApprover() != null
                && trip.getApprover().getDelegateApprover().getId().equals(approver.getId())
                && trip.getApprover().isDelegationActive();

        if (!isAssignedApprover && !isDelegateApprover) {
            throw new org.springframework.security.access.AccessDeniedException("Only the assigned approving manager or their active delegate can approve this advance");
        }

        request.setStatus(AdvanceStatus.APPROVED);
        request.setApprovedBy(approver);
        AdvanceRequest saved = advanceRequestRepository.save(request);

        notifyEmployeeAndFinance(
            request.getEmployee().getId(),
            "Advance Request Approved",
            "Your cash advance request for " + request.getRequestedAmount() + " " + request.getCurrency() +
            " has been approved by " + approver.getUsername() + ".",
            approver != null ? approver.getId() : null,
            approver != null ? approver.getUsername() : null
        );

        return saved;
    }

    @Transactional
    @AuditAction(module = "ADVANCE", action = "DISBURSE_ADVANCE")
    public AdvanceRequest disburseAdvanceRequest(Long id) {
        AdvanceRequest request = advanceRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Advance request not found"));

        // Validate Transition APPROVED -> DISBURSED
        if (request.getStatus() != AdvanceStatus.APPROVED) {
            throw new IllegalStateException("Only APPROVED advance requests can be disbursed");
        }

        request.setStatus(AdvanceStatus.DISBURSED);
        request.setDisbursementDate(LocalDate.now());
        AdvanceRequest saved = advanceRequestRepository.save(request);

        notifyEmployeeAndFinance(
            request.getEmployee().getId(),
            "Advance Disbursed",
            "Your cash advance of " + request.getRequestedAmount() + " " + request.getCurrency() +
            " has been disbursed to your account.",
            null,
            null
        );

        return saved;
    }

    @Transactional
    @AuditAction(module = "ADVANCE", action = "FORFEIT_ADVANCE")
    public AdvanceRequest forfeitAdvanceRequest(Long id) {
        AdvanceRequest request = advanceRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Advance request not found"));

        // Validate Transition DISBURSED -> FORFEITED (Block APPROVED -> FORFEITED)
        if (request.getStatus() != AdvanceStatus.DISBURSED) {
            throw new IllegalStateException("Only DISBURSED advance requests can be forfeited");
        }

        request.setStatus(AdvanceStatus.FORFEITED);
        AdvanceRequest saved = advanceRequestRepository.save(request);

        notifyEmployeeAndFinance(
            request.getEmployee().getId(),
            "Advance Request Forfeited",
            "Your cash advance request of " + request.getRequestedAmount() + " " + request.getCurrency() +
            " has been forfeited.",
            null,
            null
        );

        return saved;
    }

    // ==========================================
    // SETTLEMENTS CRUD & CALCULATIONS
    // ==========================================

    @Transactional
    public AdvanceSettlement addSettlement(Long advanceId, AdvanceSettlement settlement) {
        AdvanceRequest request = getAdvanceRequest(advanceId);

        // Enforce: Settlement can only occur after advance is DISBURSED
        if (request.getStatus() != AdvanceStatus.DISBURSED) {
            throw new IllegalStateException("Settlement can only occur after advance is DISBURSED");
        }

        // Enforce: If advance is linked to a trip, the trip must be COMPLETED before settlement
        if (request.getTripRequest() != null && request.getTripRequest().getStatus() != com.journeyplus.trip.entity.TripStatus.COMPLETED) {
            throw new IllegalStateException("Advance settlement can only occur after the trip is marked as COMPLETED.");
        }

        // Validate amounts
        if (settlement.getAmountUtilised() == null || settlement.getAmountUtilised().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("AmountUtilised must be non-negative");
        }
        if (settlement.getAmountReturned() == null || settlement.getAmountReturned().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("AmountReturned must be non-negative");
        }

        settlement.setAdvanceRequest(request);
        settlement.setSettlementDate(LocalDate.now());

        // Perform settlement math
        calculateSettlementStatusAndOutstanding(request, settlement);

        AdvanceSettlement saved = advanceSettlementRepository.save(settlement);

        // Update AdvanceRequest status if fully accounted for
        List<AdvanceSettlement> allSettlements = advanceSettlementRepository.findByAdvanceRequest_Id(advanceId);
        allSettlements.add(saved); // include the new one

        BigDecimal totalUtilised = allSettlements.stream()
                .map(AdvanceSettlement::getAmountUtilised)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalReturned = allSettlements.stream()
                .map(AdvanceSettlement::getAmountReturned)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalAccounted = totalUtilised.add(totalReturned);

        if (totalUtilised.compareTo(request.getRequestedAmount()) >= 0 || totalAccounted.compareTo(request.getRequestedAmount()) >= 0) {
            request.setStatus(AdvanceStatus.SETTLED);
            advanceRequestRepository.save(request);

            notifyEmployeeAndFinance(
                request.getEmployee().getId(),
                "Advance Request Settled",
                "Your cash advance of " + request.getRequestedAmount() + " " + request.getCurrency() +
                " has been successfully settled.",
                null,
                null
            );
        }

        return saved;
    }

    public AdvanceSettlement getSettlement(Long settlementId) {
        return advanceSettlementRepository.findById(settlementId)
                .orElseThrow(() -> new IllegalArgumentException("Advance settlement not found"));
    }

    @Transactional
    public AdvanceSettlement updateSettlement(Long settlementId, AdvanceSettlement updatedData) {
        AdvanceSettlement existing = getSettlement(settlementId);
        AdvanceRequest request = existing.getAdvanceRequest();

        if (updatedData.getAmountUtilised() == null || updatedData.getAmountUtilised().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("AmountUtilised must be non-negative");
        }
        if (updatedData.getAmountReturned() == null || updatedData.getAmountReturned().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("AmountReturned must be non-negative");
        }

        existing.setAmountUtilised(updatedData.getAmountUtilised());
        existing.setAmountReturned(updatedData.getAmountReturned());
        if (updatedData.getRemarks() != null) existing.setRemarks(updatedData.getRemarks());

        calculateSettlementStatusAndOutstanding(request, existing);

        return advanceSettlementRepository.save(existing);
    }

    public List<AdvanceSettlement> getSettlementsForAdvance(Long advanceId) {
        return advanceSettlementRepository.findByAdvanceRequest_Id(advanceId);
    }

    private void calculateSettlementStatusAndOutstanding(AdvanceRequest request, AdvanceSettlement settlement) {
        BigDecimal advanceAmount = request.getRequestedAmount();
        BigDecimal utilised = settlement.getAmountUtilised();
        BigDecimal returned = settlement.getAmountReturned();

        // Rules:
        // 1. If utilised > advanceAmount: EXCESS
        if (utilised.compareTo(advanceAmount) > 0) {
            settlement.setStatus(SettlementStatus.EXCESS);
            // Force returned to 0 in case of excess
            settlement.setAmountReturned(BigDecimal.ZERO);
        }
        // 2. If utilised + returned == advanceAmount: SETTLED
        else if (utilised.add(returned).compareTo(advanceAmount) == 0) {
            settlement.setStatus(SettlementStatus.SETTLED);
        }
        // 3. If utilised + returned < advanceAmount: PARTIALLY_SETTLED
        else if (utilised.add(returned).compareTo(advanceAmount) < 0) {
            settlement.setStatus(SettlementStatus.PARTIALLY_SETTLED);
        }
        // 4. If utilised + returned > advanceAmount but utilised <= advanceAmount: Invalid!
        else {
            throw new IllegalArgumentException("Sum of Utilised and Returned amounts cannot exceed the Advance Amount when utilised is less than or equal to advance");
        }
    }

    // ==========================================
    // LEGACY & UTILITY METHODS
    // ==========================================

    @Deprecated
    @Transactional
    @AuditAction(module = "ADVANCE", action = "SETTLE_ADVANCE")
    public AdvanceRequest settleAdvanceRequest(Long id, AdvanceSettlement settlement) {
        addSettlement(id, settlement);
        return getAdvanceRequest(id);
    }

    public AdvanceRequest getAdvanceRequest(Long id) {
        return advanceRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Advance request not found"));
    }

    public List<AdvanceRequest> getAdvancesByEmployee(Long employeeId) {
        return advanceRequestRepository.findByEmployee_Id(employeeId);
    }

    public List<AdvanceRequest> getPendingApprovals(Long managerId) {
        return advanceRequestRepository.findByStatusAndTripRequest_Approver_Id(AdvanceStatus.REQUESTED, managerId);
    }

    public List<AdvanceRequest> getPendingDisbursements() {
        return advanceRequestRepository.findByStatus(AdvanceStatus.APPROVED);
    }

    public List<AdvanceRequest> filterAdvances(AdvanceStatus status, Long employeeId, Long tripId, String currency) {
        return advanceRequestRepository.findByStatusAndEmployee_IdAndTripRequest_IdAndCurrencyIgnoreCase(status, employeeId, tripId, currency);
    }

    private void notifyEmployeeAndFinance(Long employeeId, String title, String message, Long actorId, String actorName) {
        try {
            eventPublisher.publishEvent(new StatusChangeEvent(
                employeeId,
                title,
                message,
                actorId,
                actorName,
                com.journeyplus.notification.entity.NotificationCategory.Advance
            ));
        } catch (Exception e) {
            log.warn("Failed to notify employee: {}", e.getMessage());
        }

        try {
            List<User> financeUsers = userRepository.findAll().stream()
                .filter(u -> u.getRole() == com.journeyplus.iam.entity.Role.FINANCE)
                .toList();
            for (User fin : financeUsers) {
                eventPublisher.publishEvent(new StatusChangeEvent(
                    fin.getId(),
                    title,
                    message,
                    actorId,
                    actorName,
                    com.journeyplus.notification.entity.NotificationCategory.Advance
                ));
            }
        } catch (Exception e) {
            log.warn("Failed to notify Finance users: {}", e.getMessage());
        }
    }
}
