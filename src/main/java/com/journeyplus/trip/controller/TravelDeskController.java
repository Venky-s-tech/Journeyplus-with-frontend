package com.journeyplus.trip.controller;

import com.journeyplus.iam.entity.User;
import com.journeyplus.notification.entity.Notification;
import com.journeyplus.notification.repository.NotificationRepository;
import com.journeyplus.trip.dto.*;
import com.journeyplus.trip.entity.*;
import com.journeyplus.trip.repository.ItineraryLegRepository;
import com.journeyplus.trip.repository.TripRequestRepository;
import com.journeyplus.trip.repository.VisaRequirementRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/api/travel-desk", "/api/traveldesk"})
@PreAuthorize("isAuthenticated()")
public class TravelDeskController {

    private final TripRequestRepository tripRequestRepository;
    private final ItineraryLegRepository itineraryLegRepository;
    private final VisaRequirementRepository visaRequirementRepository;
    private final NotificationRepository notificationRepository;

    public TravelDeskController(
            TripRequestRepository tripRequestRepository,
            ItineraryLegRepository itineraryLegRepository,
            VisaRequirementRepository visaRequirementRepository,
            NotificationRepository notificationRepository) {
        this.tripRequestRepository = tripRequestRepository;
        this.itineraryLegRepository = itineraryLegRepository;
        this.visaRequirementRepository = visaRequirementRepository;
        this.notificationRepository = notificationRepository;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getTravelDeskDashboard() {
        List<TripRequest> allTrips = tripRequestRepository.findAll();
        List<TripRequest> approvedTrips = allTrips.stream()
                .filter(t -> t.getStatus() == TripStatus.APPROVED)
                .collect(Collectors.toList());

        long pendingBookings = approvedTrips.size();
        long completedBookings = allTrips.stream().filter(t -> t.getStatus() == TripStatus.COMPLETED).count();

        long waitingForItinerary = approvedTrips.stream()
                .filter(t -> itineraryLegRepository.findByTripRequest_Id(t.getId()).isEmpty())
                .count();

        long waitingForVisa = approvedTrips.stream()
                .filter(t -> "INTERNATIONAL".equalsIgnoreCase(t.getTravelType()) &&
                             visaRequirementRepository.findByTripRequest_Id(t.getId()).stream()
                                     .anyMatch(v -> v.getStatus() == VisaStatus.PENDING || v.getStatus() == VisaStatus.APPLIED))
                .count();

        LocalDate today = LocalDate.now();
        long todaysTravel = approvedTrips.stream()
                .filter(t -> t.getDepartureDate() != null && t.getDepartureDate().equals(today))
                .count();

        long upcomingTravel = approvedTrips.stream()
                .filter(t -> t.getDepartureDate() != null && t.getDepartureDate().isAfter(today))
                .count();

        long internationalTrips = allTrips.stream().filter(t -> "INTERNATIONAL".equalsIgnoreCase(t.getTravelType())).count();
        long domesticTrips = allTrips.stream().filter(t -> "DOMESTIC".equalsIgnoreCase(t.getTravelType())).count();

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("pendingBookings", pendingBookings);
        metrics.put("completedBookings", completedBookings);
        metrics.put("waitingForItinerary", waitingForItinerary);
        metrics.put("waitingForVisa", waitingForVisa);
        metrics.put("todaysTravel", todaysTravel);
        metrics.put("upcomingTravel", upcomingTravel);
        metrics.put("internationalTrips", internationalTrips);
        metrics.put("domesticTrips", domesticTrips);
        metrics.put("recentlyCompleted", completedBookings);

        return ResponseEntity.ok(metrics);
    }

    @GetMapping({"/bookings", "/pending"})
    public ResponseEntity<List<Map<String, Object>>> getPendingBookings() {
        List<TripRequest> approved = tripRequestRepository.findAll().stream()
                .filter(t -> t.getStatus() == TripStatus.APPROVED)
                .collect(Collectors.toList());

        List<Map<String, Object>> result = approved.stream().map(this::toMapQueueItem).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<Map<String, Object>>> getUpcomingBookings() {
        LocalDate today = LocalDate.now();
        List<TripRequest> upcoming = tripRequestRepository.findAll().stream()
                .filter(t -> t.getStatus() == TripStatus.APPROVED && t.getDepartureDate() != null && !t.getDepartureDate().isBefore(today))
                .collect(Collectors.toList());

        List<Map<String, Object>> result = upcoming.stream().map(this::toMapQueueItem).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{tripId}/itinerary")
    public ResponseEntity<List<ItineraryLegResponse>> getItineraryLegs(@PathVariable Long tripId) {
        List<ItineraryLeg> legs = itineraryLegRepository.findByTripRequest_Id(tripId);
        List<ItineraryLegResponse> out = legs.stream().map(ItineraryLegResponse::new).collect(Collectors.toList());
        return ResponseEntity.ok(out);
    }

    @PostMapping("/{tripId}/itinerary")
    @Transactional
    public ResponseEntity<ItineraryLegResponse> addItineraryLeg(
            @PathVariable Long tripId,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal User user) {

        TripRequest trip = tripRequestRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found: " + tripId));

        ItineraryLeg leg = new ItineraryLeg();
        leg.setTripRequest(trip);
        leg.setOrigin(body.get("origin") != null ? body.get("origin").toString() : (body.get("departureCity") != null ? body.get("departureCity").toString() : "Origin"));
        leg.setDestination(body.get("destination") != null ? body.get("destination").toString() : (body.get("arrivalCity") != null ? body.get("arrivalCity").toString() : trip.getDestination()));
        
        String legTypeStr = body.get("legType") != null ? body.get("legType").toString() : (body.get("travelMode") != null ? body.get("travelMode").toString() : "FLIGHT");
        try {
            leg.setLegType(LegType.valueOf(legTypeStr.toUpperCase()));
        } catch (Exception e) {
            leg.setLegType(LegType.FLIGHT);
        }

        if (body.get("travelDate") != null) {
            leg.setTravelDate(LocalDate.parse(body.get("travelDate").toString()));
        } else {
            leg.setTravelDate(trip.getDepartureDate() != null ? trip.getDepartureDate() : LocalDate.now());
        }

        BigDecimal costVal = BigDecimal.ZERO;
        if (body.get("cost") != null) {
            costVal = new BigDecimal(body.get("cost").toString());
        } else if (body.get("estimatedCost") != null) {
            costVal = new BigDecimal(body.get("estimatedCost").toString());
        }
        leg.setCost(costVal.compareTo(BigDecimal.ZERO) > 0 ? costVal : new BigDecimal("100.00"));
        leg.setOriginalCurrency(body.get("originalCurrency") != null ? body.get("originalCurrency").toString() : "USD");
        leg.setUsdEquivalent(leg.getCost());

        if (body.get("carrierDetails") != null) leg.setCarrierDetails(body.get("carrierDetails").toString());
        if (body.get("bookingRef") != null) leg.setBookingRef(body.get("bookingRef").toString());
        if (body.get("bookingReference") != null) leg.setBookingRef(body.get("bookingReference").toString());
        leg.setStatus(ItineraryStatus.CONFIRMED);

        ItineraryLeg saved = itineraryLegRepository.save(leg);

        if (trip.getEmployee() != null) {
            Notification n = new Notification(
                    trip.getEmployee(),
                    "Itinerary Leg Added",
                    "A new itinerary leg (" + leg.getLegType() + " to " + leg.getDestination() + ") has been added by Travel Desk.",
                    user != null ? user.getId() : null,
                    user != null ? user.getUsername() : "Travel Desk"
            );
            notificationRepository.save(n);
        }

        return ResponseEntity.ok(new ItineraryLegResponse(saved));
    }

    @PutMapping("/{tripId}/itinerary/{legId}")
    @Transactional
    public ResponseEntity<ItineraryLegResponse> updateItineraryLeg(
            @PathVariable Long tripId,
            @PathVariable Long legId,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal User user) {

        ItineraryLeg leg = itineraryLegRepository.findById(legId)
                .orElseThrow(() -> new IllegalArgumentException("Itinerary leg not found: " + legId));

        if (body.get("origin") != null) leg.setOrigin(body.get("origin").toString());
        if (body.get("destination") != null) leg.setDestination(body.get("destination").toString());
        if (body.get("carrierDetails") != null) leg.setCarrierDetails(body.get("carrierDetails").toString());
        if (body.get("bookingRef") != null) leg.setBookingRef(body.get("bookingRef").toString());
        if (body.get("bookingReference") != null) leg.setBookingRef(body.get("bookingReference").toString());
        if (body.get("status") != null) {
            try { leg.setStatus(ItineraryStatus.valueOf(body.get("status").toString().toUpperCase())); } catch (Exception ignored) {}
        }

        ItineraryLeg updated = itineraryLegRepository.save(leg);
        return ResponseEntity.ok(new ItineraryLegResponse(updated));
    }

    @DeleteMapping("/{tripId}/itinerary/{legId}")
    @Transactional
    public ResponseEntity<Map<String, Object>> deleteItineraryLeg(@PathVariable Long tripId, @PathVariable Long legId) {
        itineraryLegRepository.deleteById(legId);
        return ResponseEntity.ok(Map.of("message", "Itinerary leg deleted successfully", "id", legId));
    }

    @GetMapping("/{tripId}/visa")
    public ResponseEntity<List<VisaRequirementResponse>> getVisaRequirements(@PathVariable Long tripId) {
        List<VisaRequirement> visas = visaRequirementRepository.findByTripRequest_Id(tripId);
        List<VisaRequirementResponse> out = visas.stream().map(VisaRequirementResponse::new).collect(Collectors.toList());
        return ResponseEntity.ok(out);
    }

    @PostMapping("/{tripId}/visa")
    @Transactional
    public ResponseEntity<VisaRequirementResponse> addVisaRequirement(
            @PathVariable Long tripId,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal User user) {

        TripRequest trip = tripRequestRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found: " + tripId));

        VisaRequirement visa = new VisaRequirement();
        visa.setTripRequest(trip);
        visa.setCountry(body.get("country") != null ? body.get("country").toString() : (body.get("destinationCountry") != null ? body.get("destinationCountry").toString() : trip.getDestination()));
        visa.setVisaType(body.get("visaType") != null ? body.get("visaType").toString() : "BUSINESS");
        visa.setRequiresVisa(true);

        if (body.get("status") != null) {
            try { visa.setStatus(VisaStatus.valueOf(body.get("status").toString().toUpperCase())); } catch (Exception e) { visa.setStatus(VisaStatus.GRANTED); }
        } else {
            visa.setStatus(VisaStatus.GRANTED);
        }

        if (body.get("notes") != null) visa.setNotes(body.get("notes").toString());

        VisaRequirement saved = visaRequirementRepository.save(visa);

        if (trip.getEmployee() != null) {
            Notification n = new Notification(
                    trip.getEmployee(),
                    "Visa Status Updated",
                    "Visa status for " + visa.getCountry() + " updated to " + visa.getStatus() + ".",
                    user != null ? user.getId() : null,
                    user != null ? user.getUsername() : "Travel Desk"
            );
            notificationRepository.save(n);
        }

        return ResponseEntity.ok(new VisaRequirementResponse(saved));
    }

    @PutMapping("/{tripId}/visa/{visaId}")
    @Transactional
    public ResponseEntity<VisaRequirementResponse> updateVisaRequirement(
            @PathVariable Long tripId,
            @PathVariable Long visaId,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal User user) {

        VisaRequirement visa = visaRequirementRepository.findById(visaId)
                .orElseThrow(() -> new IllegalArgumentException("Visa requirement not found: " + visaId));

        if (body.get("country") != null) visa.setCountry(body.get("country").toString());
        if (body.get("visaType") != null) visa.setVisaType(body.get("visaType").toString());
        if (body.get("status") != null) {
            try { visa.setStatus(VisaStatus.valueOf(body.get("status").toString().toUpperCase())); } catch (Exception ignored) {}
        }
        if (body.get("notes") != null) visa.setNotes(body.get("notes").toString());

        VisaRequirement updated = visaRequirementRepository.save(visa);
        return ResponseEntity.ok(new VisaRequirementResponse(updated));
    }

    @PostMapping("/{tripId}/confirm")
    @Transactional
    public ResponseEntity<Map<String, Object>> confirmBooking(
            @PathVariable Long tripId,
            @RequestBody(required = false) Map<String, Object> body,
            @AuthenticationPrincipal User user) {

        TripRequest trip = tripRequestRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found: " + tripId));

        String comments = body != null && body.get("comments") != null ? body.get("comments").toString() : "Booking confirmed by Travel Desk.";
        trip.setComments(comments);
        trip.setBookingStatus("CONFIRMED");
        trip.setWorkflowStage("BOOKING_CONFIRMED");
        trip.setTravelDeskStatus("CONFIRMED");
        tripRequestRepository.save(trip);

        List<ItineraryLeg> legs = itineraryLegRepository.findByTripRequest_Id(tripId);
        for (ItineraryLeg leg : legs) {
            leg.setStatus(ItineraryStatus.CONFIRMED);
            itineraryLegRepository.save(leg);
        }

        if (trip.getEmployee() != null) {
            Notification n = new Notification(
                    trip.getEmployee(),
                    "Travel Booking Confirmed",
                    "Your travel booking for " + trip.getDestination() + " (Trip #" + trip.getId() + ") has been fully confirmed by Travel Desk.",
                    user != null ? user.getId() : null,
                    user != null ? user.getUsername() : "Travel Desk"
            );
            notificationRepository.save(n);
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "Booking confirmed successfully");
        resp.put("tripId", tripId);
        resp.put("bookingStatus", "CONFIRMED");
        resp.put("workflowStage", "BOOKING_CONFIRMED");
        resp.put("travelDeskStatus", "CONFIRMED");
        resp.put("comments", comments);
        return ResponseEntity.ok(resp);
    }

    private Map<String, Object> toMapQueueItem(TripRequest t) {
        Map<String, Object> m = new HashMap<>();
        m.put("tripId", t.getId());
        m.put("id", t.getId());
        m.put("employeeName", t.getEmployee() != null ? t.getEmployee().getName() : "Employee");
        m.put("department", t.getEmployee() != null && t.getEmployee().getDepartment() != null ? t.getEmployee().getDepartment() : "General");
        m.put("destination", t.getDestination());
        m.put("travelType", t.getTravelType());
        m.put("departureDate", t.getDepartureDate());
        m.put("returnDate", t.getReturnDate());
        m.put("purpose", t.getPurpose());
        m.put("estimatedCost", t.getEstimatedCost());
        m.put("status", t.getStatus().name());
        m.put("workflowStage", t.getWorkflowStage() != null ? t.getWorkflowStage() : "TRAVEL_DESK");
        m.put("travelDeskStatus", t.getTravelDeskStatus() != null ? t.getTravelDeskStatus() : "QUEUED");

        List<ItineraryLeg> legs = itineraryLegRepository.findByTripRequest_Id(t.getId());
        boolean hasLegs = !legs.isEmpty();
        boolean legsConfirmed = hasLegs && legs.stream().allMatch(l -> l.getStatus() == ItineraryStatus.CONFIRMED);
        String bStatus = legsConfirmed ? "CONFIRMED" : (hasLegs ? "IN_PROGRESS" : (t.getBookingStatus() != null ? t.getBookingStatus() : "PENDING_BOOKING"));
        m.put("bookingStatus", bStatus);

        List<VisaRequirement> visas = visaRequirementRepository.findByTripRequest_Id(t.getId());
        if ("INTERNATIONAL".equalsIgnoreCase(t.getTravelType())) {
            if (visas.isEmpty()) {
                m.put("visaStatus", "REQUIRED");
            } else {
                VisaStatus vs = visas.get(0).getStatus();
                m.put("visaStatus", vs.name());
            }
        } else {
            m.put("visaStatus", "NOT_REQUIRED");
        }

        return m;
    }
}
