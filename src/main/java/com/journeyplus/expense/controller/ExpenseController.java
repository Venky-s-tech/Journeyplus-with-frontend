package com.journeyplus.expense.controller;

import com.journeyplus.expense.entity.ExpenseClaim;
import com.journeyplus.expense.entity.ExpenseLine;
import com.journeyplus.expense.entity.ExpenseStatus;
import com.journeyplus.expense.entity.Reimbursement;
import com.journeyplus.expense.service.ExpenseService;
import com.journeyplus.iam.entity.User;
import com.journeyplus.iam.repository.UserRepository;
import com.journeyplus.trip.entity.TripRequest;
import com.journeyplus.trip.service.TripService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

@RestController
@RequestMapping({"/api/expenses", "/api/claims"})
public class ExpenseController {

    private final ExpenseService expenseService;
    private final TripService tripService;
    private final UserRepository userRepository;
    private final com.journeyplus.expense.repository.ExpenseLineRepository expenseLineRepository;
    private final com.journeyplus.document.service.DocumentService documentService;

    public ExpenseController(
            ExpenseService expenseService,
            TripService tripService,
            UserRepository userRepository,
            com.journeyplus.expense.repository.ExpenseLineRepository expenseLineRepository,
            com.journeyplus.document.service.DocumentService documentService) {
        this.expenseService = expenseService;
        this.tripService = tripService;
        this.userRepository = userRepository;
        this.expenseLineRepository = expenseLineRepository;
        this.documentService = documentService;
    }

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Create an expense claim", description = "Create an expense claim for a trip. Provide 'tripRequestId' as request parameter and claim fields in the JSON body.")
    public ResponseEntity<ExpenseClaim> createExpenseClaim(
            @RequestParam Long tripRequestId,
            @RequestBody com.journeyplus.expense.dto.ExpenseClaimRequest claimRequest,
            @AuthenticationPrincipal User employee) {

        TripRequest trip = tripService.getTripRequest(tripRequestId);
        if (!trip.getEmployee().getId().equals(employee.getId())) {
            throw new IllegalArgumentException("Trip request does not belong to the authenticated employee");
        }

        ExpenseClaim claim = new ExpenseClaim();
        claim.setTripRequest(trip);
        claim.setEmployee(employee);
        claim.setClaimTitle(claimRequest.getClaimTitle());
        claim.setSubmittedDate(claimRequest.getSubmittedDate());
        if (claimRequest.getTotalAmount() != null) claim.setTotalAmount(new java.math.BigDecimal(claimRequest.getTotalAmount().toString()));
        claim.setOriginalCurrency(claimRequest.getOriginalCurrency());

        // Approver: use an explicitly-provided approverUsername if given,
        // otherwise default to the trip's own approver (the claim is filed
        // against this trip, so its approving manager is already known and
        // shouldn't need to be re-entered/re-resolved by the employee).
        if (claimRequest.getApproverUsername() != null && !claimRequest.getApproverUsername().isBlank()) {
            User mgr = userRepository.findByUsername(claimRequest.getApproverUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Approving manager not found: " + claimRequest.getApproverUsername()));
            claim.setApprover(mgr);
        } else if (trip.getApprover() != null) {
            claim.setApprover(trip.getApprover());
        }

        return ResponseEntity.ok(expenseService.createExpenseClaim(claim, claimRequest.getExpenseLines()));
    }

    @PostMapping("/{claimId}/lines")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Add an expense line", description = "Add an expense line to an existing claim. Provide expense line JSON in the request body.")
    public ResponseEntity<ExpenseLine> addExpenseLine(
            @PathVariable Long claimId,
            @RequestBody com.journeyplus.expense.dto.ExpenseLineRequest lineRequest) {
        ExpenseLine line = new ExpenseLine();
        line.setExpenseDate(lineRequest.getExpenseDate());
        line.setCategory(lineRequest.getCategory());
        line.setAmount(lineRequest.getAmount());
        line.setOriginalCurrency(lineRequest.getOriginalCurrency());
        line.setMerchant(lineRequest.getMerchant());
        line.setDescription(lineRequest.getDescription());
        line.setJustification(lineRequest.getJustification());
        line.setReceiptRef(lineRequest.getReceiptRef());
        line.setReceiptPath(lineRequest.getReceiptPath());

        return ResponseEntity.ok(expenseService.addExpenseLine(claimId, line));
    }

    @PutMapping("/{claimId}/lines/{lineId}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Update an expense line", description = "Update an expense line details in a draft claim.")
    public ResponseEntity<ExpenseLine> updateExpenseLine(
            @PathVariable Long claimId,
            @PathVariable Long lineId,
            @RequestBody com.journeyplus.expense.dto.ExpenseLineRequest lineRequest) {
        ExpenseLine lineDetails = new ExpenseLine();
        lineDetails.setExpenseDate(lineRequest.getExpenseDate());
        lineDetails.setCategory(lineRequest.getCategory());
        lineDetails.setAmount(lineRequest.getAmount());
        lineDetails.setOriginalCurrency(lineRequest.getOriginalCurrency());
        lineDetails.setMerchant(lineRequest.getMerchant());
        lineDetails.setDescription(lineRequest.getDescription());
        lineDetails.setJustification(lineRequest.getJustification());
        lineDetails.setReceiptRef(lineRequest.getReceiptRef());
        lineDetails.setReceiptPath(lineRequest.getReceiptPath());

        return ResponseEntity.ok(expenseService.updateExpenseLine(claimId, lineId, lineDetails));
    }

    @DeleteMapping("/{claimId}/lines/{lineId}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Delete an expense line", description = "Delete an expense line from a draft claim.")
    public ResponseEntity<Void> deleteExpenseLine(
            @PathVariable Long claimId,
            @PathVariable Long lineId) {
        expenseService.deleteExpenseLine(claimId, lineId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/{claimId}/lines/{lineId}/receipt", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Upload or replace expense line receipt", description = "Upload a persistent receipt document (PDF, JPG, PNG) for an expense line.")
    public ResponseEntity<ExpenseLine> uploadExpenseLineReceipt(
            @PathVariable Long claimId,
            @PathVariable Long lineId,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @AuthenticationPrincipal User employee) throws java.io.IOException {
        return ResponseEntity.ok(expenseService.uploadOrReplaceReceipt(claimId, lineId, file, employee));
    }

    @GetMapping("/{claimId}/lines/{lineId}/receipt")
    @Operation(summary = "Download/stream expense line receipt", description = "Retrieve and stream the physical receipt document for an expense line.")
    public ResponseEntity<byte[]> getExpenseLineReceipt(
            @PathVariable Long claimId,
            @PathVariable Long lineId,
            @AuthenticationPrincipal User user) throws java.io.IOException {
        ExpenseLine line = expenseLineRepository.findById(lineId)
                .orElseThrow(() -> new IllegalArgumentException("Expense line not found: " + lineId));

        java.util.Optional<com.journeyplus.document.entity.Document> docOpt = documentService.findActiveDocumentForEntity("EXPENSE_LINE", lineId);
        if (docOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        com.journeyplus.document.entity.Document doc = docOpt.get();
        byte[] data = documentService.loadContent(doc);

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getOriginalFileName() + "\"")
                .contentType(org.springframework.http.MediaType.parseMediaType(doc.getContentType() != null ? doc.getContentType() : "application/pdf"))
                .body(data);
    }

    @DeleteMapping("/{claimId}/lines/{lineId}/receipt")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Delete expense line receipt", description = "Remove receipt reference and mark document as DELETED.")
    public ResponseEntity<ExpenseLine> deleteExpenseLineReceipt(
            @PathVariable Long claimId,
            @PathVariable Long lineId,
            @AuthenticationPrincipal User employee) throws java.io.IOException {
        return ResponseEntity.ok(expenseService.deleteExpenseLineReceipt(claimId, lineId, employee));
    }

    @PostMapping("/{claimId}/lines/{lineId}/submit")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Submit an expense line for compliance re-check", description = "Trigger compliance re-check and persist the expense line. This does not submit the entire claim.")
    public ResponseEntity<ExpenseLine> submitExpenseLine(
            @PathVariable Long claimId,
            @PathVariable Long lineId) {
        return ResponseEntity.ok(expenseService.submitExpenseLine(claimId, lineId));
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

    @PatchMapping("/{claimId}/status")
    public ResponseEntity<ExpenseClaim> updateClaimStatusPatch(
            @PathVariable Long claimId,
            @RequestBody java.util.Map<String, String> body,
            @AuthenticationPrincipal User user) {
        String statusStr = body.get("status");
        String remarks = body.get("remarks");
        if (statusStr == null || statusStr.isBlank()) throw new IllegalArgumentException("Status is required");
        ExpenseStatus status = ExpenseStatus.valueOf(statusStr.toUpperCase());
        if (status == ExpenseStatus.SUBMITTED) {
            return ResponseEntity.ok(expenseService.submitExpenseClaim(claimId));
        } else if (status == ExpenseStatus.APPROVED || status == ExpenseStatus.REJECTED) {
            return ResponseEntity.ok(expenseService.approveOrRejectExpenseClaim(claimId, status, remarks, user));
        }
        return ResponseEntity.ok(expenseService.getExpenseClaim(claimId));
    }

    @PatchMapping("/{claimId}/lines/{lineId}/status")
    public ResponseEntity<com.journeyplus.expense.entity.ExpenseLine> updateLineStatusPatch(
            @PathVariable Long claimId,
            @PathVariable Long lineId,
            @RequestBody java.util.Map<String, String> body) {
        String statusStr = body.get("status");
        if (statusStr != null && !statusStr.isBlank()) {
            com.journeyplus.expense.entity.ExpenseLineStatus status = com.journeyplus.expense.entity.ExpenseLineStatus.valueOf(statusStr.toUpperCase());
            com.journeyplus.expense.entity.ExpenseLine line = expenseLineRepository.findById(lineId)
                    .orElseThrow(() -> new IllegalArgumentException("Expense line not found: " + lineId));
            line.setStatus(status);
            return ResponseEntity.ok(expenseLineRepository.save(line));
        }
        return ResponseEntity.ok(expenseService.submitExpenseLine(claimId, lineId));
    }

    @PostMapping("/{claimId}/reimburse")
    @PreAuthorize("hasRole('FINANCE')")
    @Operation(summary = "Disburse reimbursement", description = "Create a reimbursement record for a claim. Provide reimbursement JSON in the request body.")
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
                if (r != null && (r.endsWith("ADMIN") || r.endsWith("COMPLIANCE") || r.endsWith("FINANCE"))) {
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
                if (r != null && (r.endsWith("ADMIN") || r.endsWith("COMPLIANCE") || r.endsWith("FINANCE"))) {
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
