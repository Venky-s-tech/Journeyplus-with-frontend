package com.journeyplus.advance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.journeyplus.advance.entity.AdvanceRequest;
import com.journeyplus.advance.entity.AdvanceStatus;

@Repository
public interface AdvanceRequestRepository extends JpaRepository<AdvanceRequest, Long> {
    List<AdvanceRequest> findByEmployee_Id(Long employeeId);
    List<AdvanceRequest> findByTripRequest_Id(Long tripRequestId);
    List<AdvanceRequest> findByStatus(AdvanceStatus status);

    @Query("SELECT a FROM AdvanceRequest a WHERE a.status = com.journeyplus.advance.entity.AdvanceStatus.REQUESTED AND a.tripRequest.approver.id = :managerId")
    List<AdvanceRequest> findPendingApprovals(@Param("managerId") Long managerId);

    @Query("SELECT a FROM AdvanceRequest a WHERE " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:employeeId IS NULL OR a.employee.id = :employeeId) AND " +
           "(:tripId IS NULL OR a.tripRequest.id = :tripId) AND " +
           "(:currency IS NULL OR LOWER(a.currency) = LOWER(:currency))")
    List<AdvanceRequest> filterAdvances(
        @Param("status") AdvanceStatus status,
        @Param("employeeId") Long employeeId,
        @Param("tripId") Long tripId,
        @Param("currency") String currency
    );
}
