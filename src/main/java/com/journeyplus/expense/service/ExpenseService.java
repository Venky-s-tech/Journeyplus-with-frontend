package com.journeyplus.expense.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.journeyplus.compliance.service.PolicyComplianceEngine;
import com.journeyplus.config.AuditAction;
import com.journeyplus.event.StatusChangeEvent;
import com.journeyplus.expense.entity.ExpenseClaim;
import com.journeyplus.expense.entity.ExpenseLine;
import com.journeyplus.expense.entity.ExpenseStatus;
import com.journeyplus.expense.entity.Reimbursement;
import com.journeyplus.expense.repository.ExpenseClaimRepository;
import com.journeyplus.expense.repository.ExpenseLineRepository;
import com.journeyplus.expense.repository.ReimbursementRepository;
import com.journeyplus.iam.entity.User;
import com.journeyplus.iam.repository.UserRepository;
import com.journeyplus.expense.dto.ExpenseLineRequest;
import com.journeyplus.advance.repository.AdvanceRequestRepository;

@Service
public class ExpenseService {

    private static final Logger log = LoggerFactory.getLogger(ExpenseService.class);

    private final ExpenseClaimRepository expenseClaimRepository;
    private final ExpenseLineRepository expenseLineRepository;
    private final ReimbursementRepository reimbursementRepository;
    private final AdvanceRequestRepository advanceRequestRepository;
    private final com.journeyplus.trip.repository.TripRequestRepository tripRequestRepository;
    private final com.journeyplus.advance.repository.AdvanceSettlementRepository advanceSettlementRepository;
    private final PolicyComplianceEngine complianceEngine;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;
    private final com.journeyplus.document.service.DocumentService documentService;

    public ExpenseService(
            ExpenseClaimRepository expenseClaimRepository,
            ExpenseLineRepository expenseLineRepository,
            ReimbursementRepository reimbursementRepository,
            AdvanceRequestRepository advanceRequestRepository,
            com.journeyplus.trip.repository.TripRequestRepository tripRequestRepository,
            com.journeyplus.advance.repository.AdvanceSettlementRepository advanceSettlementRepository,
            PolicyComplianceEngine complianceEngine,
            ApplicationEventPublisher eventPublisher,
            UserRepository userRepository,
            com.journeyplus.document.service.DocumentService documentService) {
        this.expenseClaimRepository = expenseClaimRepository;
        this.expenseLineRepository = expenseLineRepository;
        this.reimbursementRepository = reimbursementRepository;
        this.advanceRequestRepository = advanceRequestRepository;
        this.tripRequestRepository = tripRequestRepository;
        this.advanceSettlementRepository = advanceSettlementRepository;
        this.complianceEngine = complianceEngine;
        this.eventPublisher = eventPublisher;
        this.userRepository = userRepository;
        this.documentService = documentService;
    }

    // Standard static conversion rates for multi-currency conversion to USD
    private BigDecimal getExchangeRateToUsd(String currency) {
        if (currency == null) return BigDecimal.ONE;
        return switch (currency.toUpperCase()) {
            case "INR" -> new BigDecimal("0.012");
            case "EUR" -> new BigDecimal("1.08");
            case "GBP" -> new BigDecimal("1.25");
            case "JPY" -> new BigDecimal("0.0064");
            case "CAD" -> new BigDecimal("0.73");
            default -> BigDecimal.ONE;
        };
    }

