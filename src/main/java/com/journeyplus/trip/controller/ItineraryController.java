package com.journeyplus.trip.controller;

import com.journeyplus.trip.dto.ItineraryLegResponse;
import com.journeyplus.trip.entity.ItineraryLeg;
import com.journeyplus.trip.entity.ItineraryStatus;
import com.journeyplus.trip.repository.ItineraryLegRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/itinerary")
public class ItineraryController {

    private final ItineraryLegRepository itineraryLegRepository;

    public ItineraryController(ItineraryLegRepository itineraryLegRepository) {
        this.itineraryLegRepository = itineraryLegRepository;
    }

    @GetMapping("/{legId}")
    public ResponseEntity<ItineraryLegResponse> getItineraryLeg(@PathVariable Long legId) {
        ItineraryLeg leg = itineraryLegRepository.findById(legId)
                .orElseThrow(() -> new IllegalArgumentException("Itinerary leg not found: " + legId));
        return ResponseEntity.ok(new ItineraryLegResponse(leg));
    }

    @PutMapping("/{legId}")
    public ResponseEntity<ItineraryLegResponse> updateItineraryLeg(@PathVariable Long legId, @RequestBody Map<String, Object> body) {
        ItineraryLeg leg = itineraryLegRepository.findById(legId)
                .orElseThrow(() -> new IllegalArgumentException("Itinerary leg not found: " + legId));
        if (body.get("origin") != null) leg.setOrigin(body.get("origin").toString());
        if (body.get("destination") != null) leg.setDestination(body.get("destination").toString());
        if (body.get("carrierDetails") != null) leg.setCarrierDetails(body.get("carrierDetails").toString());
        if (body.get("bookingRef") != null) leg.setBookingRef(body.get("bookingRef").toString());
        if (body.get("status") != null) {
            try { leg.setStatus(ItineraryStatus.valueOf(body.get("status").toString().toUpperCase())); } catch (Exception ignored) {}
        }
        ItineraryLeg saved = itineraryLegRepository.save(leg);
        return ResponseEntity.ok(new ItineraryLegResponse(saved));
    }

    @DeleteMapping("/{legId}")
    public ResponseEntity<Map<String, Object>> deleteItineraryLeg(@PathVariable Long legId) {
        itineraryLegRepository.deleteById(legId);
        return ResponseEntity.ok(Map.of("message", "Deleted itinerary leg successfully", "id", legId));
    }
}
