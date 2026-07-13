package com.journeyplus.analytics.entity;
 
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
 
@Entity
@Table(name = "travel_reports")
@Getter
@Setter
public class TravelReport {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(nullable = true, length = 150)
    private String title;
 
    @Column(name = "report_type", nullable = true, length = 50)
    private String reportType; // SPENDING_BY_DEPT, POLICY_VIOLATIONS, ADVANCE_AGING
 
    @Column(columnDefinition = "TEXT")
    private String parameters;
 
    @Column(name = "generated_by", nullable = true, length = 100)
    private String generatedBy;
 
    @Column(name = "generated_at")
    private LocalDateTime generatedAt = LocalDateTime.now();
 
    @Column(name = "file_path", nullable = true)
    private String filePath;

    // Spec Reporting Metrics
    @Enumerated(EnumType.STRING)
    @Column(name = "scope", length = 50)
    private ReportScope scope;

    @Column(name = "scope_value")
    private String scopeValue;

    @Column(name = "trip_count")
    private Long tripCount;

    @Column(name = "total_spend")
    private BigDecimal totalSpend;

    @Column(name = "avg_cost_per_trip")
    private BigDecimal avgCostPerTrip;

    @Column(name = "advance_settlement_rate")
    private BigDecimal advanceSettlementRate;

    @Column(name = "policy_exception_rate")
    private BigDecimal policyExceptionRate;

    @Column(name = "budget_utilisation")
    private BigDecimal budgetUtilisation;

    @Column(name = "generated_date")
    private LocalDateTime generatedDate = LocalDateTime.now();
 
    public TravelReport() {}
 
    public TravelReport(String title, String reportType, String parameters, String generatedBy, String filePath) {
        this.title = title;
        this.reportType = reportType;
        this.parameters = parameters;
        this.generatedBy = generatedBy;
        this.filePath = filePath;
        this.generatedAt = LocalDateTime.now();
    }
}