    public void calculateAdvanceAndNetReimbursable(ExpenseClaim claim) {
        if (claim.getTripRequest() == null) {
            claim.setAdvanceAdjusted(BigDecimal.ZERO);
            claim.setNetReimbursable(claim.getTotalAmount());
            return;
        }

        // Find all advance requests for the trip
        List<com.journeyplus.advance.entity.AdvanceRequest> advances =
            advanceRequestRepository.findByTripRequest_Id(claim.getTripRequest().getId());

        BigDecimal totalAdvanceUsd = BigDecimal.ZERO;
        for (com.journeyplus.advance.entity.AdvanceRequest adv : advances) {
            if (adv.getStatus() == com.journeyplus.advance.entity.AdvanceStatus.DISBURSED ||
                adv.getStatus() == com.journeyplus.advance.entity.AdvanceStatus.SETTLED) {
                // Check if there are settlements for this advance request
                List<com.journeyplus.advance.entity.AdvanceSettlement> settlements =
                    advanceSettlementRepository.findByAdvanceRequest_Id(adv.getId());
                if (!settlements.isEmpty()) {
                    for (com.journeyplus.advance.entity.AdvanceSettlement set : settlements) {
                        BigDecimal rate = getExchangeRateToUsd(adv.getCurrency());
                        BigDecimal usd = set.getAmountUtilised().multiply(rate);
                        totalAdvanceUsd = totalAdvanceUsd.add(usd);
                    }
                } else {
                    // Fallback to full requested amount if not settled yet
                    if (adv.getUsdEquivalent() != null) {
                        totalAdvanceUsd = totalAdvanceUsd.add(adv.getUsdEquivalent());
                    } else {
                        BigDecimal rate = getExchangeRateToUsd(adv.getCurrency());
                        BigDecimal usd = adv.getRequestedAmount().multiply(rate);
                        totalAdvanceUsd = totalAdvanceUsd.add(usd);
                    }
                }
            }
        }

        // Convert totalAdvanceUsd to claim's original currency
        BigDecimal claimRate = getExchangeRateToUsd(claim.getOriginalCurrency());
        BigDecimal totalAdvanceClaimCurrency = BigDecimal.ZERO;
        if (claimRate.compareTo(BigDecimal.ZERO) > 0) {
            totalAdvanceClaimCurrency = totalAdvanceUsd.divide(claimRate, 2, RoundingMode.HALF_UP);
        }

        claim.setAdvanceAdjusted(totalAdvanceClaimCurrency);

        // netReimbursable = totalAmount - advanceAdjusted
        BigDecimal net = claim.getTotalAmount().subtract(totalAdvanceClaimCurrency);
        claim.setNetReimbursable(net);
    }

    @Transactional
    @AuditAction(module = "EXPENSE", action = "CREATE_EXPENSE_CLAIM")
    public ExpenseClaim createExpenseClaim(ExpenseClaim claim) {
        return createExpenseClaim(claim, null);
    }

    @Transactional
    @AuditAction(module = "EXPENSE", action = "CREATE_EXPENSE_CLAIM")
    public ExpenseClaim createExpenseClaim(ExpenseClaim claim, List<ExpenseLineRequest> lineRequests) {
        log.info("Attempting to create expense claim with title: '{}'", claim.getClaimTitle());
        if (claim.getTripRequest() == null || claim.getTripRequest().getId() == null) {
            throw new IllegalArgumentException("Trip request is required for creating an expense claim");
        }
        com.journeyplus.trip.entity.TripRequest trip = tripRequestRepository.findById(claim.getTripRequest().getId())
                .orElseThrow(() -> new IllegalArgumentException("Associated Trip Request not found"));
        if (trip.getStatus() != com.journeyplus.trip.entity.TripStatus.COMPLETED) {
            throw new IllegalStateException("Expense claims can only be created for completed trips.");
        }
        if (claim.getEmployee() != null && trip.getEmployee() != null && !trip.getEmployee().getId().equals(claim.getEmployee().getId())) {
            throw new org.springframework.security.access.AccessDeniedException("You can only raise expense claims against your own completed trips.");
        }

        // Check if expense lines already exist for this trip
        List<ExpenseClaim> existingClaims = expenseClaimRepository.findByTripRequest_Id(trip.getId());
        for (ExpenseClaim existingClaim : existingClaims) {
            if (!expenseLineRepository.findByExpenseClaim_Id(existingClaim.getId()).isEmpty()) {
                throw new IllegalStateException("Expense lines have already been created for this trip. Duplicate expense line creation is not allowed.");
            }
        }

        claim.setTripRequest(trip);

        claim.setStatus(ExpenseStatus.DRAFT);
        claim.setTotalAmount(BigDecimal.ZERO);
        claim.setUsdEquivalent(BigDecimal.ZERO);
        calculateAdvanceAndNetReimbursable(claim);
        ExpenseClaim saved = expenseClaimRepository.save(claim);
        log.info("Expense claim successfully created with ID: {}", saved.getId());

        if (lineRequests != null && !lineRequests.isEmpty()) {
            for (ExpenseLineRequest lineReq : lineRequests) {
                ExpenseLine line = new ExpenseLine();
                line.setExpenseDate(lineReq.getExpenseDate());
                line.setCategory(lineReq.getCategory());
                line.setAmount(lineReq.getAmount());
                line.setOriginalCurrency(lineReq.getOriginalCurrency());
                line.setReceiptPath(lineReq.getReceiptPath());

                line.setId(null);
                line.setExpenseClaim(saved);
                line.setPolicyComplianceStatus("COMPLIANT");
                line.setComplianceRemarks(null);

                if (line.getAmount() == null) {
                    throw new IllegalArgumentException("Expense line amount is required");
                }
                if (line.getOriginalCurrency() == null) {
                    line.setOriginalCurrency(saved.getOriginalCurrency());
                }

                BigDecimal rate = getExchangeRateToUsd(line.getOriginalCurrency());
                BigDecimal usdEquivalent = line.getAmount().multiply(rate).setScale(2, RoundingMode.HALF_UP);
                line.setUsdEquivalent(usdEquivalent);

                ExpenseLine savedLine = expenseLineRepository.save(line);
                complianceEngine.runComplianceCheck(savedLine);
                expenseLineRepository.save(savedLine);
            }
            recomputeClaimTotals(saved);
            saved = expenseClaimRepository.findById(saved.getId()).orElse(saved);
        }

        return saved;
    }

