package com.journeyplus.trip.repository;

import com.journeyplus.trip.entity.TripRequest;
import com.journeyplus.trip.entity.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripRequestRepository extends JpaRepository<TripRequest, Long> {
    List<TripRequest> findByEmployee_Id(Long employeeId);
    List<TripRequest> findByApprover_Id(Long approverId);

    @Query("SELECT t FROM TripRequest t WHERE " +
           "(:employeeId IS NULL OR t.employee.id = :employeeId) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:travelType IS NULL OR LOWER(t.travelType) = LOWER(:travelType)) AND " +
           "(:destination IS NULL OR LOWER(t.destination) LIKE LOWER(CONCAT('%', :destination, '%')))")
    List<TripRequest> filterTrips(
        @Param("employeeId") Long employeeId,
        @Param("status") TripStatus status,
        @Param("travelType") String travelType,
        @Param("destination") String destination
    );
}





