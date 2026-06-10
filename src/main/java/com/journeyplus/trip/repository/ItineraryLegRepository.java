package com.journeyplus.trip.repository;

import com.journeyplus.trip.entity.ItineraryLeg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItineraryLegRepository extends JpaRepository<ItineraryLeg, Long> {
    List<ItineraryLeg> findByTripRequestId(Long tripRequestId);
}
