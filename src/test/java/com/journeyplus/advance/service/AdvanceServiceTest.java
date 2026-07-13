package com.journeyplus.advance.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.journeyplus.advance.entity.AdvanceRequest;
import com.journeyplus.advance.entity.AdvanceSettlement;
import com.journeyplus.advance.entity.AdvanceStatus;
import com.journeyplus.advance.entity.SettlementStatus;
import com.journeyplus.advance.repository.AdvanceRequestRepository;
import com.journeyplus.advance.repository.AdvanceSettlementRepository;
import com.journeyplus.event.StatusChangeEvent;
import com.journeyplus.iam.entity.Role;
import com.journeyplus.iam.entity.User;
import com.journeyplus.trip.entity.TripRequest;
import com.journeyplus.trip.entity.TripStatus;
import com.journeyplus.trip.service.TripService;

@ExtendWith(MockitoExtension.class)
public class AdvanceServiceTest {

    @Mock
    private AdvanceRequestRepository advanceRequestRepository;

    @Mock
    private AdvanceSettlementRepository advanceSettlementRepository;

    @Mock
    private TripService tripService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AdvanceService advanceService;

    @Test
    public void createAdvanceRequest_Success() {
        User employee = new User("empUser", "emp@journeyplus.com", "pass", Role.EMPLOYEE, "IT");
        employee.setId(10L);

        TripRequest trip = new TripRequest();
        trip.setId(100L);
        trip.setStatus(TripStatus.APPROVED);
        trip.setEmployee(employee);

        AdvanceRequest request = new AdvanceRequest();
        request.setTripRequest(trip);
        request.setEmployee(employee);
        request.setRequestedAmount(new BigDecimal("1000.00"));
        request.setCurrency("USD");
        request.setPurposeDetails("Hotel & Food");

        when(tripService.getTripRequest(100L)).thenReturn(trip);
        when(advanceRequestRepository.save(any(AdvanceRequest.class))).thenAnswer(invocation -> {
            AdvanceRequest r = invocation.getArgument(0);
            r.setId(1L);
            return r;
        });

        AdvanceRequest saved = advanceService.createAdvanceRequest(request);

        assertNotNull(saved);
        assertEquals(1L, saved.getId());
        assertEquals(AdvanceStatus.REQUESTED, saved.getStatus());
    }

