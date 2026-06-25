package com.journeyplus.policy.repository;

import com.journeyplus.iam.entity.Role;
import com.journeyplus.policy.entity.PolicyStatus;
import com.journeyplus.policy.entity.TravelPolicy;
import com.journeyplus.policy.entity.TravelType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TravelPolicyRepository extends JpaRepository<TravelPolicy, Long> {
    Optional<TravelPolicy> findByEmployeeRole(Role employeeRole);

    Optional<TravelPolicy> findByGrade_IdAndTravelTypeAndStatus(String gradeId, TravelType travelType, PolicyStatus status);

    @Query("SELECT p FROM TravelPolicy p WHERE " +
           "(:gradeId IS NULL OR p.grade.id = :gradeId) AND " +
           "(:travelType IS NULL OR p.travelType = :travelType) AND " +
           "(:status IS NULL OR p.status = :status)")
    List<TravelPolicy> searchPolicies(
            @Param("gradeId") String gradeId,
            @Param("travelType") TravelType travelType,
            @Param("status") PolicyStatus status
    );
}
