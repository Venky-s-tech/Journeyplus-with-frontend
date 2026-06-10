package com.journeyplus.trip.repository;

import com.journeyplus.trip.entity.TripRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripRequestRepository extends JpaRepository<TripRequest, Long> {
    List<TripRequest> findByEmployeeId(Long employeeId);
    List<TripRequest> findByApprovingManagerId(Long managerId);
}
