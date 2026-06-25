package com.journeyplus.policy.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.journeyplus.iam.entity.Grade;
import com.journeyplus.iam.entity.Role;
import com.journeyplus.iam.repository.GradeRepository;
import com.journeyplus.policy.dto.*;
import com.journeyplus.policy.entity.*;
import com.journeyplus.policy.repository.CityTierRepository;
import com.journeyplus.policy.repository.TravelPolicyRepository;

@ExtendWith(MockitoExtension.class)
public class PolicyServiceTest {

    @Mock
    private TravelPolicyRepository travelPolicyRepository;

    @Mock
    private CityTierRepository cityTierRepository;

    @Mock
    private GradeRepository gradeRepository;

    @InjectMocks
    private PolicyService policyService;

    // ==========================================
    // Compatibility Tests (Must continue to pass)
    // ==========================================

    @Test
    public void createPolicy_Success() {
        TravelPolicy policy = new TravelPolicy("Employee Policy", "Policy for employees", Role.EMPLOYEE, new BigDecimal("1000.00"), false, "admin");
        Grade grade = new Grade("G1", "Junior Employee", "Junior Level", "Active");

        when(gradeRepository.findById("G1")).thenReturn(Optional.of(grade));
        when(travelPolicyRepository.findByGrade_IdAndTravelTypeAndStatus("G1", TravelType.DOMESTIC, PolicyStatus.ACTIVE)).thenReturn(Optional.empty());
        when(travelPolicyRepository.save(any(TravelPolicy.class))).thenReturn(policy);

        TravelPolicy created = policyService.createPolicy(policy);

        assertNotNull(created);
        assertEquals("Employee Policy", created.getPolicyName());
        verify(travelPolicyRepository, times(1)).save(any(TravelPolicy.class));
    }

    @Test
    public void createCityTier_Success() {
        CityTier cityTier = new CityTier("New York", "TIER_1", new BigDecimal("300.00"));

        when(cityTierRepository.findByCityNameIgnoreCaseAndCountryIgnoreCase("New York", "India")).thenReturn(Optional.empty());
        when(cityTierRepository.save(any(CityTier.class))).thenReturn(cityTier);

        CityTier created = policyService.createCityTier(cityTier);

        assertNotNull(created);
        assertEquals("New York", created.getCityName());
        verify(cityTierRepository, times(1)).save(any(CityTier.class));
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
        when(travelPolicyRepository.findByEmployeeRole(Role.FINANCE)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            policyService.getPolicyByRole(Role.FINANCE);
        });

        assertEquals("No travel policy defined for role: FINANCE", exception.getMessage());
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

    // ==========================================
    // New Feature & Business Rule Tests
    // ==========================================

    @Test
    public void createPolicy_WithVersioning_SupersedesOldPolicy() {
        TravelPolicyRequest request = new TravelPolicyRequest();
        request.setPolicyName("G3 Domestic Active");
        request.setGradeId("G3");
        request.setTravelType(TravelType.DOMESTIC);
        request.setFlightClass(FlightClass.BUSINESS);
        request.setHotelCategory(HotelCategory.PREMIUM);
        request.setPerDiemRate(new BigDecimal("200.00"));
        request.setLocalConveyanceLimit(new BigDecimal("80.00"));
        request.setStatus(PolicyStatus.ACTIVE);

        Grade grade = new Grade("G3", "Manager", "Manager Level", "Active");
        TravelPolicy oldPolicy = new TravelPolicy("Old Active", "Desc", grade, TravelType.DOMESTIC, 
                FlightClass.ECONOMY, HotelCategory.STANDARD, new BigDecimal("100.00"), new BigDecimal("50.00"), 
                LocalDateTime.now(), PolicyStatus.ACTIVE, "admin");

        when(gradeRepository.findById("G3")).thenReturn(Optional.of(grade));
        when(travelPolicyRepository.findByGrade_IdAndTravelTypeAndStatus("G3", TravelType.DOMESTIC, PolicyStatus.ACTIVE))
                .thenReturn(Optional.of(oldPolicy));

        TravelPolicy savedPolicy = new TravelPolicy("G3 Domestic Active", "Desc", grade, TravelType.DOMESTIC, 
                FlightClass.BUSINESS, HotelCategory.PREMIUM, new BigDecimal("200.00"), new BigDecimal("80.00"), 
                LocalDateTime.now(), PolicyStatus.ACTIVE, "admin");

        when(travelPolicyRepository.save(any(TravelPolicy.class))).thenReturn(savedPolicy);

        TravelPolicyResponse response = policyService.createPolicy(request, "admin");

        assertNotNull(response);
        assertEquals("G3 Domestic Active", response.getPolicyName());
        assertEquals("SUPERSEDED", oldPolicy.getStatus().name());
        verify(travelPolicyRepository, times(2)).save(any(TravelPolicy.class));
    }

