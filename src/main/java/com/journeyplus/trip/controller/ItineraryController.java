package com.journeyplus.trip.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.journeyplus.trip.dto.ItineraryLegResponse;
import com.journeyplus.trip.dto.VisaRequest;
import com.journeyplus.trip.dto.VisaRequirementResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/trips")
public class ItineraryController {

    @Autowired
    private com.journeyplus.trip.repository.ItineraryLegRepository itineraryLegRepository;

    @Autowired
    private com.journeyplus.trip.repository.VisaRequirementRepository visaRequirementRepository;

    @Autowired
    private com.journeyplus.trip.service.TripService tripService;

    @GetMapping("/{id}/legs")
    public ResponseEntity<java.util.List<ItineraryLegResponse>> getLegs(@PathVariable Long id, @AuthenticationPrincipal com.journeyplus.iam.entity.User user) {
        com.journeyplus.trip.entity.TripRequest trip = tripService.getTripRequest(id);
        if (trip == null) throw new IllegalArgumentException("Trip not found");
        // same access rules as trip details
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            for (org.springframework.security.core.GrantedAuthority a : auth.getAuthorities()) {
                String r = a.getAuthority();
                if (r != null && (r.endsWith("TRAVEL_ADMIN") || r.endsWith("COMPLIANCE_OFFICER") || r.endsWith("FINANCE_EXECUTIVE") || r.endsWith("TRAVEL_DESK_COORDINATOR"))) {
                    java.util.List<com.journeyplus.trip.entity.ItineraryLeg> legs = itineraryLegRepository.findByTripRequest_Id(id);
                    java.util.List<ItineraryLegResponse> dto = legs.stream().map(this::toLegResponse).collect(java.util.stream.Collectors.toList());
                    return ResponseEntity.ok(dto);
                }
            }
        }
        if (trip.getEmployee() != null && user != null && trip.getEmployee().getId().equals(user.getId())) {
            java.util.List<com.journeyplus.trip.entity.ItineraryLeg> legs = itineraryLegRepository.findByTripRequest_Id(id);
            java.util.List<ItineraryLegResponse> dto = legs.stream().map(this::toLegResponse).collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(dto);
        }
        if (trip.getApprovingManager() != null && user != null && trip.getApprovingManager().getId().equals(user.getId())) {
            java.util.List<com.journeyplus.trip.entity.ItineraryLeg> legs = itineraryLegRepository.findByTripRequest_Id(id);
            java.util.List<ItineraryLegResponse> dto = legs.stream().map(this::toLegResponse).collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(dto);
        }
        throw new org.springframework.security.access.AccessDeniedException("You are not authorized to view itinerary legs");
    }

    @GetMapping("/{id}/visas")
    public ResponseEntity<java.util.List<VisaRequirementResponse>> getVisas(@PathVariable Long id, @AuthenticationPrincipal com.journeyplus.iam.entity.User user) {
        com.journeyplus.trip.entity.TripRequest trip = tripService.getTripRequest(id);
        if (trip == null) throw new IllegalArgumentException("Trip not found");
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            for (org.springframework.security.core.GrantedAuthority a : auth.getAuthorities()) {
                String r = a.getAuthority();
                if (r != null && (r.endsWith("TRAVEL_ADMIN") || r.endsWith("COMPLIANCE_OFFICER") || r.endsWith("FINANCE_EXECUTIVE") || r.endsWith("TRAVEL_DESK_COORDINATOR"))) {
                    java.util.List<com.journeyplus.trip.entity.VisaRequirement> visas = visaRequirementRepository.findByTripRequest_Id(id);
                    java.util.List<VisaRequirementResponse> dto = visas.stream().map(this::toVisaResponse).collect(java.util.stream.Collectors.toList());
                    return ResponseEntity.ok(dto);
                }
            }
        }
            if (trip.getEmployee() != null && user != null && trip.getEmployee().getId().equals(user.getId())) {
            java.util.List<com.journeyplus.trip.entity.VisaRequirement> visas = visaRequirementRepository.findByTripRequest_Id(id);
            java.util.List<VisaRequirementResponse> dto = visas.stream().map(this::toVisaResponse).collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(dto);
        }
        if (trip.getApprovingManager() != null && user != null && trip.getApprovingManager().getId().equals(user.getId())) {
            java.util.List<com.journeyplus.trip.entity.VisaRequirement> visas = visaRequirementRepository.findByTripRequest_Id(id);
            java.util.List<VisaRequirementResponse> dto = visas.stream().map(this::toVisaResponse).collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(dto);
        }
        throw new org.springframework.security.access.AccessDeniedException("You are not authorized to view visa requirements");
    }

    @PostMapping("/{tripId}/legs/{legId}/book")
    @PreAuthorize("hasRole('TRAVEL_DESK_COORDINATOR')")
    public ResponseEntity<ItineraryLegResponse> bookLeg(
            @PathVariable Long tripId,
            @PathVariable Long legId,
            @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody com.journeyplus.trip.dto.BookingRequest bookingRequest
    ) {
        com.journeyplus.trip.entity.ItineraryLeg leg = itineraryLegRepository.findById(legId)
                .orElseThrow(() -> new IllegalArgumentException("Leg not found"));
        if (leg.getTripRequest() == null || !leg.getTripRequest().getId().equals(tripId)) {
            throw new IllegalArgumentException("Leg does not belong to the specified trip");
        }
        String bookingRef = bookingRequest.getBookingReference();
        String bookingStatus = bookingRequest.getBookingStatus() != null ? bookingRequest.getBookingStatus() : "BOOKED";
        if (bookingRef != null) leg.setBookingReference(bookingRef);
        if (bookingStatus != null) leg.setBookingStatus(bookingStatus);
        itineraryLegRepository.save(leg);
        return ResponseEntity.ok(toLegResponse(leg));
    }

    @PostMapping("/{tripId}/visas/{visaId}")
    @PreAuthorize("hasRole('TRAVEL_DESK_COORDINATOR')")
    public ResponseEntity<VisaRequirementResponse> updateVisa(
            @PathVariable Long tripId,
            @PathVariable Long visaId,
            @Valid @RequestBody VisaRequest visaRequest
    ) {
        com.journeyplus.trip.entity.VisaRequirement v = visaRequirementRepository.findById(visaId)
                .orElseThrow(() -> new IllegalArgumentException("Visa requirement not found"));
        if (v.getTripRequest() == null || !v.getTripRequest().getId().equals(tripId)) {
            throw new IllegalArgumentException("Visa requirement does not belong to the specified trip");
        }
        // Map DTO -> entity
        if (visaRequest.getDestinationCountry() != null) v.setDestinationCountry(visaRequest.getDestinationCountry());
        if (visaRequest.getStatus() != null) v.setStatus(visaRequest.getStatus().getValue());
        if (visaRequest.getNotes() != null) v.setNotes(visaRequest.getNotes());
        if (visaRequest.getRequiresVisa() != null) v.setRequiresVisa(visaRequest.getRequiresVisa());

        visaRequirementRepository.save(v);
        return ResponseEntity.ok(toVisaResponse(v));
    }

    private ItineraryLegResponse toLegResponse(com.journeyplus.trip.entity.ItineraryLeg leg) {
        if (leg == null) return null;
        ItineraryLegResponse r = new ItineraryLegResponse();
        r.setId(leg.getId());
        r.setDepartureCity(leg.getDepartureCity());
        r.setArrivalCity(leg.getArrivalCity());
        r.setTravelMode(leg.getTravelMode());
        r.setTravelDate(leg.getTravelDate());
        r.setEstimatedCost(leg.getEstimatedCost());
        r.setOriginalCurrency(leg.getOriginalCurrency());
        r.setUsdEquivalent(leg.getUsdEquivalent());
        r.setCarrierDetails(leg.getCarrierDetails());
        r.setBookingReference(leg.getBookingReference());
        r.setBookingStatus(leg.getBookingStatus());
        return r;
    }

    private VisaRequirementResponse toVisaResponse(com.journeyplus.trip.entity.VisaRequirement v) {
        if (v == null) return null;
        VisaRequirementResponse r = new VisaRequirementResponse();
        r.setId(v.getId());
        r.setDestinationCountry(v.getDestinationCountry());
        r.setRequiresVisa(v.isRequiresVisa());
        r.setStatus(v.getStatus());
        r.setNotes(v.getNotes());
        return r;
    }
}
