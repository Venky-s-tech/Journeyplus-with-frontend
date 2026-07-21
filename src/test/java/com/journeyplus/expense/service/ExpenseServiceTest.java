package com.journeyplus.expense.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.journeyplus.compliance.service.PolicyComplianceEngine;
import com.journeyplus.event.StatusChangeEvent;
import com.journeyplus.expense.entity.ExpenseClaim;
import com.journeyplus.expense.entity.ExpenseLine;
import com.journeyplus.expense.entity.ExpenseStatus;
import com.journeyplus.expense.entity.Reimbursement;
import com.journeyplus.expense.repository.ExpenseClaimRepository;
import com.journeyplus.expense.repository.ExpenseLineRepository;
import com.journeyplus.expense.repository.ReimbursementRepository;
import com.journeyplus.iam.entity.Role;
import com.journeyplus.iam.entity.User;
import com.journeyplus.trip.entity.TripRequest;
import com.journeyplus.trip.entity.TripStatus;
import com.journeyplus.trip.repository.TripRequestRepository;
import com.journeyplus.advance.repository.AdvanceRequestRepository;
import com.journeyplus.advance.repository.AdvanceSettlementRepository;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceTest {

    @Mock
    private ExpenseClaimRepository expenseClaimRepository;

    @Mock
    private ExpenseLineRepository expenseLineRepository;

    @Mock
    private ReimbursementRepository reimbursementRepository;

    @Mock
    private AdvanceRequestRepository advanceRequestRepository;

    @Mock
    private PolicyComplianceEngine complianceEngine;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private TripRequestRepository tripRequestRepository;

    @Mock
    private AdvanceSettlementRepository advanceSettlementRepository;

    @InjectMocks
    private ExpenseService expenseService;

    @Test
    public void createExpenseClaim_Success() {
        TripRequest trip = new TripRequest();
        trip.setId(100L);
        trip.setStatus(TripStatus.COMPLETED);

        ExpenseClaim claim = new ExpenseClaim();
        claim.setClaimTitle("Conference Trip");
        claim.setOriginalCurrency("USD");
        claim.setTripRequest(trip);

        when(tripRequestRepository.findById(100L)).thenReturn(Optional.of(trip));
        when(expenseClaimRepository.save(any(ExpenseClaim.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExpenseClaim created = expenseService.createExpenseClaim(claim);

        assertNotNull(created);
        assertEquals(ExpenseStatus.DRAFT, created.getStatus());
        assertEquals(BigDecimal.ZERO, created.getTotalAmount());
        assertEquals(BigDecimal.ZERO, created.getUsdEquivalent());
        verify(expenseClaimRepository, times(1)).save(claim);
    }

    @Test
    public void addExpenseLine_Success() {
        Long claimId = 1L;
        ExpenseClaim claim = new ExpenseClaim();
        claim.setId(claimId);
        claim.setStatus(ExpenseStatus.DRAFT);
        claim.setOriginalCurrency("USD");
        claim.setTotalAmount(BigDecimal.ZERO);
        claim.setUsdEquivalent(BigDecimal.ZERO);

        ExpenseLine line = new ExpenseLine();
        line.setAmount(new BigDecimal("100.00"));
        line.setOriginalCurrency("EUR"); // Conversion is 1.08 -> 108.00 USD

        when(expenseClaimRepository.findById(claimId)).thenReturn(Optional.of(claim));
        when(expenseLineRepository.findByExpenseClaim_Id(claimId)).thenReturn(Arrays.asList(line));
        
        // Mock save of the line first
        when(expenseLineRepository.save(any(ExpenseLine.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExpenseLine result = expenseService.addExpenseLine(claimId, line);

        assertNotNull(result);
        assertEquals("COMPLIANT", result.getPolicyComplianceStatus());
        assertEquals(new BigDecimal("108.00"), result.getUsdEquivalent());
        assertEquals(claim, result.getExpenseClaim());

        // Verify total amount updates on claim
        assertEquals(new BigDecimal("100.00"), claim.getTotalAmount());
        assertEquals(new BigDecimal("108.00"), claim.getUsdEquivalent());

        verify(complianceEngine, times(1)).runComplianceCheck(any(ExpenseLine.class));
        verify(expenseClaimRepository, times(1)).save(claim);
    }

    @Test
    public void addExpenseLine_ThrowsException_NonDraft() {
        Long claimId = 1L;
        ExpenseClaim claim = new ExpenseClaim();
        claim.setId(claimId);
        claim.setStatus(ExpenseStatus.SUBMITTED);

        ExpenseLine line = new ExpenseLine();

        when(expenseClaimRepository.findById(claimId)).thenReturn(Optional.of(claim));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            expenseService.addExpenseLine(claimId, line);
        });

        assertEquals("Can only add expense lines to DRAFT claims", exception.getMessage());
    }

    @Test
    public void addExpenseLine_ThrowsException_NoAmount() {
        Long claimId = 1L;
        ExpenseClaim claim = new ExpenseClaim();
        claim.setId(claimId);
        claim.setStatus(ExpenseStatus.DRAFT);

        ExpenseLine line = new ExpenseLine();
        line.setAmount(null);

        when(expenseClaimRepository.findById(claimId)).thenReturn(Optional.of(claim));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            expenseService.addExpenseLine(claimId, line);
        });

        assertEquals("Expense line amount is required", exception.getMessage());
    }

    @Test
    public void submitExpenseClaim_Success() {
        Long claimId = 1L;
        User employee = new User("empUser", "emp@journeyplus.com", "pass", Role.EMPLOYEE, "IT");
        employee.setId(10L);

        User manager = new User("mgrUser", "mgr@journeyplus.com", "pass", Role.APPROVING_MANAGER, "IT");
        manager.setId(20L);

        TripRequest trip = new TripRequest();
        trip.setApprovingManager(manager);

        ExpenseClaim claim = new ExpenseClaim();
        claim.setId(claimId);
        claim.setStatus(ExpenseStatus.DRAFT);
        claim.setClaimTitle("Conference");
        claim.setEmployee(employee);
        claim.setTripRequest(trip);

        ExpenseLine line = new ExpenseLine();
        line.setReceiptRef("uploads/receipt.pdf");

        when(expenseClaimRepository.findById(claimId)).thenReturn(Optional.of(claim));
        when(expenseLineRepository.findByExpenseClaim_Id(claimId)).thenReturn(Arrays.asList(line));
        when(expenseClaimRepository.save(any(ExpenseClaim.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExpenseClaim submitted = expenseService.submitExpenseClaim(claimId);

        assertNotNull(submitted);
        assertEquals(ExpenseStatus.SUBMITTED, submitted.getStatus());
        assertNotNull(submitted.getSubmittedDate());

        ArgumentCaptor<StatusChangeEvent> eventCaptor = ArgumentCaptor.forClass(StatusChangeEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        
        StatusChangeEvent event = eventCaptor.getValue();
        assertEquals(20L, event.getUserId());
        assertEquals("Expense Claim Submitted", event.getTitle());
        assertTrue(event.getMessage().contains("Conference"));
    }

    @Test
    public void submitExpenseClaim_ThrowsException_NonDraft() {
        Long claimId = 1L;
        ExpenseClaim claim = new ExpenseClaim();
        claim.setId(claimId);
        claim.setStatus(ExpenseStatus.APPROVED);

        when(expenseClaimRepository.findById(claimId)).thenReturn(Optional.of(claim));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            expenseService.submitExpenseClaim(claimId);
        });

        assertEquals("Only DRAFT claims can be submitted", exception.getMessage());
    }

    @Test
    public void approveOrRejectExpenseClaim_Success_Approve() {
        Long claimId = 1L;
        User employee = new User("empUser", "emp@journeyplus.com", "pass", Role.EMPLOYEE, "IT");
        employee.setId(10L);

        ExpenseClaim claim = new ExpenseClaim();
        claim.setId(claimId);
        claim.setStatus(ExpenseStatus.SUBMITTED);
        claim.setClaimTitle("Conference");
        claim.setEmployee(employee);

        User manager = new User("mgrUser", "mgr@journeyplus.com", "pass", Role.APPROVING_MANAGER, "IT");

        when(expenseClaimRepository.findById(claimId)).thenReturn(Optional.of(claim));
        when(expenseClaimRepository.save(any(ExpenseClaim.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExpenseClaim result = expenseService.approveOrRejectExpenseClaim(claimId, ExpenseStatus.APPROVED, "Approved!", manager);

        assertNotNull(result);
        assertEquals(ExpenseStatus.APPROVED, result.getStatus());
        assertEquals("Approved!", result.getManagerComments());

        ArgumentCaptor<StatusChangeEvent> eventCaptor = ArgumentCaptor.forClass(StatusChangeEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        
        StatusChangeEvent event = eventCaptor.getValue();
        assertEquals(10L, event.getUserId());
        assertEquals("Expense Claim APPROVED", event.getTitle());
    }

    @Test
    public void approveOrRejectExpenseClaim_ThrowsException_NonSubmitted() {
        Long claimId = 1L;
        ExpenseClaim claim = new ExpenseClaim();
        claim.setId(claimId);
        claim.setStatus(ExpenseStatus.DRAFT);

        when(expenseClaimRepository.findById(claimId)).thenReturn(Optional.of(claim));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            expenseService.approveOrRejectExpenseClaim(claimId, ExpenseStatus.APPROVED, "Approved!", new User());
        });

        assertEquals("Only SUBMITTED claims can be approved or rejected", exception.getMessage());
    }

    @Test
    public void approveOrRejectExpenseClaim_ThrowsException_InvalidStatus() {
        Long claimId = 1L;
        ExpenseClaim claim = new ExpenseClaim();
        claim.setId(claimId);
        claim.setStatus(ExpenseStatus.SUBMITTED);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            expenseService.approveOrRejectExpenseClaim(claimId, ExpenseStatus.DRAFT, "Draft?!", new User());
        });

        assertEquals("Target status must be APPROVED or REJECTED", exception.getMessage());
    }

    @Test
    public void payReimbursement_Success() {
        Long claimId = 1L;
        User employee = new User("empUser", "emp@journeyplus.com", "pass", Role.EMPLOYEE, "IT");
        employee.setId(10L);

        ExpenseClaim claim = new ExpenseClaim();
        claim.setId(claimId);
        claim.setStatus(ExpenseStatus.APPROVED);
        claim.setClaimTitle("Conference");
        claim.setEmployee(employee);
        claim.setTotalAmount(new BigDecimal("500.00"));
        claim.setOriginalCurrency("USD");
        claim.setUsdEquivalent(new BigDecimal("500.00"));

        Reimbursement reimbursement = new Reimbursement();
        reimbursement.setPaymentMethod("BANK_TRANSFER");
        reimbursement.setTransactionReference("REF12345");

        when(expenseClaimRepository.findById(claimId)).thenReturn(Optional.of(claim));
        when(expenseClaimRepository.save(any(ExpenseClaim.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExpenseClaim paidClaim = expenseService.payReimbursement(claimId, reimbursement);

        assertNotNull(paidClaim);
        assertEquals(ExpenseStatus.PAID, paidClaim.getStatus());

        verify(reimbursementRepository, times(1)).save(reimbursement);
        assertEquals(paidClaim, reimbursement.getExpenseClaim());
        assertEquals(employee, reimbursement.getRecipient());
        assertEquals(new BigDecimal("500.00"), reimbursement.getAmount());

        verify(eventPublisher, times(1)).publishEvent(any(StatusChangeEvent.class));
    }

    @Test
    public void getExpenseClaim_Success() {
        ExpenseClaim claim = new ExpenseClaim();
        claim.setId(1L);
        when(expenseClaimRepository.findById(1L)).thenReturn(Optional.of(claim));

        ExpenseClaim result = expenseService.getExpenseClaim(1L);
        assertEquals(claim, result);
    }

    @Test
    public void getExpenseClaim_ThrowsException_NotFound() {
        when(expenseClaimRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            expenseService.getExpenseClaim(99L);
        });

        assertEquals("Expense claim not found", exception.getMessage());
    }

    @Test
    public void getClaimsByEmployee() {
        ExpenseClaim claim = new ExpenseClaim();
        when(expenseClaimRepository.findByEmployee_Id(10L)).thenReturn(Arrays.asList(claim));

        List<ExpenseClaim> result = expenseService.getClaimsByEmployee(10L);
        assertEquals(1, result.size());
    }

    @Test
    public void getLinesByClaim() {
        ExpenseLine line = new ExpenseLine();
        when(expenseLineRepository.findByExpenseClaim_Id(1L)).thenReturn(Arrays.asList(line));

        List<ExpenseLine> result = expenseService.getLinesByClaim(1L);
        assertEquals(1, result.size());
    }

    @Test
    public void createExpenseClaim_WithAdvanceAdjustment_Success() {
        User employee = new User("empUser", "emp@journeyplus.com", "pass", Role.EMPLOYEE, "IT");
        employee.setId(10L);

        TripRequest trip = new TripRequest();
        trip.setId(100L);
        trip.setStatus(TripStatus.COMPLETED);

        ExpenseClaim claim = new ExpenseClaim(trip, employee, "Client Dinner", "USD");
        claim.setTotalAmount(new BigDecimal("500.00"));
        claim.setUsdEquivalent(new BigDecimal("500.00"));

        com.journeyplus.advance.entity.AdvanceRequest advance = new com.journeyplus.advance.entity.AdvanceRequest();
        advance.setRequestedAmount(new BigDecimal("200.00"));
        advance.setCurrency("USD");
        advance.setUsdEquivalent(new BigDecimal("200.00"));
        advance.setStatus(com.journeyplus.advance.entity.AdvanceStatus.DISBURSED);

        when(tripRequestRepository.findById(100L)).thenReturn(Optional.of(trip));
        when(advanceRequestRepository.findByTripRequest_Id(100L)).thenReturn(Arrays.asList(advance));
        when(expenseClaimRepository.save(any(ExpenseClaim.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExpenseClaim saved = expenseService.createExpenseClaim(claim, null);

        assertNotNull(saved);
        assertEquals(new BigDecimal("200.00"), saved.getAdvanceAdjusted());
        assertEquals(new BigDecimal("-200.00"), saved.getNetReimbursable());
    }

    @Test
    public void payReimbursement_WithAdvanceAdjustment_Success() {
        Long claimId = 1L;
        User employee = new User("empUser", "emp@journeyplus.com", "pass", Role.EMPLOYEE, "IT");
        employee.setId(10L);

        TripRequest trip = new TripRequest();
        trip.setId(100L);

        ExpenseClaim claim = new ExpenseClaim();
        claim.setId(claimId);
        claim.setStatus(ExpenseStatus.APPROVED);
        claim.setClaimTitle("Conference");
        claim.setEmployee(employee);
        claim.setTripRequest(trip);
        claim.setTotalAmount(new BigDecimal("500.00"));
        claim.setOriginalCurrency("USD");
        claim.setUsdEquivalent(new BigDecimal("500.00"));

        com.journeyplus.advance.entity.AdvanceRequest advance = new com.journeyplus.advance.entity.AdvanceRequest();
        advance.setRequestedAmount(new BigDecimal("150.00"));
        advance.setCurrency("USD");
        advance.setUsdEquivalent(new BigDecimal("150.00"));
        advance.setStatus(com.journeyplus.advance.entity.AdvanceStatus.DISBURSED);

        when(advanceRequestRepository.findByTripRequest_Id(100L)).thenReturn(Arrays.asList(advance));
        when(expenseClaimRepository.findById(claimId)).thenReturn(Optional.of(claim));
        when(expenseClaimRepository.save(any(ExpenseClaim.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reimbursement reimbursement = new Reimbursement();
        reimbursement.setPaymentMethod("BANK_TRANSFER");
        reimbursement.setTransactionReference("REF12345");

        ExpenseClaim paidClaim = expenseService.payReimbursement(claimId, reimbursement);

        assertNotNull(paidClaim);
        assertEquals(ExpenseStatus.PAID, paidClaim.getStatus());
        assertEquals(new BigDecimal("150.00"), paidClaim.getAdvanceAdjusted());
        assertEquals(new BigDecimal("350.00"), paidClaim.getNetReimbursable());

        verify(reimbursementRepository, times(1)).save(reimbursement);
        assertEquals(new BigDecimal("350.00"), reimbursement.getAmount());
    }
}