    @Transactional
    @AuditAction(module = "EXPENSE", action = "ADD_EXPENSE_LINE")
    public ExpenseLine addExpenseLine(Long claimId, ExpenseLine line) {
        log.info("Attempting to add expense line of category '{}', amount '{}' {} to claim ID: {}",
                line.getCategory(), line.getAmount(), line.getOriginalCurrency(), claimId);

        ExpenseClaim claim = expenseClaimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Expense claim not found"));

        if (claim.getStatus() != ExpenseStatus.DRAFT) {
            throw new IllegalStateException("Can only add expense lines to DRAFT claims");
        }

        line.setId(null);
        line.setExpenseClaim(claim);
        line.setPolicyComplianceStatus("COMPLIANT");
        line.setComplianceRemarks(null);

        if (line.getAmount() == null) {
            throw new IllegalArgumentException("Expense line amount is required");
        }
        if (line.getOriginalCurrency() == null) line.setOriginalCurrency(claim.getOriginalCurrency());

        BigDecimal rate = getExchangeRateToUsd(line.getOriginalCurrency());
        BigDecimal usdEquivalent = line.getAmount().multiply(rate).setScale(2, RoundingMode.HALF_UP);
        line.setUsdEquivalent(usdEquivalent);

        ExpenseLine savedLine = expenseLineRepository.save(line);
        complianceEngine.runComplianceCheck(savedLine);
        savedLine = expenseLineRepository.save(savedLine);

        recomputeClaimTotals(claim);
        return savedLine;
    }

    @Transactional
    public void recomputeClaimTotals(ExpenseClaim claim) {
        List<ExpenseLine> lines = expenseLineRepository.findByExpenseClaim_Id(claim.getId());
        BigDecimal totalClaimed = BigDecimal.ZERO;
        BigDecimal usdTotal = BigDecimal.ZERO;
        for (ExpenseLine l : lines) {
            if (l.getStatus() != com.journeyplus.expense.entity.ExpenseLineStatus.REJECTED) {
                totalClaimed = totalClaimed.add(l.getAmount());
                usdTotal = usdTotal.add(l.getUsdEquivalent());
            }
        }
        claim.setTotalAmount(totalClaimed);
        claim.setUsdEquivalent(usdTotal);
        calculateAdvanceAndNetReimbursable(claim);
        expenseClaimRepository.save(claim);
    }

