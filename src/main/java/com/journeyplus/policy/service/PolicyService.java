package com.journeyplus.policy.service;

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

    @Autowired
    private TravelPolicyRepository travelPolicyRepository;

    @Autowired
    private CityTierRepository cityTierRepository;

    @Transactional
    @AuditAction(module = "POLICY", action = "CREATE_POLICY")
    public TravelPolicy createPolicy(TravelPolicy policy) {
        travelPolicyRepository.findByEmployeeRole(policy.getEmployeeRole()).ifPresent(existing -> {
            throw new IllegalArgumentException("Policy already exists for role: " + policy.getEmployeeRole());
        });
        return travelPolicyRepository.save(policy);
    }

    @Transactional
    @AuditAction(module = "POLICY", action = "CREATE_CITY_TIER")
    public CityTier createCityTier(CityTier cityTier) {
        cityTierRepository.findByCityNameIgnoreCase(cityTier.getCityName()).ifPresent(existing -> {
            throw new IllegalArgumentException("City tier already exists for city: " + cityTier.getCityName());
        });
        return cityTierRepository.save(cityTier);
    }

    public TravelPolicy getPolicyByRole(Role role) {
        return travelPolicyRepository.findByEmployeeRole(role)
                .orElseThrow(() -> new IllegalArgumentException("No travel policy defined for role: " + role));
    }

    public CityTier getCityTier(String cityName) {
        return cityTierRepository.findByCityNameIgnoreCase(cityName)
                .orElseThrow(() -> new IllegalArgumentException("No city tier defined for city: " + cityName));
    }

    public List<TravelPolicy> getAllPolicies() {
        return travelPolicyRepository.findAll();
    }

    public List<CityTier> getAllCityTiers() {
        return cityTierRepository.findAll();
    }
}
