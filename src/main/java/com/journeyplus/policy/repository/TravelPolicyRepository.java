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

    List<TravelPolicy> findListByGrade_IdAndTravelTypeAndStatus(String gradeId, TravelType travelType, PolicyStatus status);
}