    @Transactional
    @AuditAction(module = "EXPENSE", action = "UPDATE_EXPENSE_LINE")
    public ExpenseLine updateExpenseLine(Long claimId, Long lineId, ExpenseLine lineDetails) {
        log.info("Attempting to update expense line ID: {} for claim ID: {}", lineId, claimId);
        ExpenseClaim claim = expenseClaimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Expense claim not found"));

        if (claim.getStatus() != ExpenseStatus.DRAFT) {
            throw new IllegalStateException("Can only edit expense lines on DRAFT claims");
        }

        ExpenseLine line = expenseLineRepository.findById(lineId)
                .orElseThrow(() -> new IllegalArgumentException("Expense line not found"));

        if (!line.getExpenseClaim().getId().equals(claim.getId())) {
            throw new IllegalArgumentException("Expense line does not belong to the specified claim");
        }

        if (lineDetails.getExpenseDate() != null) line.setExpenseDate(lineDetails.getExpenseDate());
        if (lineDetails.getCategory() != null) line.setCategory(lineDetails.getCategory());
        if (lineDetails.getAmount() != null) line.setAmount(lineDetails.getAmount());
        if (lineDetails.getOriginalCurrency() != null) line.setOriginalCurrency(lineDetails.getOriginalCurrency());
        if (lineDetails.getMerchant() != null) line.setMerchant(lineDetails.getMerchant());
        if (lineDetails.getDescription() != null) line.setDescription(lineDetails.getDescription());
        if (lineDetails.getJustification() != null) line.setJustification(lineDetails.getJustification());
        if (lineDetails.getReceiptRef() != null) line.setReceiptRef(lineDetails.getReceiptRef());
        if (lineDetails.getReceiptPath() != null) line.setReceiptPath(lineDetails.getReceiptPath());

        BigDecimal rate = getExchangeRateToUsd(line.getOriginalCurrency());
        line.setUsdEquivalent(line.getAmount().multiply(rate).setScale(2, RoundingMode.HALF_UP));

        ExpenseLine savedLine = expenseLineRepository.save(line);
        complianceEngine.runComplianceCheck(savedLine);
        savedLine = expenseLineRepository.save(savedLine);

        recomputeClaimTotals(claim);
        return savedLine;
    }

    @Transactional
    @AuditAction(module = "EXPENSE", action = "DELETE_EXPENSE_LINE")
    public void deleteExpenseLine(Long claimId, Long lineId) {
        log.info("Attempting to delete expense line ID: {} for claim ID: {}", lineId, claimId);
        ExpenseClaim claim = expenseClaimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Expense claim not found"));

        if (claim.getStatus() != ExpenseStatus.DRAFT) {
            throw new IllegalStateException("Can only delete expense lines from DRAFT claims");
        }

        ExpenseLine line = expenseLineRepository.findById(lineId)
                .orElseThrow(() -> new IllegalArgumentException("Expense line not found"));

        if (!line.getExpenseClaim().getId().equals(claim.getId())) {
            throw new IllegalArgumentException("Expense line does not belong to the specified claim");
        }

        expenseLineRepository.delete(line);
        recomputeClaimTotals(claim);
    }

    @Transactional
    @AuditAction(module = "EXPENSE", action = "SUBMIT_EXPENSE_CLAIM")
    public ExpenseClaim submitExpenseClaim(Long claimId) {
        log.info("Attempting to submit expense claim ID: {}", claimId);
        ExpenseClaim claim = expenseClaimRepository.findById(claimId)
                .orElseThrow(() -> {
                    log.warn("Failed to submit: Expense claim ID {} not found", claimId);
                    return new IllegalArgumentException("Expense claim not found");
                });

        if (claim.getStatus() != ExpenseStatus.DRAFT) {
            log.warn("Failed to submit: Claim ID {} is in state {}, only DRAFT claims can be submitted", claimId, claim.getStatus());
            throw new IllegalStateException("Only DRAFT claims can be submitted");
        }

        List<ExpenseLine> lines = expenseLineRepository.findByExpenseClaim_Id(claimId);
        if (lines.isEmpty()) {
            throw new IllegalStateException("Cannot submit an empty claim. At least one expense line is required.");
        }

        // Check if mandatory receipts are uploaded
        for (ExpenseLine l : lines) {
            String rRef = l.getReceiptRef();
            String rPath = l.getReceiptPath();
            if ((rRef == null || rRef.isBlank()) && (rPath == null || rPath.isBlank())) {
                throw new IllegalStateException("Cannot submit claim: Expense line '" + l.getCategory() + "' is missing mandatory receipt.");
            }
        }

        claim.setStatus(ExpenseStatus.SUBMITTED);
        claim.setSubmittedDate(LocalDate.now());
        ExpenseClaim saved = expenseClaimRepository.save(claim);
        log.info("Expense claim ID: {} successfully submitted", claimId);

        // Notify Approving Manager
        if (claim.getTripRequest().getApprovingManager() != null) {
            log.info("Publishing status event for approving manager ID: {} for submitted claim ID: {}",
                    claim.getTripRequest().getApprovingManager().getId(), claimId);
            eventPublisher.publishEvent(new StatusChangeEvent(
                claim.getTripRequest().getApprovingManager().getId(),
                "Expense Claim Submitted",
                "An expense claim titled '" + claim.getClaimTitle() + "' has been submitted by " +
                claim.getEmployee().getUsername() + " and is awaiting your review.",
                claim.getEmployee() != null ? claim.getEmployee().getId() : null,
                claim.getEmployee() != null ? claim.getEmployee().getUsername() : null,
                com.journeyplus.notification.entity.NotificationCategory.ExpenseClaim
            ));
        } else {
            log.warn("No approving manager found for trip request associated with claim ID: {}", claimId);
        }

        return saved;
    }