    @Test
    public void createAdvanceRequest_ThrowsException_TripNotApproved() {
        User employee = new User();
        employee.setId(10L);

        TripRequest trip = new TripRequest();
        trip.setId(100L);
        trip.setStatus(TripStatus.SUBMITTED); // NOT approved
        trip.setEmployee(employee);

        AdvanceRequest request = new AdvanceRequest();
        request.setTripRequest(trip);
        request.setEmployee(employee);

        when(tripService.getTripRequest(100L)).thenReturn(trip);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            advanceService.createAdvanceRequest(request);
        });

        assertEquals("Advance request can only be created against an APPROVED trip", exception.getMessage());
    }

    @Test
    public void createAdvanceRequest_ThrowsException_NotOwnTrip() {
        User employee = new User();
        employee.setId(10L);

        User anotherEmployee = new User();
        anotherEmployee.setId(11L);

        TripRequest trip = new TripRequest();
        trip.setId(100L);
        trip.setStatus(TripStatus.APPROVED);
        trip.setEmployee(anotherEmployee); // different employee

        AdvanceRequest request = new AdvanceRequest();
        request.setTripRequest(trip);
        request.setEmployee(employee);

        when(tripService.getTripRequest(100L)).thenReturn(trip);

        org.springframework.security.access.AccessDeniedException exception = assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            advanceService.createAdvanceRequest(request);
        });

        assertEquals("Employee can request advance only for their own trip", exception.getMessage());
    }

    @Test
    public void approveAdvanceRequest_Success() {
        Long advanceId = 1L;
        User employee = new User();
        employee.setId(10L);

        User manager = new User("mgrUser", "mgr@journeyplus.com", "pass", Role.APPROVING_MANAGER, "IT");
        manager.setId(20L);

        TripRequest trip = new TripRequest();
        trip.setApprover(manager);

        AdvanceRequest request = new AdvanceRequest();
        request.setId(advanceId);
        request.setStatus(AdvanceStatus.REQUESTED);
        request.setEmployee(employee);
        request.setTripRequest(trip);
        request.setRequestedAmount(new BigDecimal("1000.00"));
        request.setCurrency("USD");

        when(advanceRequestRepository.findById(advanceId)).thenReturn(Optional.of(request));
        when(advanceRequestRepository.save(any(AdvanceRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdvanceRequest result = advanceService.approveAdvanceRequest(advanceId, manager);

        assertNotNull(result);
        assertEquals(AdvanceStatus.APPROVED, result.getStatus());
        assertEquals(manager, result.getApprovedBy());
        verify(eventPublisher, times(1)).publishEvent(any(StatusChangeEvent.class));
    }

    @Test
    public void approveAdvanceRequest_ThrowsException_NotAssignedManager() {
        Long advanceId = 1L;

        User manager = new User();
        manager.setId(20L);

        User anotherManager = new User();
        anotherManager.setId(21L);

        TripRequest trip = new TripRequest();
        trip.setApprover(anotherManager); // different manager

        AdvanceRequest request = new AdvanceRequest();
        request.setId(advanceId);
        request.setStatus(AdvanceStatus.REQUESTED);
        request.setTripRequest(trip);

        when(advanceRequestRepository.findById(advanceId)).thenReturn(Optional.of(request));

        org.springframework.security.access.AccessDeniedException exception = assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            advanceService.approveAdvanceRequest(advanceId, manager);
        });

        assertEquals("Only the assigned approving manager or their active delegate can approve this advance", exception.getMessage());
    }

    @Test
    public void disburseAdvanceRequest_Success() {
        Long advanceId = 1L;
        User employee = new User();
        employee.setId(10L);

        AdvanceRequest request = new AdvanceRequest();
        request.setId(advanceId);
        request.setStatus(AdvanceStatus.APPROVED);
        request.setEmployee(employee);
        request.setRequestedAmount(new BigDecimal("1000.00"));
        request.setCurrency("USD");

        when(advanceRequestRepository.findById(advanceId)).thenReturn(Optional.of(request));
        when(advanceRequestRepository.save(any(AdvanceRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdvanceRequest result = advanceService.disburseAdvanceRequest(advanceId);

        assertNotNull(result);
        assertEquals(AdvanceStatus.DISBURSED, result.getStatus());
        assertNotNull(result.getDisbursementDate());
    }

    @Test
    public void forfeitAdvanceRequest_Success() {
        Long advanceId = 1L;
        User employee = new User();
        employee.setId(10L);

        AdvanceRequest request = new AdvanceRequest();
        request.setId(advanceId);
        request.setStatus(AdvanceStatus.DISBURSED);
        request.setEmployee(employee);
        request.setRequestedAmount(new BigDecimal("1000.00"));
        request.setCurrency("USD");

        when(advanceRequestRepository.findById(advanceId)).thenReturn(Optional.of(request));
        when(advanceRequestRepository.save(any(AdvanceRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdvanceRequest result = advanceService.forfeitAdvanceRequest(advanceId);

        assertNotNull(result);
        assertEquals(AdvanceStatus.FORFEITED, result.getStatus());
    }

    @Test
    public void forfeitAdvanceRequest_ThrowsException_IfApproved() {
        Long advanceId = 1L;
        AdvanceRequest request = new AdvanceRequest();
        request.setId(advanceId);
        request.setStatus(AdvanceStatus.APPROVED); // NOT disbursed

        when(advanceRequestRepository.findById(advanceId)).thenReturn(Optional.of(request));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            advanceService.forfeitAdvanceRequest(advanceId);
        });

        assertEquals("Only DISBURSED advance requests can be forfeited", exception.getMessage());
    }

    @Test
    public void addSettlement_Success_FullySettled() {
        Long advanceId = 1L;
        User employee = new User();
        employee.setId(10L);

        AdvanceRequest request = new AdvanceRequest();
        request.setId(advanceId);
        request.setStatus(AdvanceStatus.DISBURSED);
        request.setRequestedAmount(new BigDecimal("1000.00"));
        request.setEmployee(employee);
        request.setCurrency("USD");

        AdvanceSettlement settlement = new AdvanceSettlement();
        settlement.setAmountUtilised(new BigDecimal("800.00"));
        settlement.setAmountReturned(new BigDecimal("200.00")); // Sum is 1000.00 (Fully Settled)

        when(advanceRequestRepository.findById(advanceId)).thenReturn(Optional.of(request));
        when(advanceSettlementRepository.findByAdvanceRequest_Id(advanceId)).thenReturn(new java.util.ArrayList<>());
        when(advanceSettlementRepository.save(any(AdvanceSettlement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdvanceSettlement saved = advanceService.addSettlement(advanceId, settlement);

        assertNotNull(saved);
        assertEquals(SettlementStatus.SETTLED, saved.getStatus());
        assertEquals(AdvanceStatus.SETTLED, request.getStatus());
    }

    @Test
    public void addSettlement_Success_PartiallySettled() {
        Long advanceId = 1L;
        User employee = new User();
        employee.setId(10L);

        AdvanceRequest request = new AdvanceRequest();
        request.setId(advanceId);
        request.setStatus(AdvanceStatus.DISBURSED);
        request.setRequestedAmount(new BigDecimal("1000.00"));
        request.setEmployee(employee);
        request.setCurrency("USD");

        AdvanceSettlement settlement = new AdvanceSettlement();
        settlement.setAmountUtilised(new BigDecimal("700.00"));
        settlement.setAmountReturned(new BigDecimal("100.00")); // Sum is 800.00 (Partially Settled)

        when(advanceRequestRepository.findById(advanceId)).thenReturn(Optional.of(request));
        when(advanceSettlementRepository.findByAdvanceRequest_Id(advanceId)).thenReturn(new java.util.ArrayList<>());
        when(advanceSettlementRepository.save(any(AdvanceSettlement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdvanceSettlement saved = advanceService.addSettlement(advanceId, settlement);

        assertNotNull(saved);
        assertEquals(SettlementStatus.PARTIALLY_SETTLED, saved.getStatus());
        assertEquals(AdvanceStatus.DISBURSED, request.getStatus()); // remains disbursed
    }

    @Test
    public void addSettlement_Success_Excess() {
        Long advanceId = 1L;
        User employee = new User();
        employee.setId(10L);

        AdvanceRequest request = new AdvanceRequest();
        request.setId(advanceId);
        request.setStatus(AdvanceStatus.DISBURSED);
        request.setRequestedAmount(new BigDecimal("1000.00"));
        request.setEmployee(employee);
        request.setCurrency("USD");

        AdvanceSettlement settlement = new AdvanceSettlement();
        settlement.setAmountUtilised(new BigDecimal("1300.00")); // Exceeds (Excess)
        settlement.setAmountReturned(new BigDecimal("0.00"));

        when(advanceRequestRepository.findById(advanceId)).thenReturn(Optional.of(request));
        when(advanceSettlementRepository.findByAdvanceRequest_Id(advanceId)).thenReturn(new java.util.ArrayList<>());
        when(advanceSettlementRepository.save(any(AdvanceSettlement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdvanceSettlement saved = advanceService.addSettlement(advanceId, settlement);

        assertNotNull(saved);
        assertEquals(SettlementStatus.EXCESS, saved.getStatus());
        assertEquals(AdvanceStatus.SETTLED, request.getStatus()); // fully accounted for
    }

    @Test
    public void addSettlement_ThrowsException_InvalidSum() {
        Long advanceId = 1L;
        AdvanceRequest request = new AdvanceRequest();
        request.setId(advanceId);
        request.setStatus(AdvanceStatus.DISBURSED);
        request.setRequestedAmount(new BigDecimal("1000.00"));

        AdvanceSettlement settlement = new AdvanceSettlement();
        settlement.setAmountUtilised(new BigDecimal("900.00"));
        settlement.setAmountReturned(new BigDecimal("200.00")); // Sum is 1100.00 (Exceeds but utilised <= 1000.00, Invalid)

        when(advanceRequestRepository.findById(advanceId)).thenReturn(Optional.of(request));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            advanceService.addSettlement(advanceId, settlement);
        });

        assertEquals("Sum of Utilised and Returned amounts cannot exceed the Advance Amount when utilised is less than or equal to advance", exception.getMessage());
    }
}
