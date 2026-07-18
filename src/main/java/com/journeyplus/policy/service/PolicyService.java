package com.journeyplus.policy.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.journeyplus.config.AuditAction;
import com.journeyplus.iam.entity.Grade;
import com.journeyplus.iam.entity.Role;
import com.journeyplus.iam.repository.GradeRepository;
import com.journeyplus.policy.dto.*;
import com.journeyplus.policy.entity.*;
import com.journeyplus.policy.repository.CityTierRepository;
import com.journeyplus.policy.repository.TravelPolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class PolicyService {

    private static final Logger log = LoggerFactory.getLogger(PolicyService.class);

    private final TravelPolicyRepository travelPolicyRepository;
    private final CityTierRepository cityTierRepository;
    private final GradeRepository gradeRepository;

    public PolicyService(
            TravelPolicyRepository travelPolicyRepository,
            CityTierRepository cityTierRepository,
            GradeRepository gradeRepository) {
        this.travelPolicyRepository = travelPolicyRepository;
        this.cityTierRepository = cityTierRepository;
        this.gradeRepository = gradeRepository;
    }

    // ==========================================
    // Travel Policy CRUD & Versioning
    // ==========================================

    @Transactional
    @AuditAction(module = "POLICY", action = "CREATE_POLICY")
    public TravelPolicyResponse createPolicy(TravelPolicyRequest request, String createdBy) {
        log.info("Creating travel policy for grade: {}, travelType: {}", request.getGradeId(), request.getTravelType());

        if (request.getPerDiemRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Per diem rate must be greater than zero");
        }
        if (request.getLocalConveyanceLimit().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Local conveyance limit must be greater than zero");
        }

        Grade grade = gradeRepository.findById(request.getGradeId())
                .orElseThrow(() -> new IllegalArgumentException("Grade not found with ID: " + request.getGradeId()));

        // Enforce ONE ACTIVE policy rule:
        // Find existing ACTIVE policy for the same Grade + TravelType, mark it as SUPERSEDED.
        if (request.getStatus() == PolicyStatus.ACTIVE) {
            travelPolicyRepository.findByGrade_IdAndTravelTypeAndStatus(grade.getId(), request.getTravelType(), PolicyStatus.ACTIVE)
                    .ifPresent(existing -> {
                        log.info("Superseding existing active policy ID: {} for grade {} and travel type {}",
                                existing.getId(), grade.getId(), request.getTravelType());
                        existing.setStatus(PolicyStatus.SUPERSEDED);
                        travelPolicyRepository.save(existing);
                    });
        }

        TravelPolicy policy = new TravelPolicy(
                request.getPolicyName().trim(),
                request.getDescription() != null ? request.getDescription().trim() : null,
                grade,
                request.getTravelType(),
                request.getFlightClass(),
                request.getHotelCategory(),
                request.getPerDiemRate(),
                request.getLocalConveyanceLimit(),
                LocalDateTime.now(),
                request.getStatus(),
                createdBy
        );

        if (request.getMaxAmountPerTrip() != null) {
            policy.setMaxAmountPerTrip(request.getMaxAmountPerTrip());
        }
        if (request.getRequiresVisaVerification() != null) {
            policy.setRequiresVisaVerification(request.getRequiresVisaVerification());
        }

        TravelPolicy saved = travelPolicyRepository.save(policy);
        return new TravelPolicyResponse(saved);
    }

    @Transactional
    @AuditAction(module = "POLICY", action = "UPDATE_POLICY")
    public TravelPolicyResponse updatePolicy(Long id, TravelPolicyRequest request) {
        log.info("Updating travel policy with ID: {}", id);
        TravelPolicy existingPolicy = travelPolicyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Travel policy not found with ID: " + id));

        if (request.getPerDiemRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Per diem rate must be greater than zero");
        }
        if (request.getLocalConveyanceLimit().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Local conveyance limit must be greater than zero");
        }

        Grade grade = gradeRepository.findById(request.getGradeId())
                .orElseThrow(() -> new IllegalArgumentException("Grade not found with ID: " + request.getGradeId()));

        // Auditable Versioning Pattern:
        // If the policy being updated is ACTIVE, mark it as SUPERSEDED, and create a brand new ACTIVE version.
        if (existingPolicy.getStatus() == PolicyStatus.ACTIVE) {
            log.info("Policy ID: {} is active. Superseding and creating a new version.", id);
            existingPolicy.setStatus(PolicyStatus.SUPERSEDED);
            travelPolicyRepository.save(existingPolicy);

            // Save the new active version
            TravelPolicy newPolicy = new TravelPolicy(
                    request.getPolicyName().trim(),
                    request.getDescription() != null ? request.getDescription().trim() : null,
                    grade,
                    request.getTravelType(),
                    request.getFlightClass(),
                    request.getHotelCategory(),
                    request.getPerDiemRate(),
                    request.getLocalConveyanceLimit(),
                    LocalDateTime.now(),
                    PolicyStatus.ACTIVE,
                    existingPolicy.getCreatedBy()
            );

            if (request.getMaxAmountPerTrip() != null) {
                newPolicy.setMaxAmountPerTrip(request.getMaxAmountPerTrip());
            }
            if (request.getRequiresVisaVerification() != null) {
                newPolicy.setRequiresVisaVerification(request.getRequiresVisaVerification());
            }

            TravelPolicy saved = travelPolicyRepository.save(newPolicy);
            return new TravelPolicyResponse(saved);
        } else {
            // Non-active policies can be updated in-place
            existingPolicy.setPolicyName(request.getPolicyName().trim());
            existingPolicy.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
            existingPolicy.setGrade(grade);
            existingPolicy.setTravelType(request.getTravelType());
            existingPolicy.setFlightClass(request.getFlightClass());
            existingPolicy.setHotelCategory(request.getHotelCategory());
            existingPolicy.setPerDiemRate(request.getPerDiemRate());
            existingPolicy.setLocalConveyanceLimit(request.getLocalConveyanceLimit());
            existingPolicy.setStatus(request.getStatus());
            if (request.getMaxAmountPerTrip() != null) {
                existingPolicy.setMaxAmountPerTrip(request.getMaxAmountPerTrip());
            }
            if (request.getRequiresVisaVerification() != null) {
                existingPolicy.setRequiresVisaVerification(request.getRequiresVisaVerification());
            }
            TravelPolicy saved = travelPolicyRepository.save(existingPolicy);
            return new TravelPolicyResponse(saved);
        }
    }

    @Transactional
    @AuditAction(module = "POLICY", action = "DEACTIVATE_POLICY")
    public TravelPolicyResponse deactivatePolicy(Long id) {
        log.info("Deactivating travel policy with ID: {}", id);
        TravelPolicy policy = travelPolicyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Travel policy not found with ID: " + id));
        policy.setStatus(PolicyStatus.INACTIVE);
        TravelPolicy saved = travelPolicyRepository.save(policy);
        return new TravelPolicyResponse(saved);
    }

    public TravelPolicyResponse getPolicyById(Long id) {
        TravelPolicy policy = travelPolicyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Travel policy not found with ID: " + id));
        return new TravelPolicyResponse(policy);
    }

    public List<TravelPolicyResponse> searchPolicies(String gradeId, TravelType travelType, PolicyStatus status) {
        return travelPolicyRepository.findAll()
                .stream()
                .filter(p -> gradeId == null || (p.getGrade() != null && gradeId.equals(p.getGrade().getId())))
                .filter(p -> travelType == null || travelType == p.getTravelType())
                .filter(p -> status == null || status == p.getStatus())
                .map(TravelPolicyResponse::new)
                .toList();
    }

    public TravelPolicyResponse getEffectivePolicy(String gradeId, TravelType travelType) {
        log.info("Finding effective active policy for grade: {}, travelType: {}", gradeId, travelType);
        TravelPolicy policy = travelPolicyRepository.findByGrade_IdAndTravelTypeAndStatus(gradeId, travelType, PolicyStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("No effective active policy found for grade " + gradeId + " and travel type " + travelType));
        return new TravelPolicyResponse(policy);
    }

    public TravelPolicyResponse getEffectivePolicy(String gradeId, TravelType travelType, LocalDateTime tripDate) {
        log.info("Finding effective active policy for grade: {}, travelType: {}, date: {}", gradeId, travelType, tripDate);
        if (tripDate == null) {
            return getEffectivePolicy(gradeId, travelType);
        }
        List<TravelPolicy> policies = travelPolicyRepository.findListByGrade_IdAndTravelTypeAndStatus(gradeId, travelType, PolicyStatus.ACTIVE)
                .stream()
                .filter(p -> p.getEffectiveDate() != null && !p.getEffectiveDate().isAfter(tripDate))
                .sorted(Comparator.comparing(TravelPolicy::getEffectiveDate).reversed())
                .toList();
        if (policies.isEmpty()) {
            return getEffectivePolicy(gradeId, travelType);
        }
        return new TravelPolicyResponse(policies.get(0));
    }

    // ==========================================
    // City Tier CRUD & Uniqueness
    // ==========================================

    @Transactional
    @AuditAction(module = "POLICY", action = "CREATE_CITY_TIER")
    public CityTier createCityTier(CityTierRequest request) {
        log.info("Creating city tier for city: {}, country: {}", request.getCityName(), request.getCountry());

        if (request.getPerDiemRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Per diem rate must be greater than zero");
        }
        if (request.getHotelCapPerNight().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Hotel cap per night must be greater than zero");
        }

        // Uniqueness check: CityName + Country
        cityTierRepository.findByCityNameIgnoreCaseAndCountryIgnoreCase(request.getCityName(), request.getCountry())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("City tier already exists for city: " + request.getCityName() + " in " + request.getCountry());
                });

        CityTier cityTier = new CityTier(
                request.getCityName().trim(),
                request.getCountry().trim(),
                request.getTier(),
                request.getPerDiemRate(),
                request.getHotelCapPerNight()
        );

        return cityTierRepository.save(cityTier);
    }

    @Transactional
    @AuditAction(module = "POLICY", action = "UPDATE_CITY_TIER")
    public CityTier updateCityTier(Long id, CityTierRequest request) {
        log.info("Updating city tier with ID: {}", id);
        CityTier existing = cityTierRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("City tier not found with ID: " + id));

        if (request.getPerDiemRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Per diem rate must be greater than zero");
        }
        if (request.getHotelCapPerNight().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Hotel cap per night must be greater than zero");
        }

        // Uniqueness check if name/country modified
        if (!existing.getCityName().equalsIgnoreCase(request.getCityName().trim()) ||
                !existing.getCountry().equalsIgnoreCase(request.getCountry().trim())) {
            cityTierRepository.findByCityNameIgnoreCaseAndCountryIgnoreCase(request.getCityName().trim(), request.getCountry().trim())
                    .ifPresent(dup -> {
                        throw new IllegalArgumentException("City tier already exists for city: " + request.getCityName() + " in " + request.getCountry());
                    });
        }

        existing.setCityName(request.getCityName().trim());
        existing.setCountry(request.getCountry().trim());
        existing.setTier(request.getTier());
        existing.setPerDiemRate(request.getPerDiemRate());
        existing.setHotelCapPerNight(request.getHotelCapPerNight());

        return cityTierRepository.save(existing);
    }

    @Transactional
    @AuditAction(module = "POLICY", action = "DELETE_CITY_TIER")
    public void deleteCityTier(Long id) {
        log.info("Deleting city tier with ID: {}", id);
        if (!cityTierRepository.existsById(id)) {
            throw new IllegalArgumentException("City tier not found with ID: " + id);
        }
        cityTierRepository.deleteById(id);
    }

    public CityTier getCityTierById(Long id) {
        return cityTierRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("City tier not found with ID: " + id));
    }

    public List<CityTier> searchCityTiers(CityTierType tier, String country) {
        return cityTierRepository.findAll().stream()
                .filter(ct -> tier == null || tier == ct.getTier())
                .filter(ct -> country == null || country.equalsIgnoreCase(ct.getCountry()))
                .collect(java.util.stream.Collectors.toList());
    }

    public CityCostDetailsResponse getCostDetails(String cityName, String country) {
        log.info("Fetching cost details for city: {}, country: {}", cityName, country);
        CityTier tier = cityTierRepository.findByCityNameIgnoreCaseAndCountryIgnoreCase(cityName, country)
                .orElseThrow(() -> new IllegalArgumentException("No city tier defined for city: " + cityName + " in " + country));
        return new CityCostDetailsResponse(tier);
    }

    // ==========================================
    // Integration Check Method
    // ==========================================
    public TravelAllowanceCalculationResponse calculateAllowance(String gradeId, TravelType travelType, String cityName, String country) {
        log.info("Integrating entitlements for grade: {}, travelType: {}, city: {}, country: {}",
                gradeId, travelType, cityName, country);

        TravelPolicyResponse policy = getEffectivePolicy(gradeId, travelType);
        CityCostDetailsResponse cost = getCostDetails(cityName, country);

        TravelAllowanceCalculationResponse resp = new TravelAllowanceCalculationResponse();
        resp.setGradeId(gradeId);
        resp.setTravelType(travelType.name());
        resp.setCityName(cityName);
        resp.setCountry(country);

        resp.setTier(cost.getTier());
        resp.setFlightClass(policy.getFlightClass());
        resp.setHotelCategory(policy.getHotelCategory());

        // Final per diem rate can be combined or overridden (e.g. sum or tier per diem rate)
        resp.setPerDiemRate(cost.getPerDiemRate());
        resp.setHotelCapPerNight(cost.getHotelCapPerNight());
        resp.setLocalConveyanceLimit(policy.getLocalConveyanceLimit());

        return resp;
    }

    // ==========================================
    // Backward Compatibility Methods for Compliance & Tests
    // ==========================================

    @Transactional
    @AuditAction(module = "POLICY", action = "CREATE_POLICY")
    public TravelPolicy createPolicy(TravelPolicy policy) {
        log.info("Seeding/Creating travel policy via compatibility bridge for role: {}", policy.getEmployeeRole());

        if (policy.getGrade() == null) {
            String gradeId = "G1";
            if (policy.getEmployeeRole() == Role.APPROVING_MANAGER) gradeId = "G3";
            else if (policy.getEmployeeRole() == Role.ADMIN) gradeId = "G6";
            else if (policy.getEmployeeRole() == Role.FINANCE) gradeId = "G4";
            else if (policy.getEmployeeRole() == Role.COMPLIANCE) gradeId = "G5";

            Grade grade = gradeRepository.findById(gradeId).orElse(null);
            policy.setGrade(grade);
        }

        // Validate limits
        if (policy.getPerDiemRate() == null || policy.getPerDiemRate().compareTo(BigDecimal.ZERO) <= 0) {
            policy.setPerDiemRate(new BigDecimal("100.00"));
        }
        if (policy.getLocalConveyanceLimit() == null || policy.getLocalConveyanceLimit().compareTo(BigDecimal.ZERO) <= 0) {
            policy.setLocalConveyanceLimit(new BigDecimal("50.00"));
        }

        // Prevent duplicate active policies for compatibility flows
        if (policy.getGrade() != null) {
            travelPolicyRepository.findByGrade_IdAndTravelTypeAndStatus(policy.getGrade().getId(), policy.getTravelType(), PolicyStatus.ACTIVE)
                    .ifPresent(existing -> {
                        existing.setStatus(PolicyStatus.SUPERSEDED);
                        travelPolicyRepository.save(existing);
                    });
        }

        return travelPolicyRepository.save(policy);
    }

    @Transactional
    @AuditAction(module = "POLICY", action = "CREATE_CITY_TIER")
    public CityTier createCityTier(CityTier cityTier) {
        log.info("Creating city tier via compatibility bridge for city: {}", cityTier.getCityName());

        if (cityTier.getPerDiemRate() == null || cityTier.getPerDiemRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Per diem rate must be greater than zero");
        }

        cityTierRepository.findByCityNameIgnoreCaseAndCountryIgnoreCase(cityTier.getCityName(), cityTier.getCountry())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("City tier already exists for city: " + cityTier.getCityName());
                });

        return cityTierRepository.save(cityTier);
    }

    public TravelPolicy getPolicyByRole(Role role) {
        log.info("Retrieving travel policy via compatibility bridge for role: {}", role);
        return travelPolicyRepository.findByEmployeeRole(role)
                .orElseThrow(() -> new IllegalArgumentException("No travel policy defined for role: " + role));
    }

    public CityTier getCityTier(String cityName) {
        log.info("Retrieving city tier via compatibility bridge for city: {}", cityName);
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