    @Transactional
    @AuditAction(module = "EXPENSE", action = "APPROVE_REJECT_EXPENSE_CLAIM")
    public ExpenseClaim approveOrRejectExpenseClaim(Long claimId, ExpenseStatus newStatus, String comments, User manager) {
        log.info("Manager '{}' attempting to set status of claim ID: {} to {}", manager.getUsername(), claimId, newStatus);

        if (newStatus != ExpenseStatus.APPROVED && newStatus != ExpenseStatus.REJECTED) {
            throw new IllegalArgumentException("Target status must be APPROVED or REJECTED");
        }

        ExpenseClaim claim = expenseClaimRepository.findById(claimId)
                .orElseThrow(() -> {
                    log.warn("Failed to review: Expense claim ID {} not found", claimId);
                    return new IllegalArgumentException("Expense claim not found");
                });

        if (claim.getStatus() != ExpenseStatus.SUBMITTED) {
            log.warn("Failed to review: Claim ID {} is in state {}, only SUBMITTED claims can be approved or rejected", claimId, claim.getStatus());
            throw new IllegalStateException("Only SUBMITTED claims can be approved or rejected");
        }

        // Validate that the manager is the assigned approver or their active delegate
        com.journeyplus.trip.entity.TripRequest trip = claim.getTripRequest();
        if (trip != null) {
            boolean isAssignedApprover = trip.getApprover() != null && trip.getApprover().getId().equals(manager.getId());
            boolean isDelegateApprover = trip.getApprover() != null && trip.getApprover().getDelegateApprover() != null
                    && trip.getApprover().getDelegateApprover().getId().equals(manager.getId())
                    && trip.getApprover().isDelegationActive();

            if (!isAssignedApprover && !isDelegateApprover) {
                throw new org.springframework.security.access.AccessDeniedException("Only the assigned approving manager or their active delegate can approve or reject this expense claim");
            }
        }

        claim.setStatus(newStatus);
        claim.setManagerComments(comments);
        ExpenseClaim saved = expenseClaimRepository.save(claim);
        log.info("Claim ID: {} status successfully updated to {} by manager", claimId, newStatus);

        // Notify Employee (actor = manager)
        log.info("Publishing status event to employee ID: {} for reviewed claim ID: {}", claim.getEmployee().getId(), claimId);
        eventPublisher.publishEvent(new StatusChangeEvent(
            claim.getEmployee().getId(),
            "Expense Claim " + newStatus.name(),
            "Your expense claim '" + claim.getClaimTitle() + "' has been " + newStatus.name().toLowerCase() + ".",
            manager != null ? manager.getId() : null,
            manager != null ? manager.getUsername() : null,
            com.journeyplus.notification.entity.NotificationCategory.ExpenseClaim
        ));

        // After Manager approval, notify Finance users so they can process reimbursement
        if (newStatus == ExpenseStatus.APPROVED) {
            try {
                List<User> financeUsers = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == com.journeyplus.iam.entity.Role.FINANCE)
                    .toList();
                for (User fin : financeUsers) {
                    log.info("Publishing status event to Finance user ID: {} for approved claim ID: {}", fin.getId(), claimId);
                    eventPublisher.publishEvent(new StatusChangeEvent(
                        fin.getId(),
                        "Expense Claim Approved - Pending Reimbursement",
                        "Expense claim '" + claim.getClaimTitle() + "' by " + claim.getEmployee().getUsername() +
                        " has been approved by manager and is ready for reimbursement.",
                        manager != null ? manager.getId() : null,
                        manager != null ? manager.getUsername() : null,
                        com.journeyplus.notification.entity.NotificationCategory.ExpenseClaim
                    ));
                }
            } catch (Exception e) {
                log.warn("Failed to notify Finance users for approved claim ID: {}: {}", claimId, e.getMessage());
            }
        }

