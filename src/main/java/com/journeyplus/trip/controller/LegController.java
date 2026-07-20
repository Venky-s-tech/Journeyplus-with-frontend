package com.journeyplus.trip.controller;

import com.journeyplus.trip.dto.ItineraryLegInput;
import com.journeyplus.trip.dto.ItineraryLegResponse;
import com.journeyplus.trip.entity.ItineraryLeg;
import com.journeyplus.trip.service.TripService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/legs")
public class LegController {

    private final TripService tripService;

    public LegController(TripService tripService) {
        this.tripService = tripService;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TRAVEL_DESK', 'ADMIN')")
    public ResponseEntity<ItineraryLegResponse> updateLeg(
            @PathVariable Long id,
            @Valid @RequestBody ItineraryLegInput input) {
        ItineraryLeg legData = new ItineraryLeg();
        legData.setOrigin(input.getOrigin());
        legData.setDestination(input.getDestination());
        legData.setLegType(input.getLegType());
        legData.setTravelDate(input.getTravelDate());
        legData.setDepartureDateTime(input.getDepartureDateTime());
        legData.setArrivalDateTime(input.getArrivalDateTime());
        legData.setCarrierDetails(input.getCarrierDetails());
        legData.setCost(input.getCost());
        legData.setOriginalCurrency(input.getOriginalCurrency());
        legData.setBookingRef(input.getBookingRef());
        if (input.getUsdEquivalent() != null) legData.setUsdEquivalent(input.getUsdEquivalent());
        else legData.setUsdEquivalent(input.getCost());

        ItineraryLeg saved = tripService.updateItineraryLeg(id, legData);
        return ResponseEntity.ok(new ItineraryLegResponse(saved));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TRAVEL_DESK', 'ADMIN')")
    public ResponseEntity<Void> deleteLeg(@PathVariable Long id) {
        tripService.deleteItineraryLeg(id);
        return ResponseEntity.noContent().build();
    }
}
