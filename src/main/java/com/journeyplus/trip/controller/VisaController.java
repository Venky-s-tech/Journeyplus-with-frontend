package com.journeyplus.trip.controller;

import com.journeyplus.trip.dto.VisaRequirementResponse;
import com.journeyplus.trip.dto.VisaStatus;
import com.journeyplus.trip.entity.VisaRequirement;
import com.journeyplus.trip.repository.VisaRequirementRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/visa")
public class VisaController {

    private final VisaRequirementRepository visaRequirementRepository;

    public VisaController(VisaRequirementRepository visaRequirementRepository) {
        this.visaRequirementRepository = visaRequirementRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<VisaRequirementResponse> getVisaRequirement(@PathVariable Long id) {
        VisaRequirement visa = visaRequirementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Visa requirement not found: " + id));
        return ResponseEntity.ok(new VisaRequirementResponse(visa));
    }

    @PutMapping("/{id}")
    @PatchMapping("/{id}/status")
    public ResponseEntity<VisaRequirementResponse> updateVisaRequirementStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        VisaRequirement visa = visaRequirementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Visa requirement not found: " + id));

        if (body.get("country") != null) visa.setCountry(body.get("country").toString());
        if (body.get("visaType") != null) visa.setVisaType(body.get("visaType").toString());
        if (body.get("status") != null) {
            try { visa.setStatus(VisaStatus.valueOf(body.get("status").toString().toUpperCase())); } catch (Exception ignored) {}
        }
        if (body.get("notes") != null) visa.setNotes(body.get("notes").toString());

        VisaRequirement saved = visaRequirementRepository.save(visa);
        return ResponseEntity.ok(new VisaRequirementResponse(saved));
    }
}
