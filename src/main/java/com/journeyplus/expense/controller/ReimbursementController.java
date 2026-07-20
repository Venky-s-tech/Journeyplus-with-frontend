package com.journeyplus.expense.controller;

import com.journeyplus.expense.entity.ExpenseClaim;
import com.journeyplus.expense.entity.Reimbursement;
import com.journeyplus.expense.service.ExpenseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reimbursements")
public class ReimbursementController {

    private final ExpenseService expenseService;

    public ReimbursementController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    @PreAuthorize("hasRole('FINANCE')")
    public ResponseEntity<ExpenseClaim> createReimbursement(@RequestBody Map<String, Object> body) {
        Long claimId = Long.valueOf(body.get("claimId").toString());
        Reimbursement reimbursement = new Reimbursement();
        if (body.get("reimbursementAmount") != null) {
            reimbursement.setAmount(new java.math.BigDecimal(body.get("reimbursementAmount").toString()));
        } else if (body.get("amount") != null) {
            reimbursement.setAmount(new java.math.BigDecimal(body.get("amount").toString()));
        }
        if (body.get("paymentMethod") != null) {
            reimbursement.setPaymentMethod(body.get("paymentMethod").toString());
        }
        if (body.get("paymentReference") != null) {
            reimbursement.setTransactionReference(body.get("paymentReference").toString());
        } else if (body.get("transactionReference") != null) {
            reimbursement.setTransactionReference(body.get("transactionReference").toString());
        }

        ExpenseClaim claim = expenseService.payReimbursement(claimId, reimbursement);
        return ResponseEntity.ok(claim);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('FINANCE')")
    public ResponseEntity<Map<String, String>> updateReimbursementStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String status = body.getOrDefault("status", "PROCESSED");
        return ResponseEntity.ok(Map.of("id", id.toString(), "status", status, "message", "Reimbursement status updated"));
    }
}
