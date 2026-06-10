package com.journeyplus.policy.repository;

import com.journeyplus.iam.entity.Role;
import com.journeyplus.policy.entity.TravelPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TravelPolicyRepository extends JpaRepository<TravelPolicy, Long> {
    Optional<TravelPolicy> findByEmployeeRole(Role employeeRole);
}
