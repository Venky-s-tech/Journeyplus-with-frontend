package com.journeyplus.analytics.service;

import com.journeyplus.analytics.entity.TravelReport;
import com.journeyplus.analytics.repository.TravelReportRepository;
import com.journeyplus.config.AuditAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ReportService {

    @Autowired
    private TravelReportRepository travelReportRepository;

    @Transactional
    @AuditAction(module = "ANALYTICS", action = "GENERATE_REPORT")
    public TravelReport generateReport(String title, String reportType, String parameters, String generatedBy) {
        // Generate a mock file reference path on server
        String mockFileName = "report_" + reportType.toLowerCase() + "_" + UUID.randomUUID().toString().substring(0, 8) + ".csv";
        String localFilePath = "c:/journeyplus/reports/" + mockFileName;

        TravelReport report = new TravelReport(title, reportType, parameters, generatedBy, localFilePath);
        return travelReportRepository.save(report);
    }

    public List<TravelReport> getReportsByType(String reportType) {
        return travelReportRepository.findByReportType(reportType);
    }

    public List<TravelReport> getAllReports() {
        return travelReportRepository.findAll();
    }
}
