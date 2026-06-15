package com.journeyplus.policy.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.journeyplus.iam.entity.Role;
import com.journeyplus.policy.entity.CityTier;
import com.journeyplus.policy.entity.TravelPolicy;
import com.journeyplus.policy.repository.CityTierRepository;
import com.journeyplus.policy.repository.TravelPolicyRepository;

@ExtendWith(MockitoExtension.class)
public class PolicyServiceTest {

    @Mock
    private TravelPolicyRepository travelPolicyRepository;

    @Mock
    private CityTierRepository cityTierRepository;

    @InjectMocks
    private PolicyService policyService;

    @Test
    public void createPolicy_Success() {
        TravelPolicy policy = new TravelPolicy("Employee Policy", "Policy for employees", Role.EMPLOYEE, new BigDecimal("1000.00"), false, "admin");
        
        when(travelPolicyRepository.findByEmployeeRole(Role.EMPLOYEE)).thenReturn(Optional.empty());
        when(travelPolicyRepository.save(any(TravelPolicy.class))).thenReturn(policy);

        TravelPolicy created = policyService.createPolicy(policy);

        assertNotNull(created);
        assertEquals("Employee Policy", created.getPolicyName());
        assertEquals(Role.EMPLOYEE, created.getEmployeeRole());
        verify(travelPolicyRepository, times(1)).save(policy);
    }

    @Test
    public void createPolicy_ThrowsException_AlreadyExists() {
        TravelPolicy policy = new TravelPolicy("Employee Policy", "Policy for employees", Role.EMPLOYEE, new BigDecimal("1000.00"), false, "admin");
        
        when(travelPolicyRepository.findByEmployeeRole(Role.EMPLOYEE)).thenReturn(Optional.of(policy));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            policyService.createPolicy(policy);
        });

        assertEquals("Policy already exists for role: EMPLOYEE", exception.getMessage());
        verify(travelPolicyRepository, never()).save(any(TravelPolicy.class));
    }

    @Test
    public void createCityTier_Success() {
        CityTier cityTier = new CityTier("New York", "TIER_1", new BigDecimal("300.00"));

        when(cityTierRepository.findByCityNameIgnoreCase("New York")).thenReturn(Optional.empty());
        when(cityTierRepository.save(any(CityTier.class))).thenReturn(cityTier);

        CityTier created = policyService.createCityTier(cityTier);

        assertNotNull(created);
        assertEquals("New York", created.getCityName());
        assertEquals("TIER_1", created.getTier());
        verify(cityTierRepository, times(1)).save(cityTier);
    }

    @Test
    public void createCityTier_ThrowsException_AlreadyExists() {
        CityTier cityTier = new CityTier("New York", "TIER_1", new BigDecimal("300.00"));

        when(cityTierRepository.findByCityNameIgnoreCase("New York")).thenReturn(Optional.of(cityTier));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            policyService.createCityTier(cityTier);
        });

        assertEquals("City tier already exists for city: New York", exception.getMessage());
        verify(cityTierRepository, never()).save(any(CityTier.class));
    }

    @Test
    public void getPolicyByRole_Success() {
        TravelPolicy policy = new TravelPolicy("Manager Policy", "Policy for managers", Role.APPROVING_MANAGER, new BigDecimal("2000.00"), true, "admin");

        when(travelPolicyRepository.findByEmployeeRole(Role.APPROVING_MANAGER)).thenReturn(Optional.of(policy));

        TravelPolicy result = policyService.getPolicyByRole(Role.APPROVING_MANAGER);

        assertNotNull(result);
        assertEquals("Manager Policy", result.getPolicyName());
    }

    @Test
    public void getPolicyByRole_ThrowsException_NotFound() {
        when(travelPolicyRepository.findByEmployeeRole(Role.FINANCE_EXECUTIVE)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            policyService.getPolicyByRole(Role.FINANCE_EXECUTIVE);
        });

        assertEquals("No travel policy defined for role: FINANCE_EXECUTIVE", exception.getMessage());
    }

    @Test
    public void getCityTier_Success() {
        CityTier cityTier = new CityTier("San Francisco", "TIER_1", new BigDecimal("350.00"));

        when(cityTierRepository.findByCityNameIgnoreCase("San Francisco")).thenReturn(Optional.of(cityTier));

        CityTier result = policyService.getCityTier("San Francisco");

        assertNotNull(result);
        assertEquals("San Francisco", result.getCityName());
    }

    @Test
    public void getCityTier_ThrowsException_NotFound() {
        when(cityTierRepository.findByCityNameIgnoreCase("Chicago")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            policyService.getCityTier("Chicago");
        });

        assertEquals("No city tier defined for city: Chicago", exception.getMessage());
    }

    @Test
    public void getAllPolicies() {
        TravelPolicy p1 = new TravelPolicy();
        TravelPolicy p2 = new TravelPolicy();
        when(travelPolicyRepository.findAll()).thenReturn(Arrays.asList(p1, p2));

        List<TravelPolicy> result = policyService.getAllPolicies();

        assertEquals(2, result.size());
        verify(travelPolicyRepository, times(1)).findAll();
    }

    @Test
    public void getAllCityTiers() {
        CityTier c1 = new CityTier();
        CityTier c2 = new CityTier();
        when(cityTierRepository.findAll()).thenReturn(Arrays.asList(c1, c2));

        List<CityTier> result = policyService.getAllCityTiers();

        assertEquals(2, result.size());
        verify(cityTierRepository, times(1)).findAll();
    }
}
