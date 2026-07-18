package com.journeyplus.advance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.journeyplus.advance.entity.AdvanceRequest;
import com.journeyplus.advance.entity.AdvanceStatus;

@Repository
public interface AdvanceRequestRepository extends JpaRepository<AdvanceRequest, Long> {
    List<AdvanceRequest> findByEmployee_Id(Long employeeId);
    List<AdvanceRequest> findByTripRequest_Id(Long tripRequestId);
    List<AdvanceRequest> findByStatus(AdvanceStatus status);

    List<AdvanceRequest> findByStatusAndTripRequest_Approver_Id(AdvanceStatus status, Long approverId);

    List<AdvanceRequest> findByStatusAndEmployee_IdAndTripRequest_IdAndCurrencyIgnoreCase(
        AdvanceStatus status,
        Long employeeId,
        Long tripId,
        String currency
    );
}





