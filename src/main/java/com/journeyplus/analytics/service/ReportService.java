package com.journeyplus.analytics.service;

import com.journeyplus.advance.entity.AdvanceRequest;
import com.journeyplus.advance.entity.AdvanceSettlement;
import com.journeyplus.advance.entity.AdvanceStatus;
import com.journeyplus.advance.repository.AdvanceRequestRepository;
import com.journeyplus.advance.repository.AdvanceSettlementRepository;
import com.journeyplus.analytics.dto.TopTravellerResponse;
import com.journeyplus.analytics.entity.ReportScope;
import com.journeyplus.analytics.entity.TravelReport;
import com.journeyplus.analytics.repository.TravelReportRepository;
import com.journeyplus.config.AuditAction;
import com.journeyplus.expense.entity.ExpenseClaim;
import com.journeyplus.expense.entity.ExpenseLine;
import com.journeyplus.expense.entity.ExpenseLineStatus;
import com.journeyplus.expense.entity.ExpenseStatus;
import com.journeyplus.expense.repository.ExpenseClaimRepository;
import com.journeyplus.expense.repository.ExpenseLineRepository;
import com.journeyplus.iam.entity.User;
import com.journeyplus.trip.entity.TripRequest;
import com.journeyplus.trip.repository.TripRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final TravelReportRepository travelReportRepository;
    private final TripRequestRepository tripRequestRepository;
    private final ExpenseClaimRepository expenseClaimRepository;
    private final ExpenseLineRepository expenseLineRepository;
    private final AdvanceRequestRepository advanceRequestRepository;
    private final AdvanceSettlementRepository advanceSettlementRepository;
    private final double budgetCap;

    public ReportService(
            TravelReportRepository travelReportRepository,
            TripRequestRepository tripRequestRepository,
            ExpenseClaimRepository expenseClaimRepository,
            ExpenseLineRepository expenseLineRepository,
            AdvanceRequestRepository advanceRequestRepository,
            AdvanceSettlementRepository advanceSettlementRepository,
            @Value("${app.analytics.budget-cap:100000.00}") double budgetCap) {
        this.travelReportRepository = travelReportRepository;
        this.tripRequestRepository = tripRequestRepository;
        this.expenseClaimRepository = expenseClaimRepository;
        this.expenseLineRepository = expenseLineRepository;
        this.advanceRequestRepository = advanceRequestRepository;
        this.advanceSettlementRepository = advanceSettlementRepository;
        this.budgetCap = budgetCap;
    }

    @Transactional
    @AuditAction(module = "ANALYTICS", action = "GENERATE_REPORT")
    public TravelReport generateReport(String title, String reportType, String parameters, String generatedBy) {
        String mockFileName = "report_" + reportType.toLowerCase() + "_" + UUID.randomUUID().toString().substring(0, 8) + ".csv";
        String localFilePath = "c:/journeyplus/reports/" + mockFileName;

        TravelReport report = new TravelReport(title, reportType, parameters, generatedBy, localFilePath);
        return travelReportRepository.save(report);
    }

    @Transactional
    @AuditAction(module = "ANALYTICS", action = "GENERATE_REPORT")
    public TravelReport generateTravelReport(ReportScope scope, String scopeValue, String generatedBy) {
        log.info("Generating travel report for scope: {}, value: {}", scope, scopeValue);

        List<TripRequest> allTrips = tripRequestRepository.findAll();
        List<ExpenseClaim> allClaims = expenseClaimRepository.findAll();

        List<TripRequest> trips = allTrips.stream().filter(t -> {
            if (scope == ReportScope.Department) {
                return t.getEmployee() != null && scopeValue.equalsIgnoreCase(t.getEmployee().getDepartmentId());
            }
            if (scope == ReportScope.Grade) {
                return t.getEmployee() != null && t.getEmployee().getGrade() != null && scopeValue.equalsIgnoreCase(t.getEmployee().getGrade().getId());
            }
            if (scope == ReportScope.Destination) {
                return scopeValue.equalsIgnoreCase(t.getDestination());
            }
            if (scope == ReportScope.Period) {
                if (t.getDepartureDate() != null) {
                    String period = t.getDepartureDate().getYear() + "-" + String.format("%02d", t.getDepartureDate().getMonthValue());
                    return scopeValue.equals(period);
                }
            }
            return true;
        }).toList();

        List<ExpenseClaim> claims = allClaims.stream().filter(c -> {
            if (scope == ReportScope.Department) {
                return c.getEmployee() != null && scopeValue.equalsIgnoreCase(c.getEmployee().getDepartmentId());
            }
            if (scope == ReportScope.Grade) {
                return c.getEmployee() != null && c.getEmployee().getGrade() != null && scopeValue.equalsIgnoreCase(c.getEmployee().getGrade().getId());
            }
            if (scope == ReportScope.Destination) {
                return c.getTripRequest() != null && scopeValue.equalsIgnoreCase(c.getTripRequest().getDestination());
            }
            if (scope == ReportScope.Period) {
                if (c.getSubmittedDate() != null) {
                    String period = c.getSubmittedDate().getYear() + "-" + String.format("%02d", c.getSubmittedDate().getMonthValue());
                    return scopeValue.equals(period);
                }
            }
            return true;
        }).toList();

        long tripCount = trips.size();
        BigDecimal totalSpend = BigDecimal.ZERO;
        for (ExpenseClaim c : claims) {
            if (c.getStatus() == ExpenseStatus.APPROVED ||
                c.getStatus() == ExpenseStatus.PAID ||
                c.getStatus() == ExpenseStatus.PARTIALLY_PAID) {
                totalSpend = totalSpend.add(c.getUsdEquivalent());
            }
        }

        BigDecimal avgCostPerTrip = BigDecimal.ZERO;
        if (tripCount > 0) {
            avgCostPerTrip = totalSpend.divide(BigDecimal.valueOf(tripCount), 2, RoundingMode.HALF_UP);
        }

        BigDecimal totalDisbursedAdvancesUsd = BigDecimal.ZERO;
        BigDecimal totalSettledUtilisedAdvancesUsd = BigDecimal.ZERO;

        for (TripRequest t : trips) {
            List<AdvanceRequest> advRequests = advanceRequestRepository.findByTripRequest_Id(t.getId());
            for (AdvanceRequest adv : advRequests) {
                if (adv.getStatus() == AdvanceStatus.DISBURSED ||
                    adv.getStatus() == AdvanceStatus.SETTLED) {
                    BigDecimal advAmt = adv.getUsdEquivalent() != null ? adv.getUsdEquivalent() : adv.getRequestedAmount();
                    totalDisbursedAdvancesUsd = totalDisbursedAdvancesUsd.add(advAmt);

                    List<AdvanceSettlement> settlements = advanceSettlementRepository.findByAdvanceRequest_Id(adv.getId());
                    for (AdvanceSettlement set : settlements) {
                        BigDecimal rate = adv.getUsdEquivalent() != null && adv.getRequestedAmount().compareTo(BigDecimal.ZERO) > 0 ?
                            adv.getUsdEquivalent().divide(adv.getRequestedAmount(), 4, RoundingMode.HALF_UP) : BigDecimal.ONE;
                        totalSettledUtilisedAdvancesUsd = totalSettledUtilisedAdvancesUsd.add(set.getAmountUtilised().multiply(rate));
                    }
                }
            }
        }

        BigDecimal advanceSettlementRate = BigDecimal.ZERO;
        if (totalDisbursedAdvancesUsd.compareTo(BigDecimal.ZERO) > 0) {
            advanceSettlementRate = totalSettledUtilisedAdvancesUsd.divide(totalDisbursedAdvancesUsd, 4, RoundingMode.HALF_UP);
        }

        long totalLines = 0;
        long flaggedLines = 0;
        for (ExpenseClaim c : claims) {
            List<ExpenseLine> lines = expenseLineRepository.findByExpenseClaim_Id(c.getId());
            totalLines += lines.size();
            flaggedLines += lines.stream().filter(l -> l.getStatus() == ExpenseLineStatus.FLAGGED ||
                                                       l.getStatus() == ExpenseLineStatus.REJECTED).count();
        }

        BigDecimal policyExceptionRate = BigDecimal.ZERO;
        if (totalLines > 0) {
            policyExceptionRate = BigDecimal.valueOf(flaggedLines).divide(BigDecimal.valueOf(totalLines), 4, RoundingMode.HALF_UP);
        }

        BigDecimal budgetUtilisation = BigDecimal.ZERO;
        BigDecimal cap = BigDecimal.valueOf(budgetCap);
        if (cap.compareTo(BigDecimal.ZERO) > 0) {
            budgetUtilisation = totalSpend.divide(cap, 4, RoundingMode.HALF_UP);
        }

        TravelReport report = new TravelReport();
        report.setTitle("Travel Spend Report - " + scope + " (" + scopeValue + ")");
        report.setReportType("METRICS_" + scope.name().toUpperCase());
        report.setParameters("scopeValue=" + scopeValue);
        report.setGeneratedBy(generatedBy);
        report.setGeneratedAt(LocalDateTime.now());
        report.setFilePath("N/A - Database Record");
        report.setScope(scope);
        report.setScopeValue(scopeValue);
        report.setTripCount(tripCount);
        report.setTotalSpend(totalSpend);
        report.setAvgCostPerTrip(avgCostPerTrip);
        report.setAdvanceSettlementRate(advanceSettlementRate);
        report.setPolicyExceptionRate(policyExceptionRate);
        report.setBudgetUtilisation(budgetUtilisation);
        report.setGeneratedDate(LocalDateTime.now());

        return travelReportRepository.save(report);
    }

    public List<TopTravellerResponse> getTopTravellers() {
        log.info("Calculating Top Travellers list");
        List<ExpenseClaim> allClaims = expenseClaimRepository.findAll();
        Map<User, BigDecimal> userSpend = new HashMap<>();

        for (ExpenseClaim c : allClaims) {
            if (c.getStatus() == ExpenseStatus.APPROVED ||
                c.getStatus() == ExpenseStatus.PAID ||
                c.getStatus() == ExpenseStatus.PARTIALLY_PAID) {
                User emp = c.getEmployee();
                if (emp != null) {
                    userSpend.put(emp, userSpend.getOrDefault(emp, BigDecimal.ZERO).add(c.getUsdEquivalent()));
                }
            }
        }

        return userSpend.entrySet().stream()
                .map(entry -> new TopTravellerResponse(
                        entry.getKey().getId(),
                        entry.getKey().getName(),
                        entry.getKey().getEmail(),
                        entry.getKey().getDepartmentId(),
                        entry.getValue()
                ))
                .sorted(Comparator.comparing(TopTravellerResponse::getTotalSpend).reversed())
                .limit(10)
                .toList();
    }

    public List<TravelReport> getReportsByType(String reportType) {
        return travelReportRepository.findByReportType(reportType);
    }

    public List<TravelReport> getAllReports() {
        return travelReportRepository.findAll();
    }
}
