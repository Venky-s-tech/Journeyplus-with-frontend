package com.journeyplus.trip.service;

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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.journeyplus.event.StatusChangeEvent;
import com.journeyplus.iam.entity.Role;
import com.journeyplus.iam.entity.User;
import com.journeyplus.trip.dto.VisaRequest;
import com.journeyplus.trip.dto.VisaStatus;
import com.journeyplus.trip.entity.ItineraryLeg;
import com.journeyplus.trip.entity.LegType;
import com.journeyplus.trip.entity.TripRequest;
import com.journeyplus.trip.entity.TripStatus;
import com.journeyplus.trip.entity.VisaRequirement;
import com.journeyplus.trip.repository.ItineraryLegRepository;
import com.journeyplus.trip.repository.TripRequestRepository;
import com.journeyplus.trip.repository.VisaRequirementRepository;

@ExtendWith(MockitoExtension.class)
public class TripServiceTest {

    @Mock
    private TripRequestRepository tripRequestRepository;

    @Mock
    private ItineraryLegRepository itineraryLegRepository;

    @Mock
    private VisaRequirementRepository visaRequirementRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TripService tripService;

    @Test
    public void createTripRequest_Success() {
        User employee = new User("empUser", "emp@journeyplus.com", "pass", Role.EMPLOYEE, "IT");
        employee.setId(10L);

        TripRequest trip = new TripRequest(employee, "Client Meeting", "London", LocalDate.now(), LocalDate.now().plusDays(5));
        trip.setTravelType("INTERNATIONAL");
        trip.setEstimatedCost(new BigDecimal("500.00"));

        ItineraryLeg leg = new ItineraryLeg();
        leg.setOrigin("Paris");
        leg.setDestination("London");
        leg.setLegType(LegType.FLIGHT);
        leg.setTravelDate(LocalDate.now());
        leg.setCost(new BigDecimal("150.00"));
        leg.setOriginalCurrency("EUR");
        leg.setUsdEquivalent(new BigDecimal("165.00"));

        VisaRequirement visa = new VisaRequirement();
        visa.setCountry("UK");
        visa.setVisaType("Business");

        when(tripRequestRepository.save(any(TripRequest.class))).thenAnswer(invocation -> {
            TripRequest t = invocation.getArgument(0);
            t.setId(100L);
            return t;
        });

        TripRequest created = tripService.createTripRequest(trip, Arrays.asList(leg), Arrays.asList(visa));

        assertNotNull(created);
        assertEquals(100L, created.getId());
        assertEquals(TripStatus.DRAFT, created.getStatus());

        verify(itineraryLegRepository, times(1)).save(leg);
        verify(visaRequirementRepository, times(1)).save(visa);
        assertEquals(created, leg.getTripRequest());
        assertEquals(created, visa.getTripRequest());
    }

