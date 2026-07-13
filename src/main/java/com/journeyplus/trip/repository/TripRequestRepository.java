package com.journeyplus.trip.repository;

import com.journeyplus.trip.entity.TripRequest;
import com.journeyplus.trip.entity.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripRequestRepository extends JpaRepository<TripRequest, Long> {
    List<TripRequest> findByEmployee_Id(Long employeeId);
    List<TripRequest> findByApprover_Id(Long approverId);

    List<TripRequest> findByEmployee_IdAndStatusAndTravelTypeIgnoreCaseAndDestinationContainingIgnoreCase(Long employeeId, TripStatus status, String travelType, String destination);

    default List<TripRequest> filterTrips(
        Long employeeId,
        TripStatus status,
        String travelType,
        String destination
    ) {
        return findByEmployee_IdAndStatusAndTravelTypeIgnoreCaseAndDestinationContainingIgnoreCase(employeeId, status, travelType, destination);
    }
}