        return saved;
    }

    @Transactional
    @AuditAction(module = "EXPENSE", action = "PAY_REIMBURSEMENT")
    public ExpenseClaim payReimbursement(Long claimId, Reimbursement reimbursement) {
        log.info("Attempting to pay reimbursement for claim ID: {}, method: {}", claimId, reimbursement.getPaymentMethod());
        ExpenseClaim claim = expenseClaimRepository.findById(claimId)
                .orElseThrow(() -> {
                    log.warn("Failed to disburse payment: Expense claim ID {} not found", claimId);
                    return new IllegalArgumentException("Expense claim not found");
                });

        if (claim.getStatus() != ExpenseStatus.APPROVED) {
            log.warn("Failed to disburse payment: Claim ID {} is in state {}, only APPROVED claims can be PAID", claimId, claim.getStatus());
            throw new IllegalStateException("Reimbursements can only be disbursed for APPROVED claims");
        }

        calculateAdvanceAndNetReimbursable(claim);

        BigDecimal amountPaid = reimbursement.getAmount();
        if (amountPaid == null || amountPaid.compareTo(BigDecimal.ZERO) <= 0) {
            amountPaid = claim.getNetReimbursable();
        }

        reimbursement.setStatus("PROCESSED");

        if ("PROCESSED".equalsIgnoreCase(reimbursement.getStatus())) {
            if (amountPaid.compareTo(claim.getNetReimbursable()) >= 0) {
                claim.setStatus(ExpenseStatus.PAID);
            } else {
                claim.setStatus(ExpenseStatus.PARTIALLY_PAID);
            }
        }

        ExpenseClaim savedClaim = expenseClaimRepository.save(claim);

        reimbursement.setExpenseClaim(savedClaim);
        reimbursement.setRecipient(savedClaim.getEmployee());
        reimbursement.setAmount(amountPaid);
        reimbursement.setOriginalCurrency(savedClaim.getOriginalCurrency());
        BigDecimal rate = getExchangeRateToUsd(savedClaim.getOriginalCurrency());
        reimbursement.setUsdEquivalent(amountPaid.multiply(rate).setScale(2, RoundingMode.HALF_UP));
        reimbursement.setPaymentDate(LocalDate.now());
        if (reimbursement.getTransactionReference() == null) {
            reimbursement.setTransactionReference("TXN-" + System.currentTimeMillis());
        }
        reimbursementRepository.save(reimbursement);
        log.info("Reimbursement record created successfully for claim ID: {}, payment method: {}", claimId, reimbursement.getPaymentMethod());

        // Notify Employee
        log.info("Publishing status event to employee ID: {} for claim ID: {} with status {}", claim.getEmployee().getId(), claimId, claim.getStatus());
        eventPublisher.publishEvent(new StatusChangeEvent(
            claim.getEmployee().getId(),
            "Expense Claim " + claim.getStatus().name(),
            "Your expense claim '" + claim.getClaimTitle() + "' has been paid via " +
            reimbursement.getPaymentMethod() + ". Status: " + claim.getStatus().name().toLowerCase().replace("_", " "),
            null,
            null,
            com.journeyplus.notification.entity.NotificationCategory.ExpenseClaim
        ));

        return savedClaim;
    }

    public ExpenseClaim getExpenseClaim(Long id) {
        log.info("Retrieving expense claim ID: {}", id);
        return expenseClaimRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Lookup failed: Expense claim ID {} not found", id);
                    return new IllegalArgumentException("Expense claim not found");
                });
    }

    public List<ExpenseClaim> getClaimsByEmployee(Long employeeId) {
        log.info("Retrieving expense claims for employee ID: {}", employeeId);
        return expenseClaimRepository.findByEmployee_Id(employeeId);
    }

    public List<ExpenseLine> getLinesByClaim(Long claimId) {
        log.info("Retrieving expense lines for claim ID: {}", claimId);
        return expenseLineRepository.findByExpenseClaim_Id(claimId);
    }

    @Transactional
    @AuditAction(module = "EXPENSE", action = "SUBMIT_EXPENSE_LINE")
    public ExpenseLine submitExpenseLine(Long claimId, Long lineId) {
        log.info("Attempting to submit expense line ID: {} for claim ID: {}", lineId, claimId);
        ExpenseClaim claim = expenseClaimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Expense claim not found"));

        ExpenseLine line = expenseLineRepository.findById(lineId)
                .orElseThrow(() -> new IllegalArgumentException("Expense line not found"));

        if (line.getExpenseClaim() == null || !line.getExpenseClaim().getId().equals(claim.getId())) {
            log.warn("Line {} does not belong to claim {}", lineId, claimId);
            throw new IllegalArgumentException("Expense line does not belong to the provided claim");
        }

        if (claim.getStatus() != ExpenseStatus.DRAFT) {
            log.warn("Cannot submit line: Claim {} is in state {}", claimId, claim.getStatus());
            throw new IllegalStateException("Can only submit lines for claims in DRAFT state");
        }

        // Re-run policy compliance to ensure line is evaluated before final claim submission
        complianceEngine.runComplianceCheck(line);
        ExpenseLine saved = expenseLineRepository.save(line);
        log.info("Expense line ID: {} re-checked and saved", saved.getId());
        return saved;
    }

    @Transactional
    @AuditAction(module = "EXPENSE", action = "RECEIPT_UPLOADED")
    public ExpenseLine uploadOrReplaceReceipt(Long claimId, Long lineId, org.springframework.web.multipart.MultipartFile file, User user) throws java.io.IOException {
        log.info("Uploading receipt for line ID {} in claim ID {}", lineId, claimId);
        ExpenseClaim claim = getExpenseClaim(claimId);
        ExpenseLine line = expenseLineRepository.findById(lineId)
                .orElseThrow(() -> new IllegalArgumentException("Expense line not found: " + lineId));

        if (!line.getExpenseClaim().getId().equals(claim.getId())) {
            throw new IllegalArgumentException("Expense line does not belong to claim ID: " + claimId);
        }

        if (claim.getStatus() != ExpenseStatus.DRAFT) {
            throw new IllegalStateException("Receipts can only be uploaded for claims in DRAFT state");
        }

        com.journeyplus.document.entity.Document doc = documentService.saveEntityDocument(file, "RECEIPT", "EXPENSE_LINE", lineId, user.getId());
        String receiptRef = "/api/documents/" + doc.getId();
        line.setReceiptRef(receiptRef);
        line.setReceiptPath(doc.getPath());

        complianceEngine.runComplianceCheck(line);
        ExpenseLine savedLine = expenseLineRepository.save(line);
        recomputeClaimTotals(claim);

        log.info("Receipt uploaded successfully for line ID {}. Document ID: {}, ReceiptRef: {}", lineId, doc.getId(), receiptRef);
        return savedLine;
    }

    @Transactional
    @AuditAction(module = "EXPENSE", action = "RECEIPT_DELETED")
    public ExpenseLine deleteExpenseLineReceipt(Long claimId, Long lineId, User user) throws java.io.IOException {
        log.info("Deleting receipt for line ID {} in claim ID {}", lineId, claimId);
        ExpenseClaim claim = getExpenseClaim(claimId);
        ExpenseLine line = expenseLineRepository.findById(lineId)
                .orElseThrow(() -> new IllegalArgumentException("Expense line not found: " + lineId));

        if (!line.getExpenseClaim().getId().equals(claim.getId())) {
            throw new IllegalArgumentException("Expense line does not belong to claim ID: " + claimId);
        }

        if (claim.getStatus() != ExpenseStatus.DRAFT) {
            throw new IllegalStateException("Receipts can only be deleted from claims in DRAFT state");
        }

        documentService.deleteEntityDocument("EXPENSE_LINE", lineId);
        line.setReceiptRef(null);
        line.setReceiptPath(null);

        complianceEngine.runComplianceCheck(line);
        ExpenseLine savedLine = expenseLineRepository.save(line);
        recomputeClaimTotals(claim);

        log.info("Receipt deleted for line ID {}. Missing receipt policy check re-evaluated.", lineId);
        return savedLine;
    }
}
