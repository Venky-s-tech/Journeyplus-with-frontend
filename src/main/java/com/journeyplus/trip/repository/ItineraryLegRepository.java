package com.journeyplus.trip.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.journeyplus.trip.entity.ItineraryLeg;

@Repository
public interface ItineraryLegRepository extends JpaRepository<ItineraryLeg, Long> {
    List<ItineraryLeg> findByTripRequest_Id(Long tripRequestId);
}
