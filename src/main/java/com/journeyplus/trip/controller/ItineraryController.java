package com.journeyplus.trip.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.journeyplus.iam.entity.User;
import com.journeyplus.trip.dto.ItineraryLegInput;
import com.journeyplus.trip.dto.ItineraryLegResponse;
import com.journeyplus.trip.dto.VisaRequest;
import com.journeyplus.trip.dto.VisaRequirementInput;
import com.journeyplus.trip.dto.VisaRequirementResponse;
import com.journeyplus.trip.entity.ItineraryLeg;
import com.journeyplus.trip.entity.TripRequest;
import com.journeyplus.trip.entity.VisaRequirement;
import com.journeyplus.trip.repository.ItineraryLegRepository;
import com.journeyplus.trip.repository.VisaRequirementRepository;
import com.journeyplus.trip.service.TripService;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ItineraryController {

    @Autowired
    private ItineraryLegRepository itineraryLegRepository;

    @Autowired
    private VisaRequirementRepository visaRequirementRepository;

    @Autowired
    private TripService tripService;

    // ==========================================
    // ITINERARY ENDPOINTS
    // ==========================================

    @PostMapping("/api/trips/{tripId}/itinerary")
    @PreAuthorize("hasAnyRole('TRAVEL_DESK', 'ADMIN')")
    public ResponseEntity<ItineraryLegResponse> addItineraryLeg(
            @PathVariable Long tripId,
            @Valid @RequestBody ItineraryLegInput input) {
        ItineraryLeg leg = new ItineraryLeg();
        leg.setOrigin(input.getOrigin());
        leg.setDestination(input.getDestination());
        leg.setLegType(input.getLegType());
        leg.setTravelDate(input.getTravelDate());
        leg.setDepartureDateTime(input.getDepartureDateTime());
        leg.setArrivalDateTime(input.getArrivalDateTime());
        leg.setCarrierDetails(input.getCarrierDetails());
        leg.setBookingRef(input.getBookingRef());
        if (input.getCost() != null) leg.setCost(input.getCost());
        leg.setOriginalCurrency(input.getOriginalCurrency());
        if (input.getUsdEquivalent() != null) leg.setUsdEquivalent(input.getUsdEquivalent());

        ItineraryLeg saved = tripService.addItineraryLeg(tripId, leg);
        return ResponseEntity.ok(toLegResponse(saved));
    }

    @GetMapping("/api/trips/{tripId}/itinerary")
    public ResponseEntity<List<ItineraryLegResponse>> getItinerary(@PathVariable Long tripId, @AuthenticationPrincipal User user) {
        TripRequest trip = tripService.getTripRequest(tripId);
        validateTripViewAuthorization(trip, user);

        List<ItineraryLeg> legs = itineraryLegRepository.findByTripRequest_Id(tripId);
        List<ItineraryLegResponse> dto = legs.stream().map(this::toLegResponse).collect(Collectors.toList());
        return ResponseEntity.ok(dto);
    }

    // Alias for backward compatibility
    @GetMapping("/api/trips/{id}/legs")
    public ResponseEntity<List<ItineraryLegResponse>> getLegs(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return getItinerary(id, user);
    }

    @GetMapping("/api/itinerary/{legId}")
    public ResponseEntity<ItineraryLegResponse> getItineraryLeg(@PathVariable Long legId, @AuthenticationPrincipal User user) {
        ItineraryLeg leg = tripService.getItineraryLeg(legId);
        validateTripViewAuthorization(leg.getTripRequest(), user);
        return ResponseEntity.ok(toLegResponse(leg));
    }

    @PutMapping("/api/itinerary/{legId}")
    @PreAuthorize("hasAnyRole('TRAVEL_DESK', 'ADMIN')")
    public ResponseEntity<ItineraryLegResponse> updateItineraryLeg(
            @PathVariable Long legId,
            @Valid @RequestBody ItineraryLegInput input) {
        ItineraryLeg updated = new ItineraryLeg();
        updated.setOrigin(input.getOrigin());
        updated.setDestination(input.getDestination());
        updated.setLegType(input.getLegType());
        updated.setTravelDate(input.getTravelDate());
        updated.setDepartureDateTime(input.getDepartureDateTime());
        updated.setArrivalDateTime(input.getArrivalDateTime());
        updated.setCarrierDetails(input.getCarrierDetails());
        updated.setBookingRef(input.getBookingRef());
        if (input.getCost() != null) updated.setCost(input.getCost());
        updated.setOriginalCurrency(input.getOriginalCurrency());
        if (input.getUsdEquivalent() != null) updated.setUsdEquivalent(input.getUsdEquivalent());

        ItineraryLeg saved = tripService.updateItineraryLeg(legId, updated);
        return ResponseEntity.ok(toLegResponse(saved));
    }

    @DeleteMapping("/api/itinerary/{legId}")
    @PreAuthorize("hasAnyRole('TRAVEL_DESK', 'ADMIN')")
    public ResponseEntity<Void> deleteItineraryLeg(@PathVariable Long legId) {
        tripService.deleteItineraryLeg(legId);
        return ResponseEntity.ok().build();
    }

    // ==========================================
    // VISA ENDPOINTS
    // ==========================================

    @PostMapping("/api/trips/{tripId}/visa")
    @PreAuthorize("hasAnyRole('TRAVEL_DESK', 'ADMIN')")
    public ResponseEntity<VisaRequirementResponse> addVisaRequirement(
            @PathVariable Long tripId,
            @Valid @RequestBody VisaRequirementInput input) {
        VisaRequirement visa = new VisaRequirement();
        visa.setCountry(input.getCountry());
        visa.setVisaType(input.getVisaType());
        visa.setRequiresVisa(input.isRequiresVisa());
        visa.setNotes(input.getNotes());
        visa.setApplicationDate(input.getApplicationDate());
        visa.setSubmittedDate(input.getSubmittedDate());
        if (input.getStatus() != null) visa.setStatus(input.getStatus());

        VisaRequirement saved = tripService.addVisaRequirement(tripId, visa);
        return ResponseEntity.ok(toVisaResponse(saved));
    }

    @GetMapping("/api/trips/{tripId}/visa")
    public ResponseEntity<List<VisaRequirementResponse>> getVisaRequirements(@PathVariable Long tripId, @AuthenticationPrincipal User user) {
        TripRequest trip = tripService.getTripRequest(tripId);
        validateTripViewAuthorization(trip, user);

        List<VisaRequirement> visas = visaRequirementRepository.findByTripRequest_Id(tripId);
        List<VisaRequirementResponse> dto = visas.stream().map(this::toVisaResponse).collect(Collectors.toList());
        return ResponseEntity.ok(dto);
    }

    // Alias for backward compatibility
    @GetMapping("/api/trips/{id}/visas")
    public ResponseEntity<List<VisaRequirementResponse>> getVisas(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return getVisaRequirements(id, user);
    }

    @GetMapping("/api/visa/{visaId}")
    public ResponseEntity<VisaRequirementResponse> getVisaRequirement(@PathVariable Long visaId, @AuthenticationPrincipal User user) {
        VisaRequirement visa = tripService.getVisaRequirement(visaId);
        validateTripViewAuthorization(visa.getTripRequest(), user);
        return ResponseEntity.ok(toVisaResponse(visa));
    }

    @PutMapping("/api/visa/{visaId}")
    @PreAuthorize("hasAnyRole('TRAVEL_DESK', 'ADMIN')")
    public ResponseEntity<VisaRequirementResponse> updateVisaRequirement(
            @PathVariable Long visaId,
            @Valid @RequestBody VisaRequirementInput input) {
        VisaRequirement updated = new VisaRequirement();
        updated.setCountry(input.getCountry());
        updated.setVisaType(input.getVisaType());
        updated.setRequiresVisa(input.isRequiresVisa());
        updated.setNotes(input.getNotes());
        updated.setApplicationDate(input.getApplicationDate());
        updated.setSubmittedDate(input.getSubmittedDate());
        if (input.getStatus() != null) updated.setStatus(input.getStatus());

        VisaRequirement saved = tripService.updateVisaRequirement(visaId, updated);
        return ResponseEntity.ok(toVisaResponse(saved));
    }

    // Alias for backward compatibility (Update visa)
    @PostMapping("/api/trips/{tripId}/visas/{visaId}")
    @PreAuthorize("hasAnyRole('TRAVEL_DESK', 'ADMIN')")
    public ResponseEntity<VisaRequirementResponse> updateVisa(
            @PathVariable Long tripId,
            @PathVariable Long visaId,
            @Valid @RequestBody VisaRequest visaRequest) {
        VisaRequirement saved = tripService.updateVisaRequirement(tripId, visaId, visaRequest);
        return ResponseEntity.ok(toVisaResponse(saved));
    }

    @PostMapping("/api/trips/{tripId}/legs/{legId}/book")
    @PreAuthorize("hasAnyRole('TRAVEL_DESK', 'ADMIN')")
    public ResponseEntity<ItineraryLegResponse> bookLeg(
            @PathVariable Long tripId,
            @PathVariable Long legId,
            @Valid @RequestBody com.journeyplus.trip.dto.BookingRequest bookingRequest) {
        ItineraryLeg leg = itineraryLegRepository.findById(legId)
                .orElseThrow(() -> new IllegalArgumentException("Leg not found"));
        if (leg.getTripRequest() == null || !leg.getTripRequest().getId().equals(tripId)) {
            throw new IllegalArgumentException("Leg does not belong to the specified trip");
        }
        String bookingRef = bookingRequest.getBookingReference();
        String bookingStatus = bookingRequest.getBookingStatus() != null ? bookingRequest.getBookingStatus() : "CONFIRMED";

        if (bookingRef != null) leg.setBookingRef(bookingRef);
        if (bookingStatus != null) {
            // Map bookingStatus to ItineraryStatus
            try {
                if ("CONFIRMED".equalsIgnoreCase(bookingStatus) || "BOOKED".equalsIgnoreCase(bookingStatus)) {
                    leg.setStatus(com.journeyplus.trip.entity.ItineraryStatus.CONFIRMED);
                } else if ("CANCELLED".equalsIgnoreCase(bookingStatus)) {
                    leg.setStatus(com.journeyplus.trip.entity.ItineraryStatus.CANCELLED);
                } else {
                    leg.setStatus(com.journeyplus.trip.entity.ItineraryStatus.valueOf(bookingStatus.toUpperCase()));
                }
            } catch (Exception e) {
                leg.setStatus(com.journeyplus.trip.entity.ItineraryStatus.CONFIRMED);
            }
        }
        itineraryLegRepository.save(leg);
        return ResponseEntity.ok(toLegResponse(leg));
    }

    // ==========================================
    // SECURITY AUTHORIZATION HELPER
    // ==========================================

    private void validateTripViewAuthorization(TripRequest trip, User user) {
        if (user == null) {
            throw new org.springframework.security.access.AccessDeniedException("User not authenticated");
        }

        // Admins, Compliance, Finance, Travel Desk can view all
        com.journeyplus.iam.entity.Role role = user.getRole();
        if (role == com.journeyplus.iam.entity.Role.ADMIN ||
            role == com.journeyplus.iam.entity.Role.COMPLIANCE ||
            role == com.journeyplus.iam.entity.Role.FINANCE ||
            role == com.journeyplus.iam.entity.Role.TRAVEL_DESK) {
            return;
        }

        // Owner can view
        if (trip.getEmployee() != null && trip.getEmployee().getId().equals(user.getId())) {
            return;
        }

        // Assigned Approving Manager can view
        if (trip.getApprover() != null && trip.getApprover().getId().equals(user.getId())) {
            return;
        }

        throw new org.springframework.security.access.AccessDeniedException("You are not authorized to view this trip details");
    }

    private ItineraryLegResponse toLegResponse(ItineraryLeg leg) {
        if (leg == null) return null;
        ItineraryLegResponse r = new ItineraryLegResponse();
        r.setId(leg.getId());
        r.setOrigin(leg.getOrigin());
        r.setDestination(leg.getDestination());
        r.setLegType(leg.getLegType() != null ? leg.getLegType().name() : null);
        r.setTravelDate(leg.getTravelDate());
        r.setDepartureDateTime(leg.getDepartureDateTime());
        r.setArrivalDateTime(leg.getArrivalDateTime());
        r.setCost(leg.getCost());
        r.setOriginalCurrency(leg.getOriginalCurrency());
        r.setUsdEquivalent(leg.getUsdEquivalent());
        r.setCarrierDetails(leg.getCarrierDetails());
        r.setBookingRef(leg.getBookingRef());
        r.setStatus(leg.getStatus() != null ? leg.getStatus().name() : null);
        return r;
    }

    private VisaRequirementResponse toVisaResponse(VisaRequirement v) {
        if (v == null) return null;
        VisaRequirementResponse r = new VisaRequirementResponse();
        r.setId(v.getId());
        r.setCountry(v.getCountry());
        r.setVisaType(v.getVisaType());
        r.setRequiresVisa(v.isRequiresVisa());
        r.setApplicationDate(v.getApplicationDate());
        r.setSubmittedDate(v.getSubmittedDate());
        r.setStatus(v.getStatus() != null ? v.getStatus().name() : null);
        r.setNotes(v.getNotes());
        r.setCreatedDate(v.getCreatedDate());
        r.setUpdatedDate(v.getUpdatedDate());
        return r;
    }
}
