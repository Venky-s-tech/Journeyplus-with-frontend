package com.journeyplus.traveldesk.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.journeyplus.iam.entity.User;
import com.journeyplus.traveldesk.service.TravelDeskService;
import com.journeyplus.trip.dto.VisaStatus;
import com.journeyplus.trip.entity.ItineraryLeg;
import com.journeyplus.trip.entity.TripRequest;
import com.journeyplus.trip.entity.VisaRequirement;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping({"/api/travel-desk", "/travel-desk"})
public class TravelDeskController {

    private final TravelDeskService travelDeskService;

    public TravelDeskController(TravelDeskService travelDeskService) {
        this.travelDeskService = travelDeskService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('TRAVEL_DESK','ADMIN')")
    @Operation(summary = "Get Travel Desk Dashboard Metrics")
    public ResponseEntity<Map<String, Object>> getTravelDeskDashboard() {
        return ResponseEntity.ok(travelDeskService.getTravelDeskDashboard());
    }

    @GetMapping("/queue")
    @PreAuthorize("hasAnyRole('TRAVEL_DESK','ADMIN')")
    @Operation(summary = "Get Travel Desk Queue of Manager-Approved Trips")
    public ResponseEntity<List<Map<String, Object>>> getTravelDeskQueue() {
        return ResponseEntity.ok(travelDeskService.getTravelDeskQueue());
    }

    @GetMapping("/itinerary/{tripId}")
    @PreAuthorize("hasAnyRole('TRAVEL_DESK','ADMIN','EMPLOYEE','APPROVING_MANAGER')")
    @Operation(summary = "Get Itinerary Legs for a Trip")
    public ResponseEntity<List<ItineraryLeg>> getItineraryLegs(@PathVariable Long tripId) {
        return ResponseEntity.ok(travelDeskService.getItineraryLegs(tripId));
    }

    @PostMapping("/itinerary")
    @PreAuthorize("hasAnyRole('TRAVEL_DESK','ADMIN')")
    @Operation(summary = "Create an Itinerary Leg for an Approved Trip")
    public ResponseEntity<ItineraryLeg> addItineraryLeg(
            @RequestParam Long tripId,
            @RequestBody ItineraryLeg leg) {
        return ResponseEntity.ok(travelDeskService.addItineraryLeg(tripId, leg));
    }

    @PutMapping("/itinerary/{legId}")
    @PreAuthorize("hasAnyRole('TRAVEL_DESK','ADMIN')")
    @Operation(summary = "Update an Itinerary Leg")
    public ResponseEntity<ItineraryLeg> updateItineraryLeg(
            @PathVariable Long legId,
            @RequestBody ItineraryLeg legDetails) {
        return ResponseEntity.ok(travelDeskService.updateItineraryLeg(legId, legDetails));
    }

    @DeleteMapping("/itinerary/{legId}")
    @PreAuthorize("hasAnyRole('TRAVEL_DESK','ADMIN')")
    @Operation(summary = "Delete an Itinerary Leg")
    public ResponseEntity<Void> deleteItineraryLeg(@PathVariable Long legId) {
        travelDeskService.deleteItineraryLeg(legId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/booking/confirm/{tripId}")
    @PreAuthorize("hasAnyRole('TRAVEL_DESK','ADMIN')")
    @Operation(summary = "Confirm Booking for a Trip")
    public ResponseEntity<TripRequest> confirmBooking(
            @PathVariable Long tripId,
            @RequestParam(required = false) String comments) {
        return ResponseEntity.ok(travelDeskService.confirmBooking(tripId, comments));
    }

    @PutMapping("/booking/confirm")
    @PreAuthorize("hasAnyRole('TRAVEL_DESK','ADMIN')")
    @Operation(summary = "Confirm Booking for a Trip (JSON body)")
    public ResponseEntity<TripRequest> confirmBookingBody(@RequestBody Map<String, Object> body) {
        Long tripId = Long.valueOf(body.get("tripId").toString());
        String comments = body.get("comments") != null ? body.get("comments").toString() : null;
        return ResponseEntity.ok(travelDeskService.confirmBooking(tripId, comments));
    }

    @GetMapping("/visa")
    @PreAuthorize("hasAnyRole('TRAVEL_DESK','ADMIN')")
    @Operation(summary = "Get Visa Processing Queue for International Trips")
    public ResponseEntity<List<VisaRequirement>> getVisaQueue() {
        return ResponseEntity.ok(travelDeskService.getVisaQueue());
    }

    @PostMapping("/visa")
    @PreAuthorize("hasAnyRole('TRAVEL_DESK','ADMIN')")
    @Operation(summary = "Add a Visa Requirement to an Approved Trip")
    public ResponseEntity<VisaRequirement> addVisaRequirement(
            @RequestParam Long tripId,
            @RequestBody VisaRequirement visa) {
        return ResponseEntity.ok(travelDeskService.addVisaRequirement(tripId, visa));
    }

    @PutMapping("/visa/status")
    @PreAuthorize("hasAnyRole('TRAVEL_DESK','ADMIN')")
    @Operation(summary = "Update Visa Requirement Status")
    public ResponseEntity<VisaRequirement> updateVisaStatus(@RequestBody Map<String, String> body) {
        Long visaId = Long.valueOf(body.get("visaId"));
        String statusStr = body.get("status");
        String notes = body.get("notes");
        VisaStatus status = VisaStatus.valueOf(statusStr.toUpperCase());
        return ResponseEntity.ok(travelDeskService.updateVisaStatus(visaId, status, notes));
    }

    @PostMapping("/reject/{tripId}")
    @PreAuthorize("hasAnyRole('TRAVEL_DESK','ADMIN')")
    @Operation(summary = "Reject trip request back from Travel Desk")
    public ResponseEntity<TripRequest> rejectTripBack(
            @PathVariable Long tripId,
            @RequestParam(required = false) String comments,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(travelDeskService.rejectTripBack(tripId, comments, user));
    }
}
