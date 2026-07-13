package com.journeyplus.analytics.controller;

import com.journeyplus.analytics.entity.TravelReport;
import com.journeyplus.analytics.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasAnyRole('FINANCE', 'COMPLIANCE', 'ADMIN')")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @PostMapping
    public ResponseEntity<TravelReport> generateReport(
            @RequestParam String title,
            @RequestParam String reportType,
            @RequestParam(required = false) String parameters,
            Principal principal) {
        return ResponseEntity.ok(reportService.generateReport(title, reportType, parameters, principal.getName()));
    }

    @PostMapping("/metrics")
    public ResponseEntity<TravelReport> generateMetricsReport(
            @RequestParam com.journeyplus.analytics.entity.ReportScope scope,
            @RequestParam String scopeValue,
            Principal principal) {
        return ResponseEntity.ok(reportService.generateTravelReport(scope, scopeValue, principal.getName()));
    }

    @GetMapping("/top-travellers")
    public ResponseEntity<List<com.journeyplus.analytics.dto.TopTravellerResponse>> getTopTravellers() {
        return ResponseEntity.ok(reportService.getTopTravellers());
    }

    @GetMapping
    public ResponseEntity<List<TravelReport>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<TravelReport>> getReportsByType(@PathVariable String type) {
        return ResponseEntity.ok(reportService.getReportsByType(type));
    }
}
