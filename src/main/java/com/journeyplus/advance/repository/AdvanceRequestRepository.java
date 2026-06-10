package com.journeyplus.advance.repository;

import com.journeyplus.advance.entity.AdvanceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdvanceRequestRepository extends JpaRepository<AdvanceRequest, Long> {
    List<AdvanceRequest> findByEmployeeId(Long employeeId);
    List<AdvanceRequest> findByTripRequestId(Long tripRequestId);
}
