package com.journeyplus.policy.repository;

import com.journeyplus.iam.entity.Role;
import com.journeyplus.policy.entity.PolicyStatus;
import com.journeyplus.policy.entity.TravelPolicy;
import com.journeyplus.policy.entity.TravelType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TravelPolicyRepository extends JpaRepository<TravelPolicy, Long> {
    Optional<TravelPolicy> findByEmployeeRole(Role employeeRole);

    Optional<TravelPolicy> findByGrade_IdAndTravelTypeAndStatus(String gradeId, TravelType travelType, PolicyStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM TravelPolicy p WHERE p.grade.id = :gradeId AND p.travelType = :travelType AND p.status = com.journeyplus.policy.entity.PolicyStatus.ACTIVE AND p.effectiveDate <= :tripDate ORDER BY p.effectiveDate DESC")
    List<TravelPolicy> findEffectivePoliciesForDate(
            @org.springframework.data.repository.query.Param("gradeId") String gradeId, 
            @org.springframework.data.repository.query.Param("travelType") TravelType travelType, 
            @org.springframework.data.repository.query.Param("tripDate") java.time.LocalDateTime tripDate);

    List<TravelPolicy> findListByGrade_IdAndTravelTypeAndStatus(String gradeId, TravelType travelType, PolicyStatus status);

    default List<TravelPolicy> searchPolicies(
            String gradeId,
            TravelType travelType,
            PolicyStatus status
    ) {
        return findListByGrade_IdAndTravelTypeAndStatus(gradeId, travelType, status);
    }
}