    @Test
    public void submitTripRequest_Success() {
        Long tripId = 100L;
        User employee = new User("empUser", "emp@journeyplus.com", "pass", Role.EMPLOYEE, "IT");
        employee.setId(10L);

        User manager = new User("mgrUser", "mgr@journeyplus.com", "pass", Role.APPROVING_MANAGER, "IT");
        manager.setId(20L);

        TripRequest trip = new TripRequest(employee, "Client Meeting", "London", LocalDate.now(), LocalDate.now().plusDays(5));
        trip.setId(tripId);
        trip.setStatus(TripStatus.DRAFT);
        trip.setApprover(manager);
        trip.setTravelType("DOMESTIC");
        trip.setEstimatedCost(new BigDecimal("200.00"));

        when(tripRequestRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(tripRequestRepository.save(any(TripRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TripRequest result = tripService.submitTripRequest(tripId);

        assertNotNull(result);
        assertEquals(TripStatus.SUBMITTED, result.getStatus());

        verify(eventPublisher, times(2)).publishEvent(any(StatusChangeEvent.class));
    }

    @Test
    public void submitTripRequest_ThrowsException_NonDraft() {
        Long tripId = 100L;
        TripRequest trip = new TripRequest();
        trip.setId(tripId);
        trip.setStatus(TripStatus.SUBMITTED);

        when(tripRequestRepository.findById(tripId)).thenReturn(Optional.of(trip));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            tripService.submitTripRequest(tripId);
        });

        assertEquals("Only DRAFT trip requests can be submitted", exception.getMessage());
    }

    @Test
    public void approveOrRejectTripRequest_Success_Approve() {
        Long tripId = 100L;
        User employee = new User("empUser", "emp@journeyplus.com", "pass", Role.EMPLOYEE, "IT");
        employee.setId(10L);

        User manager = new User("mgrUser", "mgr@journeyplus.com", "pass", Role.APPROVING_MANAGER, "IT");
        manager.setId(20L);

        TripRequest trip = new TripRequest();
        trip.setId(tripId);
        trip.setStatus(TripStatus.SUBMITTED);
        trip.setEmployee(employee);
        trip.setDestination("London");
        trip.setApprover(manager);

        when(tripRequestRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(tripRequestRepository.save(any(TripRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TripRequest result = tripService.approveOrRejectTripRequest(tripId, TripStatus.APPROVED, "Approved!", manager);

        assertNotNull(result);
        assertEquals(TripStatus.APPROVED, result.getStatus());
        assertEquals("Approved!", result.getComments());
        assertEquals(manager, result.getApprover());

        verify(eventPublisher, times(1)).publishEvent(any(StatusChangeEvent.class));
    }

    @Test
    public void approveOrRejectTripRequest_ThrowsException_NonSubmitted() {
        Long tripId = 100L;
        TripRequest trip = new TripRequest();
        trip.setId(tripId);
        trip.setStatus(TripStatus.APPROVED);

        when(tripRequestRepository.findById(tripId)).thenReturn(Optional.of(trip));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            tripService.approveOrRejectTripRequest(tripId, TripStatus.APPROVED, "Approved!", new User());
        });

        assertEquals("Only SUBMITTED trip requests can be approved or rejected", exception.getMessage());
    }

    @Test
    public void approveOrRejectTripRequest_ThrowsException_InvalidStatus() {
        Long tripId = 100L;
        TripRequest trip = new TripRequest();
        trip.setId(tripId);
        trip.setStatus(TripStatus.SUBMITTED);

        when(tripRequestRepository.findById(tripId)).thenReturn(Optional.of(trip));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            tripService.approveOrRejectTripRequest(tripId, TripStatus.COMPLETED, "Done!", new User());
        });

        assertEquals("Invalid status: Status must be APPROVED or REJECTED", exception.getMessage());
    }

    @Test
    public void completeOrCancelTripRequest_Success_Complete() {
        Long tripId = 100L;
        User employee = new User("empUser", "emp@journeyplus.com", "pass", Role.EMPLOYEE, "IT");
        employee.setId(10L);

        TripRequest trip = new TripRequest();
        trip.setId(tripId);
        trip.setStatus(TripStatus.APPROVED);
        trip.setEmployee(employee);

        when(tripRequestRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(tripRequestRepository.save(any(TripRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TripRequest result = tripService.completeOrCancelTripRequest(tripId, TripStatus.COMPLETED);

        assertNotNull(result);
        assertEquals(TripStatus.COMPLETED, result.getStatus());
        verify(eventPublisher, times(1)).publishEvent(any(StatusChangeEvent.class));
    }

    @Test
    public void completeOrCancelTripRequest_ThrowsException_CompleteNonApproved() {
        Long tripId = 100L;
        TripRequest trip = new TripRequest();
        trip.setId(tripId);
        trip.setStatus(TripStatus.SUBMITTED);

        when(tripRequestRepository.findById(tripId)).thenReturn(Optional.of(trip));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            tripService.completeOrCancelTripRequest(tripId, TripStatus.COMPLETED);
        });

        assertEquals("Only APPROVED trips can be marked as COMPLETED", exception.getMessage());
    }

    @Test
    public void completeOrCancelTripRequest_Success_Cancel() {
        Long tripId = 100L;
        User employee = new User("empUser", "emp@journeyplus.com", "pass", Role.EMPLOYEE, "IT");
        employee.setId(10L);

        TripRequest trip = new TripRequest();
        trip.setId(tripId);
        trip.setStatus(TripStatus.APPROVED);
        trip.setEmployee(employee);

        when(tripRequestRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(tripRequestRepository.save(any(TripRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TripRequest result = tripService.completeOrCancelTripRequest(tripId, TripStatus.CANCELLED);

        assertNotNull(result);
        assertEquals(TripStatus.CANCELLED, result.getStatus());
        verify(eventPublisher, times(1)).publishEvent(any(StatusChangeEvent.class));
    }

    @Test
    public void completeOrCancelTripRequest_ThrowsException_CancelCompleted() {
        Long tripId = 100L;
        TripRequest trip = new TripRequest();
        trip.setId(tripId);
        trip.setStatus(TripStatus.COMPLETED);

        when(tripRequestRepository.findById(tripId)).thenReturn(Optional.of(trip));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            tripService.completeOrCancelTripRequest(tripId, TripStatus.CANCELLED);
        });

        assertEquals("Cannot cancel a completed or already cancelled trip", exception.getMessage());
    }

    @Test
    public void updateVisaRequirement_Success() {
        Long tripId = 100L;
        Long visaId = 200L;

        TripRequest trip = new TripRequest();
        trip.setId(tripId);

        VisaRequirement visa = new VisaRequirement();
        visa.setId(visaId);
        visa.setTripRequest(trip);
        visa.setCountry("France");
        visa.setVisaType("Schengen");
        visa.setRequiresVisa(true);
        visa.setStatus(VisaStatus.PENDING);

        VisaRequest updateRequest = new VisaRequest();
        updateRequest.setCountry("Germany");
        updateRequest.setVisaType("Schengen");
        updateRequest.setRequiresVisa(true);
        updateRequest.setStatus(VisaStatus.GRANTED);
        updateRequest.setNotes("Approved via Embassy");

        when(visaRequirementRepository.findById(visaId)).thenReturn(Optional.of(visa));
        when(visaRequirementRepository.save(any(VisaRequirement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VisaRequirement result = tripService.updateVisaRequirement(tripId, visaId, updateRequest);

        assertNotNull(result);
        assertEquals("Germany", result.getCountry());
        assertEquals(VisaStatus.GRANTED, result.getStatus());
        assertEquals("Approved via Embassy", result.getNotes());
    }

    @Test
    public void updateVisaRequirement_ThrowsException_VisaNotBelongsToTrip() {
        Long tripId = 100L;
        Long visaId = 200L;

        TripRequest trip = new TripRequest();
        trip.setId(101L); // Different trip ID

        VisaRequirement visa = new VisaRequirement();
        visa.setId(visaId);
        visa.setTripRequest(trip);

        VisaRequest updateRequest = new VisaRequest();

        when(visaRequirementRepository.findById(visaId)).thenReturn(Optional.of(visa));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            tripService.updateVisaRequirement(tripId, visaId, updateRequest);
        });

        assertEquals("Visa requirement does not belong to the specified trip", exception.getMessage());
    }

    @Test
    public void getTripRequest_Success() {
        TripRequest trip = new TripRequest();
        trip.setId(100L);
        when(tripRequestRepository.findById(100L)).thenReturn(Optional.of(trip));

        TripRequest result = tripService.getTripRequest(100L);
        assertEquals(trip, result);
    }

    @Test
    public void getTripRequest_ThrowsException_NotFound() {
        when(tripRequestRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            tripService.getTripRequest(999L);
        });

        assertEquals("Trip request not found", exception.getMessage());
    }

    @Test
    public void getTripsByEmployee() {
        TripRequest trip = new TripRequest();
        when(tripRequestRepository.findByEmployee_Id(10L)).thenReturn(Arrays.asList(trip));

        List<TripRequest> result = tripService.getTripsByEmployee(10L);
        assertEquals(1, result.size());
    }

    @Test
    public void getTripsForManager() {
        TripRequest trip = new TripRequest();
        when(tripRequestRepository.findByApprover_Id(20L)).thenReturn(Arrays.asList(trip));

        List<TripRequest> result = tripService.getTripsForManager(20L);
        assertEquals(1, result.size());
    }
}
