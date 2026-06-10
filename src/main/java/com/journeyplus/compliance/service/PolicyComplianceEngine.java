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

import java.math.BigDecimal;
import java.util.Optional;

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

    public void runComplianceCheck(ExpenseLine line) {
        ExpenseClaim claim = line.getExpenseClaim();
        User employee = claim.getEmployee();
        
        boolean hasBreach = false;
        StringBuilder violations = new StringBuilder();
        BigDecimal exceededAmount = BigDecimal.ZERO;
        TravelPolicy matchedPolicy = null;

        // 1. Check Role-based Maximum Trip Limit Policy
        Optional<TravelPolicy> policyOpt = travelPolicyRepository.findByEmployeeRole(employee.getRole());
        if (policyOpt.isPresent()) {
            matchedPolicy = policyOpt.get();
            BigDecimal limit = matchedPolicy.getMaxAmountPerTrip();
            // We compare USD equivalents for standardized multi-currency audit limits
            if (line.getUsdEquivalent().compareTo(limit) > 0) {
                hasBreach = true;
                exceededAmount = line.getUsdEquivalent().subtract(limit);
                violations.append("Role policy limit of ").append(limit).append(" USD exceeded by ")
                        .append(exceededAmount).append(" USD. ");
            }
        }

        // 2. Check City Tier Daily Allowance Limits
        // In Phase 1, we can extract tier by checking destination of the trip or if the category is ACCOMMODATION/MEALS
        // If there's a city tier defined matching the trip request destination, check daily allowance
        String destination = claim.getTripRequest().getDestination();
        Optional<CityTier> cityTierOpt = cityTierRepository.findByCityNameIgnoreCase(destination);
        if (cityTierOpt.isPresent()) {
            CityTier tier = cityTierOpt.get();
            BigDecimal dailyLimit = tier.getDailyAllowanceLimit();
            if (line.getUsdEquivalent().compareTo(dailyLimit) > 0) {
                hasBreach = true;
                BigDecimal diff = line.getUsdEquivalent().subtract(dailyLimit);
                exceededAmount = exceededAmount.add(diff);
                violations.append("City Tier allowance limit of ").append(dailyLimit).append(" USD exceeded by ")
                        .append(diff).append(" USD. ");
            }
        }

        if (hasBreach) {
            line.setPolicyComplianceStatus("NON_COMPLIANT");
            line.setComplianceRemarks(violations.toString());

            ComplianceAudit audit = new ComplianceAudit(
                    line,
                    null, // System automated audit
                    "FLAG_BREACH",
                    violations.toString(),
                    "Automated policy compliance check failed."
            );
            ComplianceAudit savedAudit = complianceAuditRepository.save(audit);

            PolicyException exception = new PolicyException(
                    savedAudit,
                    matchedPolicy,
                    line,
                    "POLICY_LIMIT_EXCEEDED",
                    exceededAmount
            );
            policyExceptionRepository.save(exception);
        } else {
            line.setPolicyComplianceStatus("COMPLIANT");
            line.setComplianceRemarks("Automated check passed. All limits satisfied.");

            ComplianceAudit audit = new ComplianceAudit(
                    line,
                    null,
                    "PASSED",
                    null,
                    "Automated policy compliance check passed successfully."
            );
            complianceAuditRepository.save(audit);
        }
    }
}
