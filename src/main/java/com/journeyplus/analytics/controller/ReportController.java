package com.journeyplus.analytics.controller;

import com.journeyplus.analytics.entity.TravelReport;
import com.journeyplus.analytics.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasAnyRole('FINANCE', 'COMPLIANCE', 'ADMIN')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

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

    @PostMapping("/generate")
    public ResponseEntity<TravelReport> generateReportJson(
            @RequestBody(required = false) java.util.Map<String, Object> body,
            Principal principal) {
        String title = body != null && body.get("title") != null ? body.get("title").toString() : "Travel Analytics Report";
        String reportType = body != null && body.get("reportType") != null ? body.get("reportType").toString() : "SUMMARY";
        String parameters = body != null && body.get("filters") != null ? body.get("filters").toString() : null;
        String username = principal != null ? principal.getName() : "SYSTEM";
        return ResponseEntity.ok(reportService.generateReport(title, reportType, parameters, username));
    }

    @GetMapping("/{id}/export")
    public ResponseEntity<?> exportReport(
            @PathVariable Long id,
            @RequestParam(defaultValue = "csv") String format) {
        String filename = "report-" + id + "." + ("pdf".equalsIgnoreCase(format) ? "pdf" : "csv");
        String contentType = "pdf".equalsIgnoreCase(format) ? "application/pdf" : "text/csv";
        
        String content = "Report ID,Title,Report Type,Generated Date\n"
                + id + ",Travel Analytics Report,SUMMARY," + java.time.LocalDateTime.now() + "\n";
        
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                .body(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<TravelReport>> getReportsByType(@PathVariable String type) {
        return ResponseEntity.ok(reportService.getReportsByType(type));
    }
}
