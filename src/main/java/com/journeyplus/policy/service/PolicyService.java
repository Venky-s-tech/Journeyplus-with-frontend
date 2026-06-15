package com.journeyplus.policy.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.journeyplus.config.AuditAction;
import com.journeyplus.iam.entity.Role;
import com.journeyplus.policy.entity.CityTier;
import com.journeyplus.policy.entity.TravelPolicy;
import com.journeyplus.policy.repository.CityTierRepository;
import com.journeyplus.policy.repository.TravelPolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PolicyService {

    private static final Logger log = LoggerFactory.getLogger(PolicyService.class);

    @Autowired
    private TravelPolicyRepository travelPolicyRepository;

    @Autowired
    private CityTierRepository cityTierRepository;

    @Transactional
    @AuditAction(module = "POLICY", action = "CREATE_POLICY")
    public TravelPolicy createPolicy(TravelPolicy policy) {
        log.info("Attempting to create travel policy for role: {}", policy.getEmployeeRole());
        travelPolicyRepository.findByEmployeeRole(policy.getEmployeeRole()).ifPresent(existing -> {
            log.warn("Policy creation failed: Travel policy already exists for role {}", policy.getEmployeeRole());
            throw new IllegalArgumentException("Policy already exists for role: " + policy.getEmployeeRole());
        });
        TravelPolicy savedPolicy = travelPolicyRepository.save(policy);
        log.info("Travel policy successfully created for role: {}, ID: {}", savedPolicy.getEmployeeRole(), savedPolicy.getId());
        return savedPolicy;
    }

    @Transactional
    @AuditAction(module = "POLICY", action = "CREATE_CITY_TIER")
    public CityTier createCityTier(CityTier cityTier) {
        log.info("Attempting to create city tier for city: {}, tier: {}", cityTier.getCityName(), cityTier.getTier());
        cityTierRepository.findByCityNameIgnoreCase(cityTier.getCityName()).ifPresent(existing -> {
            log.warn("City tier creation failed: City tier already exists for city '{}'", cityTier.getCityName());
            throw new IllegalArgumentException("City tier already exists for city: " + cityTier.getCityName());
        });
        CityTier savedCityTier = cityTierRepository.save(cityTier);
        log.info("City tier successfully created for city: {}, tier: {}, ID: {}", savedCityTier.getCityName(), savedCityTier.getTier(), savedCityTier.getId());
        return savedCityTier;
    }

    public TravelPolicy getPolicyByRole(Role role) {
        log.info("Retrieving travel policy for role: {}", role);
        return travelPolicyRepository.findByEmployeeRole(role)
                .orElseThrow(() -> {
                    log.warn("Lookup failed: No travel policy defined for role {}", role);
                    return new IllegalArgumentException("No travel policy defined for role: " + role);
                });
    }

    public CityTier getCityTier(String cityName) {
        log.info("Retrieving city tier definition for city: {}", cityName);
        return cityTierRepository.findByCityNameIgnoreCase(cityName)
                .orElseThrow(() -> {
                    log.warn("Lookup failed: No city tier defined for city '{}'", cityName);
                    return new IllegalArgumentException("No city tier defined for city: " + cityName);
                });
    }

    public List<TravelPolicy> getAllPolicies() {
        log.info("Retrieving all travel policies");
        return travelPolicyRepository.findAll();
    }

    public List<CityTier> getAllCityTiers() {
        log.info("Retrieving all city tier configurations");
        return cityTierRepository.findAll();
    }
}
