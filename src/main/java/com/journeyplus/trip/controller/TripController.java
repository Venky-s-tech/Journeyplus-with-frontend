package com.journeyplus.trip.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.journeyplus.iam.entity.User;
import com.journeyplus.iam.repository.UserRepository;
import com.journeyplus.trip.dto.ItineraryLegInput;
import com.journeyplus.trip.dto.ItineraryLegResponse;
import com.journeyplus.trip.dto.SimpleUserDTO;
import com.journeyplus.trip.dto.TripCreationRequest;
import com.journeyplus.trip.dto.TripRequestInput;
import com.journeyplus.trip.dto.TripResponse;
import com.journeyplus.trip.dto.VisaRequirementInput;
import com.journeyplus.trip.dto.VisaRequirementResponse;
import com.journeyplus.trip.entity.TripRequest;
import com.journeyplus.trip.entity.TripStatus;
import com.journeyplus.trip.service.TripService;


// Disabled: Split into TripRequestController and ItineraryController
// @RestController
// @RequestMapping("/api/trips")
public class TripController {

    @Autowired
    private TripService tripService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.journeyplus.trip.repository.ItineraryLegRepository itineraryLegRepository;

    @Autowired
    private com.journeyplus.trip.repository.VisaRequirementRepository visaRequirementRepository;

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<TripRequest> createTripRequest(
            @RequestBody TripCreationRequest payload,
            @AuthenticationPrincipal User employee) {

        // Map TripRequestInput -> TripRequest entity
        TripRequestInput r = payload.getTripRequest();
        TripRequest tripEntity = new TripRequest();
        if (r != null) {
            tripEntity.setPurpose(r.getPurpose());
            tripEntity.setDestination(r.getDestination());
            tripEntity.setStartDate(r.getStartDate());
            tripEntity.setEndDate(r.getEndDate());
            tripEntity.setComments(r.getComments());
            if (r.getApprovingManagerUsername() != null && !r.getApprovingManagerUsername().isBlank()) {
                User mgr = userRepository.findByUsername(r.getApprovingManagerUsername())
                        .orElseThrow(() -> new IllegalArgumentException("Approving manager not found: " + r.getApprovingManagerUsername()));
                tripEntity.setApprovingManager(mgr);
            }
        }

        // Map legs
        List<com.journeyplus.trip.entity.ItineraryLeg> legs = null;
        if (payload.getLegs() != null) {
            legs = new ArrayList<>();
            for (ItineraryLegInput li : payload.getLegs()) {
                com.journeyplus.trip.entity.ItineraryLeg leg = new com.journeyplus.trip.entity.ItineraryLeg();
                leg.setDepartureCity(li.getDepartureCity());
                leg.setArrivalCity(li.getArrivalCity());
                leg.setTravelMode(li.getTravelMode());
                leg.setTravelDate(li.getTravelDate());
                if (li.getEstimatedCost() != null) leg.setEstimatedCost(li.getEstimatedCost());
                leg.setOriginalCurrency(li.getOriginalCurrency());
                if (li.getUsdEquivalent() != null) leg.setUsdEquivalent(li.getUsdEquivalent());
                leg.setCarrierDetails(li.getCarrierDetails());
                leg.setBookingReference(li.getBookingReference());
                legs.add(leg);
            }
        }

        // Map visas
        List<com.journeyplus.trip.entity.VisaRequirement> visas = null;
        if (payload.getVisas() != null) {
            visas = new ArrayList<>();
            for (VisaRequirementInput vi : payload.getVisas()) {
                com.journeyplus.trip.entity.VisaRequirement v = new com.journeyplus.trip.entity.VisaRequirement();
                v.setDestinationCountry(vi.getDestinationCountry());
                v.setRequiresVisa(vi.isRequiresVisa());
                v.setNotes(vi.getNotes());
                visas.add(v);
            }
        }

        // The controller/service will set employee from the authenticated principal
        tripEntity.setEmployee(employee);

        TripRequest trip = tripService.createTripRequest(
                tripEntity,
                legs,
                visas
        );

        return ResponseEntity.ok(trip);
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<TripRequest> submitTripRequest(@PathVariable Long id) {
        return ResponseEntity.ok(tripService.submitTripRequest(id));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('APPROVING_MANAGER')")
    public ResponseEntity<TripRequest> approveTripRequest(
            @PathVariable Long id,
            @RequestParam(required = false) String comments,
            @AuthenticationPrincipal User manager) {
        return ResponseEntity.ok(tripService.approveOrRejectTripRequest(id, TripStatus.APPROVED, comments, manager));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('APPROVING_MANAGER')")
    public ResponseEntity<TripRequest> rejectTripRequest(
            @PathVariable Long id,
            @RequestParam(required = false) String comments,
            @AuthenticationPrincipal User manager) {
        return ResponseEntity.ok(tripService.approveOrRejectTripRequest(id, TripStatus.REJECTED, comments, manager));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'TRAVEL_DESK_COORDINATOR')")
    public ResponseEntity<TripRequest> completeTripRequest(@PathVariable Long id) {
        return ResponseEntity.ok(tripService.completeOrCancelTripRequest(id, TripStatus.COMPLETED));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'TRAVEL_DESK_COORDINATOR')")
    public ResponseEntity<TripRequest> cancelTripRequest(@PathVariable Long id) {
        return ResponseEntity.ok(tripService.completeOrCancelTripRequest(id, TripStatus.CANCELLED));
    }

    @GetMapping("/my-trips")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<TripResponse>> getMyTrips(@AuthenticationPrincipal User employee) {
        List<TripRequest> trips = tripService.getTripsByEmployee(employee.getId());
        List<TripResponse> dto = trips.stream().map(this::toTripResponse).collect(Collectors.toList());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/pending-approvals")
    @PreAuthorize("hasRole('APPROVING_MANAGER')")
    public ResponseEntity<List<TripResponse>> getPendingApprovals(@AuthenticationPrincipal User manager) {
        List<TripRequest> trips = tripService.getTripsForManager(manager.getId());
        List<TripResponse> dto = trips.stream().map(this::toTripResponse).collect(Collectors.toList());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TripResponse> getTripRequest(@PathVariable Long id) {
        TripRequest trip = tripService.getTripRequest(id);
        return ResponseEntity.ok(toTripResponse(trip));
    }

    private SimpleUserDTO toSimpleUser(com.journeyplus.iam.entity.User u) {
        if (u == null) return null;
        SimpleUserDTO s = new SimpleUserDTO();
        try {
            // Handle possible Hibernate proxy without initializing it
            if (u instanceof org.hibernate.proxy.HibernateProxy) {
                org.hibernate.proxy.HibernateProxy proxy = (org.hibernate.proxy.HibernateProxy) u;
                Object idObj = proxy.getHibernateLazyInitializer().getIdentifier();
                if (idObj != null) {
                    s.setId(Long.valueOf(idObj.toString()));
                }
                if (proxy.getHibernateLazyInitializer().isUninitialized()) {
                    // Do not try to access other properties, return minimal DTO with id only
                    return s;
                }
                // If initialized, get the underlying implementation
                u = (com.journeyplus.iam.entity.User) proxy.getHibernateLazyInitializer().getImplementation();
            }

            s.setId(u.getId());
            s.setUsername(u.getUsername());
            s.setEmail(u.getEmail());
            s.setRole(u.getRole() != null ? u.getRole().name() : null);
        } catch (Exception e) {
            // Fallback: try to set id if possible, otherwise return partial DTO
            try {
                if (u.getId() != null) s.setId(u.getId());
            } catch (Exception ex) {
                // ignore
            }
        }
        return s;
    }

    private TripResponse toTripResponse(TripRequest t) {
        if (t == null) return null;
        TripResponse r = new TripResponse();
        r.setId(t.getId());
        r.setEmployee(toSimpleUser(t.getEmployee()));
        r.setPurpose(t.getPurpose());
        r.setDestination(t.getDestination());
        r.setStartDate(t.getStartDate());
        r.setEndDate(t.getEndDate());
        r.setStatus(t.getStatus() != null ? t.getStatus().name() : null);
        r.setComments(t.getComments());
        r.setApprovingManager(toSimpleUser(t.getApprovingManager()));
        r.setCreatedAt(t.getCreatedAt());
        r.setUpdatedAt(t.getUpdatedAt());
        // legs and visas are not stored as collections on TripRequest entity in this model,
        // so we omit them here to avoid lazy-init problems. They can be fetched separately if needed.
        return r;
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

    @GetMapping("/{id}/legs")
    public ResponseEntity<java.util.List<ItineraryLegResponse>> getLegs(@PathVariable Long id) {
        java.util.List<com.journeyplus.trip.entity.ItineraryLeg> legs = itineraryLegRepository.findByTripRequest_Id(id);
        java.util.List<ItineraryLegResponse> dto = legs.stream().map(this::toLegResponse).collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}/visas")
    public ResponseEntity<java.util.List<VisaRequirementResponse>> getVisas(@PathVariable Long id) {
        java.util.List<com.journeyplus.trip.entity.VisaRequirement> visas = visaRequirementRepository.findByTripRequest_Id(id);
        java.util.List<VisaRequirementResponse> dto = visas.stream().map(this::toVisaResponse).collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(dto);
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
            @RequestBody java.util.Map<String, Object> payload
    ) {
        com.journeyplus.trip.entity.VisaRequirement v = visaRequirementRepository.findById(visaId)
                .orElseThrow(() -> new IllegalArgumentException("Visa requirement not found"));
        if (v.getTripRequest() == null || !v.getTripRequest().getId().equals(tripId)) {
            throw new IllegalArgumentException("Visa requirement does not belong to the specified trip");
        }
        if (payload.containsKey("status")) v.setStatus(String.valueOf(payload.get("status")));
        if (payload.containsKey("notes")) v.setNotes(String.valueOf(payload.get("notes")));
        if (payload.containsKey("requiresVisa")) v.setRequiresVisa(Boolean.parseBoolean(String.valueOf(payload.get("requiresVisa"))));
        visaRequirementRepository.save(v);
        return ResponseEntity.ok(toVisaResponse(v));
    }
}
