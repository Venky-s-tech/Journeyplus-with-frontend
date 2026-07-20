package com.journeyplus.trip.controller;

import com.journeyplus.trip.dto.VisaRequirementResponse;
import com.journeyplus.trip.dto.VisaStatus;
import com.journeyplus.trip.entity.VisaRequirement;
import com.journeyplus.trip.service.TripService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/visa")
public class VisaController {

    private final TripService tripService;

    public VisaController(TripService tripService) {
        this.tripService = tripService;
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('TRAVEL_DESK', 'ADMIN')")
    public ResponseEntity<VisaRequirementResponse> updateVisaStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String statusStr = body.get("status");
        if (statusStr == null || statusStr.isBlank()) {
            throw new IllegalArgumentException("Status is required");
        }
        VisaStatus status = VisaStatus.valueOf(statusStr.toUpperCase());
        VisaRequirement existing = tripService.getVisaRequirement(id);
        existing.setStatus(status);
        if (body.containsKey("notes")) existing.setNotes(body.get("notes"));

        VisaRequirement saved = tripService.updateVisaRequirement(id, existing);
        return ResponseEntity.ok(new VisaRequirementResponse(saved));
    }
}
