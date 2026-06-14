package com.journeyplus.advance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.journeyplus.advance.entity.AdvanceRequest;

@Repository
public interface AdvanceRequestRepository extends JpaRepository<AdvanceRequest, Long> {
    // Original method names (resolved by method naming convention)
    List<AdvanceRequest> findByEmployee_Id(Long employeeId);
    List<AdvanceRequest> findByTripRequest_Id(Long tripRequestId);
}