    @Test
    public void createPolicy_ThrowsException_InvalidRates() {
        TravelPolicyRequest request = new TravelPolicyRequest();
        request.setPolicyName("Invalid Rates");
        request.setPerDiemRate(BigDecimal.ZERO); // Invalid
        request.setLocalConveyanceLimit(new BigDecimal("-10.00")); // Invalid

        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () -> {
            policyService.createPolicy(request, "admin");
        });
        assertTrue(ex1.getMessage().contains("Per diem rate must be greater than zero"));
    }

    @Test
    public void createCityTier_ThrowsException_DuplicateCityCountry() {
        CityTierRequest request = new CityTierRequest();
        request.setCityName("Chennai");
        request.setCountry("India");
        request.setTier(CityTierType.TIER1);
        request.setPerDiemRate(new BigDecimal("150.00"));
        request.setHotelCapPerNight(new BigDecimal("100.00"));

        CityTier existing = new CityTier("Chennai", "India", CityTierType.TIER1, new BigDecimal("150.00"), new BigDecimal("100.00"));

        when(cityTierRepository.findByCityNameIgnoreCaseAndCountryIgnoreCase("Chennai", "India"))
                .thenReturn(Optional.of(existing));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            policyService.createCityTier(request);
        });
        assertTrue(ex.getMessage().contains("City tier already exists"));
    }

    @Test
    public void calculateAllowance_Success_IntegratedRetrieval() {
        Grade grade = new Grade("G3", "Manager", "Manager Level", "Active");
        TravelPolicy policy = new TravelPolicy("G3 Domestic Active", "Desc", grade, TravelType.DOMESTIC, 
                FlightClass.BUSINESS, HotelCategory.PREMIUM, new BigDecimal("200.00"), new BigDecimal("80.00"), 
                LocalDateTime.now(), PolicyStatus.ACTIVE, "admin");

        CityTier cityTier = new CityTier("Chennai", "India", CityTierType.TIER1, new BigDecimal("150.00"), new BigDecimal("120.00"));

        when(travelPolicyRepository.findByGrade_IdAndTravelTypeAndStatus("G3", TravelType.DOMESTIC, PolicyStatus.ACTIVE))
                .thenReturn(Optional.of(policy));
        when(cityTierRepository.findByCityNameIgnoreCaseAndCountryIgnoreCase("Chennai", "India"))
                .thenReturn(Optional.of(cityTier));

        TravelAllowanceCalculationResponse allowance = policyService.calculateAllowance("G3", TravelType.DOMESTIC, "Chennai", "India");

        assertNotNull(allowance);
        assertEquals("G3", allowance.getGradeId());
        assertEquals("TIER1", allowance.getTier());
        assertEquals("BUSINESS", allowance.getFlightClass());
        assertEquals("PREMIUM", allowance.getHotelCategory());
        assertEquals(new BigDecimal("150.00"), allowance.getPerDiemRate());
        assertEquals(new BigDecimal("120.00"), allowance.getHotelCapPerNight());
        assertEquals(new BigDecimal("80.00"), allowance.getLocalConveyanceLimit());
    }
}
