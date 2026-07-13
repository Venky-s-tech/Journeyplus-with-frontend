package com.journeyplus.compliance.service;

import com.journeyplus.compliance.entity.ComplianceAudit;
import com.journeyplus.compliance.entity.PolicyException;
import com.journeyplus.compliance.repository.ComplianceAuditRepository;
import com.journeyplus.compliance.repository.PolicyExceptionRepository;
import com.journeyplus.expense.entity.ExpenseClaim;
import com.journeyplus.expense.entity.ExpenseLine;
import com.journeyplus.iam.entity.User;
import com.journeyplus.policy.entity.CityTier;
import com.journeyplus.policy.entity.TravelPolicy;
import com.journeyplus.policy.repository.CityTierRepository;
import com.journeyplus.policy.repository.TravelPolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.context.ApplicationEventPublisher;
import com.journeyplus.event.StatusChangeEvent;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;
import java.time.LocalDate;

@Component
public class PolicyComplianceEngine {

    @Autowired
    private TravelPolicyRepository travelPolicyRepository;

    @Autowired
    private CityTierRepository cityTierRepository;

    @Autowired
    private ComplianceAuditRepository complianceAuditRepository;

    @Autowired
    private PolicyExceptionRepository policyExceptionRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void runComplianceCheck(ExpenseLine line) {
        ExpenseClaim claim = line.getExpenseClaim();
        User employee = claim.getEmployee();
        
        boolean hasBreach = false;
        StringBuilder violations = new StringBuilder();
        BigDecimal exceededAmount = BigDecimal.ZERO;
        TravelPolicy matchedPolicy = null;

        // Resolve Effective Policy for the trip date
        String gradeId = employee.getGrade() != null ? employee.getGrade().getId() : "G1";
        String tType = claim.getTripRequest().getTravelType();
        com.journeyplus.policy.entity.TravelType travelType = com.journeyplus.policy.entity.TravelType.DOMESTIC;
        if (tType != null && ("INTERNATIONAL".equalsIgnoreCase(tType) || "INTL".equalsIgnoreCase(tType))) {
            travelType = com.journeyplus.policy.entity.TravelType.INTERNATIONAL;
        }

        java.time.LocalDateTime tripDate = claim.getTripRequest().getDepartureDate().atStartOfDay();
        List<TravelPolicy> policies = travelPolicyRepository.findEffectivePoliciesForDate(gradeId, travelType, tripDate);
        if (!policies.isEmpty()) {
            matchedPolicy = policies.get(0);
        }

        // 1. Check Per Diem / Local Conveyance Limits (OverEntitlement)
        boolean isOverEntitlement = false;
        BigDecimal lineLimit = BigDecimal.ZERO;
        
        if (matchedPolicy != null) {
            if ("TRANSPORT".equalsIgnoreCase(line.getCategory())) {
                lineLimit = matchedPolicy.getLocalConveyanceLimit();
                if (lineLimit != null && line.getUsdEquivalent().compareTo(lineLimit) > 0) {
                    isOverEntitlement = true;
                    BigDecimal diff = line.getUsdEquivalent().subtract(lineLimit);
                    exceededAmount = exceededAmount.add(diff);
                    violations.append("Local transport limit of ").append(lineLimit).append(" USD exceeded by ").append(diff).append(" USD. ");
                }
            } else if ("MEALS".equalsIgnoreCase(line.getCategory()) || "MISC".equalsIgnoreCase(line.getCategory())) {
                lineLimit = matchedPolicy.getPerDiemRate();
                if (lineLimit != null && line.getUsdEquivalent().compareTo(lineLimit) > 0) {
                    isOverEntitlement = true;
                    BigDecimal diff = line.getUsdEquivalent().subtract(lineLimit);
                    exceededAmount = exceededAmount.add(diff);
                    violations.append("Policy per diem limit of ").append(lineLimit).append(" USD exceeded by ").append(diff).append(" USD. ");
                }
            }
        }

        // 2. Check City Tier Daily Allowance & Hotel Cap (OverEntitlement)
        String destination = claim.getTripRequest().getDestination();
        Optional<CityTier> cityTierOpt = cityTierRepository.findByCityNameIgnoreCase(destination);
        if (cityTierOpt.isPresent()) {
            CityTier tier = cityTierOpt.get();
            if ("ACCOMMODATION".equalsIgnoreCase(line.getCategory())) {
                BigDecimal hotelCap = tier.getHotelCapPerNight();
                if (hotelCap != null && line.getUsdEquivalent().compareTo(hotelCap) > 0) {
                    isOverEntitlement = true;
                    BigDecimal diff = line.getUsdEquivalent().subtract(hotelCap);
                    exceededAmount = exceededAmount.add(diff);
                    violations.append("City tier hotel cap of ").append(hotelCap).append(" USD exceeded by ").append(diff).append(" USD. ");
                }
            } else if ("MEALS".equalsIgnoreCase(line.getCategory())) {
                BigDecimal dailyLimit = tier.getPerDiemRate();
                if (dailyLimit != null && line.getUsdEquivalent().compareTo(dailyLimit) > 0) {
                    isOverEntitlement = true;
                    BigDecimal diff = line.getUsdEquivalent().subtract(dailyLimit);
                    exceededAmount = exceededAmount.add(diff);
                    violations.append("City tier daily allowance per-diem limit of ").append(dailyLimit).append(" USD exceeded by ").append(diff).append(" USD. ");
                }
            }
        }

        if (isOverEntitlement) {
            hasBreach = true;
        }

        // 3. Check for Missing Receipt
        boolean isMissingReceipt = (line.getReceiptPath() == null || line.getReceiptPath().isBlank());
        if (isMissingReceipt) {
            hasBreach = true;
            violations.append("Receipt is missing. ");
        }

        // 4. Check for Late Claim (>30 days after trip return date)
        boolean isLateClaim = false;
        LocalDate returnDate = claim.getTripRequest().getReturnDate();
        LocalDate submissionDate = claim.getSubmittedDate() != null ? claim.getSubmittedDate() : LocalDate.now();
        if (submissionDate.isAfter(returnDate.plusDays(30))) {
            isLateClaim = true;
            hasBreach = true;
            violations.append("Late claim submission: submitted more than 30 days after trip return date. ");
        }

        if (hasBreach) {
            line.setPolicyComplianceStatus("NON_COMPLIANT");
            line.setComplianceRemarks(violations.toString());
            line.setPolicyCompliant(false);
            line.setStatus(com.journeyplus.expense.entity.ExpenseLineStatus.FLAGGED);

            ComplianceAudit audit = new ComplianceAudit(
                    line,
                    null, // System automated audit
                    "FLAG_BREACH",
                    violations.toString(),
                    "Automated policy compliance check failed."
            );
            ComplianceAudit savedAudit = complianceAuditRepository.save(audit);
            try {
                savedAudit.setClaim(claim);
                complianceAuditRepository.save(savedAudit);
            } catch (Exception e) {}

            // Save PolicyExceptions for each breach type
            if (isOverEntitlement) {
                PolicyException ex = new PolicyException(savedAudit, matchedPolicy, line, "OverEntitlement", exceededAmount);
                ex.setClaim(claim);
                policyExceptionRepository.save(ex);
            }
            if (isMissingReceipt) {
                PolicyException ex = new PolicyException(savedAudit, matchedPolicy, line, "MissingReceipt", BigDecimal.ZERO);
                ex.setClaim(claim);
                policyExceptionRepository.save(ex);
            }
            if (isLateClaim) {
                PolicyException ex = new PolicyException(savedAudit, matchedPolicy, line, "LateClaim", BigDecimal.ZERO);
                ex.setClaim(claim);
                policyExceptionRepository.save(ex);
            }

            // Notify Employee about the compliance exception
            try {
                eventPublisher.publishEvent(new StatusChangeEvent(
                    employee.getId(),
                    "Compliance Policy Exception Flagged",
                    "A policy compliance exception has been flagged on your expense claim: " + violations.toString(),
                    null,
                    "System Automated Compliance"
                ));
            } catch (Exception e) {}
        } else {
            line.setPolicyComplianceStatus("COMPLIANT");
            line.setComplianceRemarks("Automated check passed. All limits satisfied.");
            line.setPolicyCompliant(true);
            line.setStatus(com.journeyplus.expense.entity.ExpenseLineStatus.INCLUDED);

            ComplianceAudit audit = new ComplianceAudit(
                    line,
                    null,
                    "PASSED",
                    null,
                    "Automated policy compliance check passed successfully."
            );
            ComplianceAudit saved = complianceAuditRepository.save(audit);
            try {
                saved.setClaim(claim);
                complianceAuditRepository.save(saved);
            } catch (Exception e) {}
        }
    }
}
